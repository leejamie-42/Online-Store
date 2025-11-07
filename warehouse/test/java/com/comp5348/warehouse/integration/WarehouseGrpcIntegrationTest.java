package com.comp5348.warehouse.integration;

import com.comp5348.store.grpc.warehouse.*;
import com.comp5348.warehouse.model.Inventory;
import com.comp5348.warehouse.model.Reservation;
import com.comp5348.warehouse.model.Warehouse;
import com.comp5348.warehouse.model.WarehouseProduct;
import com.comp5348.warehouse.repository.InventoryRepository;
import com.comp5348.warehouse.repository.ReservationRepository;
import com.comp5348.warehouse.repository.WarehouseRepository;
import com.comp5348.warehouse.repository.WarehouseProductRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WarehouseGrpcIntegrationTest {

    @Autowired
    private GrpcServerLifecycle grpcServerLifecycle;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WarehouseProductRepository productRepository;

    private ManagedChannel channel;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub stub;

    @BeforeAll
    void setup() {
        setupTestData();

        grpcServerLifecycle.start();
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        stub = WarehouseServiceGrpc.newBlockingStub(channel);
    }

    private void setupTestData() {
        // Clear existing data
        reservationRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        warehouseRepository.deleteAll();

        // Create test warehouses
        Warehouse warehouse1 = new Warehouse(
                "Sydney Warehouse",
                "Primary warehouse in Sydney",
                "123 George Street",
                "Building A",
                "Australia",
                "Sydney",
                "CBD",
                "2000"
        );
        Warehouse savedWarehouse1 = warehouseRepository.save(warehouse1);

        Warehouse warehouse2 = new Warehouse(
                "Melbourne Warehouse",
                "Secondary warehouse in Melbourne",
                "456 Collins Street",
                "Level 2",
                "Australia",
                "Melbourne",
                "VIC",
                "3000"
        );
        Warehouse savedWarehouse2 = warehouseRepository.save(warehouse2);

        // Create test product
        WarehouseProduct product = new WarehouseProduct(
                "Test Product",
                "Test Description",
                99.99,
                "http://test.com/image.jpg",
                true
        );
        WarehouseProduct savedProduct = productRepository.save(product);

        // Create test inventory for warehouse 1
        Inventory inventory1 = new Inventory();
        inventory1.setWarehouseId(savedWarehouse1.getId());
        inventory1.setProductId(savedProduct.getId());
        inventory1.setQuantity(100);
        inventoryRepository.save(inventory1);

        // Create test inventory for warehouse 2 (smaller quantity for multi-warehouse testing)
        Inventory inventory2 = new Inventory();
        inventory2.setWarehouseId(savedWarehouse2.getId());
        inventory2.setProductId(savedProduct.getId());
        inventory2.setQuantity(50);
        inventoryRepository.save(inventory2);
    }

    @AfterAll
    void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
        grpcServerLifecycle.stop();
    }

    @Test
    void testCheckStockResponse_Available() {
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(1L)
                .setQuantity(10)
                .build();

        CheckStockResponse response = stub.checkStock(request);

        assertThat(response).isNotNull();
        assertThat(response.getAvailable()).isTrue();
        assertThat(response.getTotalAvailable()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void testCheckStockResponse_NotAvailable() {
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(1L)
                .setQuantity(200)  // More than available
                .build();

        CheckStockResponse response = stub.checkStock(request);

        assertThat(response).isNotNull();
        assertThat(response.getAvailable()).isFalse();
    }

    @Test
    void testReserveStock_Success() {
        ReserveStockRequest request = ReserveStockRequest.newBuilder()
                .setOrderId(123L)
                .setProductId(1L)
                .setQuantity(10)
                .build();

        ReserveStockResponse response = stub.reserveStock(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getReservedFromWarehousesList()).isNotEmpty();
    }

    @Test
    void testCommitStock_Success_WithDeliveryPackages() {
        // Step 1: Reserve stock for an order
        long orderId = 200L;
        ReserveStockRequest reserveRequest = ReserveStockRequest.newBuilder()
                .setOrderId(orderId)
                .setProductId(1L)
                .setQuantity(10)
                .build();

        ReserveStockResponse reserveResponse = stub.reserveStock(reserveRequest);
        assertThat(reserveResponse.getSuccess()).isTrue();

        // Step 2: Commit the stock
        CommitStockRequest commitRequest = CommitStockRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        CommitStockResponse commitResponse = stub.commitStock(commitRequest);

        // Step 3: Verify response
        assertThat(commitResponse).isNotNull();
        assertThat(commitResponse.getSuccess()).isTrue();
        assertThat(commitResponse.getMessage()).isEqualTo("Stock committed");

        // Step 4: Verify delivery packages are populated
        assertThat(commitResponse.getDeliveryPackagesList()).isNotEmpty();
        assertThat(commitResponse.getDeliveryPackagesList()).hasSize(1);

        DeliveryPackage pkg = commitResponse.getDeliveryPackages(0);
        assertThat(pkg.getWarehouseAddress()).contains("123 George Street");
        assertThat(pkg.getWarehouseAddress()).contains("Sydney");
        assertThat(pkg.getWarehouseAddress()).contains("Australia");
        assertThat(pkg.getProductId()).isEqualTo("1");
        assertThat(pkg.getQuantity()).isEqualTo(10);

        // Step 5: Verify reservation status changed to COMMITTED
        java.util.List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        assertThat(reservations).isNotEmpty();
        reservations.forEach(res -> 
            assertThat(res.getStatus()).isEqualTo(Reservation.Status.COMMITTED)
        );
    }

    @Test
    void testCommitStock_NoReservation() {
        // Try to commit stock for non-existent order
        long nonExistentOrderId = 999999L;
        CommitStockRequest request = CommitStockRequest.newBuilder()
                .setOrderId(nonExistentOrderId)
                .build();

        CommitStockResponse response = stub.commitStock(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No reservation found for order");
        assertThat(response.getDeliveryPackagesList()).isEmpty();
    }

    @Test
    void testRollbackStock_Success() {
        // Step 1: Reserve stock
        long orderId = 300L;
        long productId = 1L;
        int quantity = 20;

        // Get initial inventory
        int initialInventory = inventoryRepository.sumQuantityByProductId(productId);

        ReserveStockRequest reserveRequest = ReserveStockRequest.newBuilder()
                .setOrderId(orderId)
                .setProductId(productId)
                .setQuantity(quantity)
                .build();

        ReserveStockResponse reserveResponse = stub.reserveStock(reserveRequest);
        assertThat(reserveResponse.getSuccess()).isTrue();

        // Verify inventory decreased
        int afterReserve = inventoryRepository.sumQuantityByProductId(productId);
        assertThat(afterReserve).isEqualTo(initialInventory - quantity);

        // Step 2: Rollback the stock
        RollbackStockRequest rollbackRequest = RollbackStockRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        RollbackStockResponse rollbackResponse = stub.rollbackStock(rollbackRequest);

        // Step 3: Verify rollback response
        assertThat(rollbackResponse).isNotNull();
        assertThat(rollbackResponse.getRolledBack()).isTrue();
        assertThat(rollbackResponse.getMessage()).isEqualTo("Stock rolled back successfully");

        // Step 4: Verify inventory restored to original value
        int afterRollback = inventoryRepository.sumQuantityByProductId(productId);
        assertThat(afterRollback).isEqualTo(initialInventory);

        // Step 5: Verify reservation status changed to ROLLED_BACK
        java.util.List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        assertThat(reservations).isNotEmpty();
        reservations.forEach(res -> 
            assertThat(res.getStatus()).isEqualTo(Reservation.Status.ROLLED_BACK)
        );
    }

    @Test
    void testRollbackStock_NoReservation() {
        // Try to rollback stock for non-existent order
        long nonExistentOrderId = 999998L;
        RollbackStockRequest request = RollbackStockRequest.newBuilder()
                .setOrderId(nonExistentOrderId)
                .build();

        RollbackStockResponse response = stub.rollbackStock(request);

        assertThat(response).isNotNull();
        assertThat(response.getRolledBack()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No reservation found for order");
    }

    @Test
    void testReserveStock_MultipleWarehouses() {
        // Step 1: Create a product that needs stock from multiple warehouses
        // Warehouse 1 has 100 units, Warehouse 2 has 50 units
        // Order 120 units to require both warehouses
        long orderId = 400L;
        long productId = 1L;
        int quantity = 120;

        ReserveStockRequest request = ReserveStockRequest.newBuilder()
                .setOrderId(orderId)
                .setProductId(productId)
                .setQuantity(quantity)
                .build();

        // Step 2: Reserve stock
        ReserveStockResponse response = stub.reserveStock(request);

        // Step 3: Verify response
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Reservation successful");

        // Step 4: Verify stock was reserved from multiple warehouses
        assertThat(response.getReservedFromWarehousesList()).hasSize(2);

        // Step 5: Verify reservations created correctly
        java.util.List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        assertThat(reservations).hasSize(2);

        // Verify total quantity matches requested
        int totalReserved = reservations.stream()
                .mapToInt(Reservation::getQuantity)
                .sum();
        assertThat(totalReserved).isEqualTo(quantity);

        // Step 6: Verify inventory deducted from both warehouses
        java.util.List<Inventory> inventories = inventoryRepository.findByProductIdOrderByWarehouseIdPessimistic(productId);
        assertThat(inventories).hasSizeGreaterThanOrEqualTo(2);

        // Warehouse 1 should have 0 units left (100 - 100)
        // Warehouse 2 should have 30 units left (50 - 20)
        int totalRemainingStock = inventories.stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
        assertThat(totalRemainingStock).isEqualTo(30); // 150 - 120 = 30
    }
}
