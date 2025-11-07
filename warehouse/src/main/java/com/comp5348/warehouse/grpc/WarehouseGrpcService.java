package com.comp5348.warehouse.grpc;

import com.comp5348.store.grpc.warehouse.*;
import com.comp5348.warehouse.config.RabbitMQConfig;
import com.comp5348.warehouse.dto.event.ProductUpdateEvent;
import com.comp5348.warehouse.model.Inventory;
import com.comp5348.warehouse.model.Reservation;
import com.comp5348.warehouse.model.Warehouse;
import com.comp5348.warehouse.model.WarehouseProduct;
import com.comp5348.warehouse.repository.InventoryRepository;
import com.comp5348.warehouse.repository.ReservationRepository;
import com.comp5348.warehouse.repository.WarehouseProductRepository;
import com.comp5348.warehouse.repository.WarehouseRepository;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class WarehouseGrpcService
    extends WarehouseServiceGrpc.WarehouseServiceImplBase {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final RabbitTemplate rabbitTemplate;

    // Helper function to send product stock updates to store-backend
    private void publishProductUpdate(Long productId) {
        int totalStock = inventoryRepository.sumQuantityByProductId(productId);
        WarehouseProduct product = warehouseProductRepository
            .findById(productId)
            .orElse(null);

        ProductUpdateEvent event = ProductUpdateEvent.builder()
            .productId(productId)
            .stock(totalStock)
            .name(product != null ? product.getName() : "Unknown")
            .price(product != null ? product.getPrice() : 0.0)
            .published(product != null ? product.isPublished() : false)
            .imageUrl(product != null ? product.getImageUrl() : null)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        log.info(
            "Publishing stock update for product={} -> stock={}",
            productId,
            totalStock
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TOPIC_EXCHANGE,
            RabbitMQConfig.PRODUCT_ROUTING_KEY,
            event
        );
    }

    /**
     * Check whether enough stock exists for a given product.
     */
    @Override
    public void checkStock(
        CheckStockRequest request,
        StreamObserver<CheckStockResponse> responseObserver
    ) {
        long productId = request.getProductId();
        int quantity = request.getQuantity();
        log.info(
            "Received CheckStock request: product={} qty={}",
            productId,
            quantity
        );

        int totalAvailable = inventoryRepository.sumQuantityByProductId(
            productId
        );
        boolean available = (totalAvailable >= quantity);

        CheckStockResponse response = CheckStockResponse.newBuilder()
            .setAvailable(available)
            .setTotalAvailable(totalAvailable)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Reserve stock for an order across available warehouses.
     */
    @Override
    @Transactional
    public void reserveStock(
        ReserveStockRequest request,
        StreamObserver<ReserveStockResponse> responseObserver
    ) {
        long productId = request.getProductId();
        int quantity = request.getQuantity();
        long orderId = request.getOrderId();
        log.info(
            "ReserveStock request: order={} product={} qty={}",
            orderId,
            productId,
            quantity
        );

        // First, check if the total stock for that product ID across all warehouses is
        // even enough to complete the order
        // If not enough, we immediately return message "insufficient stock" to avoid
        // wasting time
        int totalAvailable = inventoryRepository.sumQuantityByProductId(
            productId
        );
        if (totalAvailable < quantity) {
            log.warn(
                "Not enough stock available for product={} (available={}, requested={})",
                productId,
                totalAvailable,
                quantity
            );
            ReserveStockResponse response = ReserveStockResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Insufficient stock to reserve")
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        List<Inventory> inventories =
            inventoryRepository.findByProductIdOrderByWarehouseIdPessimistic(
                productId
            ); // get all warehouses that have stock of a specific product, ordered by
        // warehouseId
        int remaining = quantity;
        ReserveStockResponse.Builder builder =
            ReserveStockResponse.newBuilder();

        for (Inventory inv : inventories) {
            if (remaining <= 0) break; // we already allocated enough stock for the order

            // greedy algorithm: at each warehouse, we allocate all available stock in that
            // warehouse for our reservation
            int allocatable = Math.min(inv.getQuantity(), remaining);
            if (allocatable > 0) {
                // update the available stock (deduct current stock by the stock we are going to
                // reserve for that order)
                inv.setQuantity(inv.getQuantity() - allocatable);
                inventoryRepository.save(inv);

                // create a new reservation
                Reservation reservation = Reservation.builder()
                    .orderId(orderId)
                    .productId(productId)
                    .warehouseId(inv.getWarehouseId())
                    .quantity(allocatable)
                    .status(Reservation.Status.RESERVED)
                    .build();
                reservationRepository.save(reservation);

                // track which warehouses we already used
                builder.addReservedFromWarehouses(
                    String.valueOf(inv.getWarehouseId())
                );

                // update remaining quantity of product needed to fulfill the order
                remaining = remaining - allocatable;
            }
        }

        // verify if we have sourced enough stock to create reservation or not (atomic)
        if (remaining == 0) {
            builder.setSuccess(true).setMessage("Reservation successful");
        } else {
            builder
                .setSuccess(false)
                .setMessage("Not enough stock to fulfill reservation");
        }

        // Notify store backend of updated stock
        publishProductUpdate(productId);

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * Commit reserved stock after successful payment.
     */
    @Override
    @Transactional
    public void commitStock(
        CommitStockRequest request,
        StreamObserver<CommitStockResponse> responseObserver
    ) {
        long orderId = request.getOrderId();
        log.info("CommitStock request: order={}", orderId);

        // get all reservations associated with that order
        List<Reservation> reservations = reservationRepository.findByOrderId(
            orderId
        );
        // if no reservations exist, we cannot commit
        if (reservations.isEmpty()) {
            CommitStockResponse response = CommitStockResponse.newBuilder()
                .setSuccess(false)
                .setMessage("No reservation found for order")
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        // update reservation status
        for (Reservation res : reservations) {
            res.setStatus(Reservation.Status.COMMITTED);
            reservationRepository.save(res);
        }

        // Notify store backend that stock changed
        publishProductUpdate(reservations.get(0).getProductId());

        // Build delivery packages for each reservation
        List<DeliveryPackage> deliveryPackages = new ArrayList<>();
        for (Reservation res : reservations) {
            Warehouse warehouse = warehouseRepository
                .findById(res.getWarehouseId())
                .orElse(null);

            // Validate warehouse exists and has valid address
            if (warehouse == null) {
                log.error(
                    "Warehouse {} not found for order {} reservation",
                    res.getWarehouseId(),
                    orderId
                );
                CommitStockResponse errorResponse =
                    CommitStockResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(
                            "Warehouse " + res.getWarehouseId() + " not found"
                        )
                        .build();
                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
                return;
            }

            if (!isValidWarehouseAddress(warehouse)) {
                log.error(
                    "Warehouse {} has invalid address for order {} reservation",
                    res.getWarehouseId(),
                    orderId
                );
                CommitStockResponse errorResponse =
                    CommitStockResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(
                            "Warehouse " +
                                res.getWarehouseId() +
                                " has invalid address"
                        )
                        .build();
                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
                return;
            }

            // Format warehouse address using helper method
            String warehouseAddress = formatWarehouseAddress(warehouse);

            DeliveryPackage pkg = DeliveryPackage.newBuilder()
                .setWarehouseAddress(warehouseAddress)
                .setProductId(String.valueOf(res.getProductId()))
                .setQuantity(res.getQuantity())
                .build();

            deliveryPackages.add(pkg);
        }

        // create response that stock committed successfully
        CommitStockResponse response = CommitStockResponse.newBuilder()
            .setSuccess(true)
            .addAllDeliveryPackages(deliveryPackages)
            .setMessage("Stock committed")
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Rollback (release) reserved stock if customer didn't complete payment for
     * order, or customer cancelled their order
     */
    @Override
    @Transactional
    public void rollbackStock(
        RollbackStockRequest request,
        StreamObserver<RollbackStockResponse> responseObserver
    ) {
        long orderId = request.getOrderId();
        log.info("RollbackStock request: order={}", orderId);

        List<Reservation> reservations = reservationRepository.findByOrderId(
            orderId
        );
        // if no reservations found, there is nothing to roll back
        if (reservations.isEmpty()) {
            RollbackStockResponse response = RollbackStockResponse.newBuilder()
                .setRolledBack(false)
                .setMessage("No reservation found for order")
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        // return stock to inventory
        for (Reservation res : reservations) {
            // update product stock information (add rolled back qty to current stock)
            Inventory inv = inventoryRepository
                .findByWarehouseIdAndProductId(
                    res.getWarehouseId(),
                    res.getProductId()
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Inventory not found for rollback"
                    )
                );
            inv.setQuantity(inv.getQuantity() + res.getQuantity());
            inventoryRepository.save(inv);

            // mark reservation as rolled back
            res.setStatus(Reservation.Status.ROLLED_BACK);
            reservationRepository.save(res);
        }

        // Notify store backend after rollback
        if (!reservations.isEmpty()) {
            publishProductUpdate(reservations.get(0).getProductId());
        }

        RollbackStockResponse response = RollbackStockResponse.newBuilder()
            .setRolledBack(true)
            .setMessage("Stock rolled back successfully")
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Validate that a warehouse has all required address fields populated.
     *
     * @param warehouse the warehouse to validate
     * @return true if warehouse has valid address, false otherwise
     */
    private boolean isValidWarehouseAddress(Warehouse warehouse) {
        return (
            warehouse.getAddressLine1() != null &&
            !warehouse.getAddressLine1().isBlank() &&
            warehouse.getCity() != null &&
            !warehouse.getCity().isBlank() &&
            warehouse.getCountry() != null &&
            !warehouse.getCountry().isBlank()
        );
    }

    /**
     * Format warehouse address for delivery package.
     * Handles optional fields (suburb, postcode) gracefully.
     *
     * @param warehouse the warehouse to format address for
     * @return formatted address string
     */
    private String formatWarehouseAddress(Warehouse warehouse) {
        String suburb = warehouse.getSuburb() != null
            ? warehouse.getSuburb()
            : "";
        String postcode = warehouse.getPostcode() != null
            ? warehouse.getPostcode()
            : "";

        return String.format(
            "%s, %s %s %s, %s",
            warehouse.getAddressLine1(),
            warehouse.getCity(),
            suburb,
            postcode,
            warehouse.getCountry()
        )
            .replaceAll("\\s+", " ")
            .trim();
    }
}
