package com.comp5348.store.service.warehouse;

import com.comp5348.store.grpc.warehouse.*;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WarehouseGrpcClient {

    @GrpcClient("warehouse")
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    /**
     * Check if sufficient stock is available for a product.
     *
     * @param productId the product ID
     * @param quantity  the desired quantity
     * @return CheckStockResponse with availability status
     * @throws StatusRuntimeException if gRPC call fails
     */
    public CheckStockResponse checkStock(Long productId, Integer quantity) {
        log.debug(
                "Checking stock for product {} quantity {}",
                productId,
                quantity);

        try {
            CheckStockRequest request = CheckStockRequest.newBuilder()
                    .setProductId(productId)
                    .setQuantity(quantity)
                    .build();

            CheckStockResponse response = warehouseStub.checkStock(request);

            log.debug(
                    "Stock check result for product {}: available={}, total_available={}",
                    productId,
                    response.getAvailable(),
                    response.getTotalAvailable());

            return response;
        } catch (StatusRuntimeException e) {
            log.error(
                    "Failed to check stock for product {}: {}",
                    productId,
                    e.getStatus());
            throw e;
        }
    }

    /**
     * Reserve stock for an order atomically.
     *
     * <p>
     * This operation is atomic - either the full quantity is reserved or the
     * operation fails.
     * </p>
     *
     * @param productId the product ID
     * @param quantity  the quantity to reserve
     * @param orderId   the order ID for tracking
     * @return ReserveStockResponse with success status and warehouse ID
     * @throws StatusRuntimeException if gRPC call fails
     */
    public ReserveStockResponse reserveStock(
            Long productId,
            Integer quantity,
            Long orderId) {
        log.info(
                "Reserving stock for order {}: product {} quantity {}",
                orderId,
                productId,
                quantity);

        try {
            ReserveStockRequest request = ReserveStockRequest.newBuilder()
                    .setProductId(productId)
                    .setQuantity(quantity)
                    .setOrderId(orderId)
                    .build();

            ReserveStockResponse response = warehouseStub.reserveStock(request);

            if (response.getSuccess()) {
                log.info(
                        "Stock reserved successfully for order {} from warehouses: {}",
                        orderId,
                        response.getReservedFromWarehousesList());
            } else {
                log.warn(
                        "Failed to reserve stock for order {}: {}",
                        orderId,
                        response.getMessage());
            }

            return response;
        } catch (StatusRuntimeException e) {
            log.error(
                    "gRPC error reserving stock for order {}: {}",
                    orderId,
                    e.getStatus());
            throw e;
        }
    }

    /**
     * Commit reserved stock (mark as sold).
     *
     * <p>
     * Called after successful payment to finalize the inventory transaction.
     * </p>
     *
     * @param orderId the order ID
     * @return CommitStockResponse with success status
     * @throws StatusRuntimeException if gRPC call fails
     */
    public CommitStockResponse commitStock(Long orderId) {
        log.info("Committing stock for order {}", orderId);

        try {
            CommitStockRequest request = CommitStockRequest.newBuilder()
                    .setOrderId(orderId)
                    .build();

            CommitStockResponse response = warehouseStub.commitStock(request);

            if (response.getSuccess()) {
                log.info("Stock committed successfully for order {}", orderId);
            } else {
                log.warn(
                        "Failed to commit stock for order {}: {}",
                        orderId,
                        response.getMessage());
            }

            return response;
        } catch (StatusRuntimeException e) {
            log.error(
                    "gRPC error committing stock for order {}: {}",
                    orderId,
                    e.getStatus());
            throw e;
        }
    }

    /**
     * Rollback reserved stock (compensation transaction).
     *
     * <p>
     * Called when order fails (payment timeout, delivery failure, cancellation).
     * </p>
     *
     * @param orderId the order ID
     * @return RollbackStockResponse with rollback status
     * @throws StatusRuntimeException if gRPC call fails
     */
    public RollbackStockResponse rollbackStock(Long orderId) {
        log.warn("Rolling back stock for order {}", orderId);

        try {
            RollbackStockRequest request = RollbackStockRequest.newBuilder()
                    .setOrderId(orderId)
                    .build();

            RollbackStockResponse response = warehouseStub.rollbackStock(request);

            if (response.getRolledBack()) {
                log.info(
                        "Stock rolled back successfully for order {}",
                        orderId);
            } else {
                log.error(
                        "Failed to rollback stock for order {}: {}",
                        orderId,
                        response.getMessage());
            }

            return response;
        } catch (StatusRuntimeException e) {
            log.error(
                    "gRPC error rolling back stock for order {}: {}",
                    orderId,
                    e.getStatus());
            throw e;
        }
    }
}
