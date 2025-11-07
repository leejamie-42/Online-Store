package com.comp5348.warehouse.controller;

import com.comp5348.warehouse.dto.InventoryDTO;
import com.comp5348.warehouse.dto.ReservationDTO;
import com.comp5348.warehouse.dto.WarehouseDTO;
import com.comp5348.warehouse.dto.WarehouseProductDTO;
import com.comp5348.warehouse.model.Reservation;
import com.comp5348.warehouse.repository.ReservationRepository;
import com.comp5348.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Warehouse operations.
 * Handles product listing, stock checking, warehouse admin endpoints, and monitoring.
 */
@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final ReservationRepository reservationRepository;

    // ===== Product Endpoints =====

    /**
     * GET /warehouse/products
     * Returns all warehouse products with current stock info.
     */
    @GetMapping("/products")
    public ResponseEntity<List<WarehouseProductDTO>> getAllProducts() {
        List<WarehouseProductDTO> products = warehouseService.getAllProductsWithStock();
        return ResponseEntity.ok(products);
    }

    // ===== Warehouse Endpoints =====

    /**
     * GET /warehouse/{warehouseId}
     * Returns warehouse detail and current inventory.
     */
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable Long warehouseId) {
        WarehouseDTO warehouse = warehouseService.getWarehouseById(warehouseId);
        return ResponseEntity.ok(warehouse);
    }

    // ===== Inventory Endpoints =====

    /**
     * POST /warehouse/inventory/check
     * Checks if requested product quantities can be fulfilled.
     */
    @PostMapping("/inventory/check")
    public ResponseEntity<Boolean> checkInventory(@RequestBody InventoryDTO request) {
        boolean available = warehouseService.isStockAvailable(
                request.getProductId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(available);
    }

    /**
     * POST /warehouse/inventory/update
     * Updates inventory (reserve/release stock) based on order status.
     */
    @PostMapping("/inventory/update")
    public ResponseEntity<InventoryDTO> updateInventory(@RequestBody InventoryDTO request) {
        InventoryDTO updated = warehouseService.updateStock(request);
        return ResponseEntity.ok(updated);
    }

    // ===== Reservation Monitoring Endpoints =====

    /**
     * GET /warehouse/reservations/order/{orderId}
     * Returns all reservations for a specific order (for debugging/monitoring).
     */
    @GetMapping("/reservations/order/{orderId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByOrder(
            @PathVariable Long orderId) {
        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        List<ReservationDTO> dtos = reservations.stream()
                .map(ReservationDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /warehouse/reservations
     * Returns all reservations (admin endpoint for monitoring).
     */
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationDTO> dtos = reservations.stream()
                .map(ReservationDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}