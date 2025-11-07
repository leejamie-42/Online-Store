package com.comp5348.deliveryco.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Shipment tracks each individual warehouse pickup
// One delivery can have multiple shipments from different warehouses
// e.g. Order needs 3 laptops but warehouse A only has 2, warehouse B has 1
// So we create 2 shipments - one from A, one from B
@Entity
@Table(name = "shipment")
public class Shipment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Links back to the delivery request
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_request_id", nullable = false)
  private DeliveryRequest deliveryRequest;

  // Which warehouse is this shipment from (using ID for internal tracking)
  @Column(name = "warehouse_id", nullable = false)
  private Long warehouseId;

  // Warehouse address for pickup (delivery drivers need actual address, not just ID)
  @Column(name = "warehouse_address", length = 500)
  private String warehouseAddress;

  // Product ID for this shipment (one shipment per product)
  @Column(name = "product_id", length = 100)
  private String productId;

  // Quantity of items in this shipment
  @Column(name = "quantity")
  private Integer quantity;

  // Shipment status - tracks this specific pickup/delivery
  @Column(name = "status", nullable = false, length = 50)
  private String status;

  // Progress for this shipment (0-100%)
  @Column(name = "progress", nullable = false)
  private Integer progress;

  // When this shipment was created and last updated
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Constructors
  public Shipment() {
    this.status = "PENDING";
    this.progress = 0;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public Shipment(DeliveryRequest deliveryRequest, Long warehouseId) {
    this();
    this.deliveryRequest = deliveryRequest;
    this.warehouseId = warehouseId;
  }

  public Shipment(DeliveryRequest deliveryRequest, Long warehouseId, String warehouseAddress) {
    this();
    this.deliveryRequest = deliveryRequest;
    this.warehouseId = warehouseId;
    this.warehouseAddress = warehouseAddress;
  }

  public Shipment(DeliveryRequest deliveryRequest, Long warehouseId, String warehouseAddress,
                  String productId, Integer quantity) {
    this();
    this.deliveryRequest = deliveryRequest;
    this.warehouseId = warehouseId;
    this.warehouseAddress = warehouseAddress;
    this.productId = productId;
    this.quantity = quantity;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DeliveryRequest getDeliveryRequest() {
    return deliveryRequest;
  }

  public void setDeliveryRequest(DeliveryRequest deliveryRequest) {
    this.deliveryRequest = deliveryRequest;
  }

  public Long getWarehouseId() {
    return warehouseId;
  }

  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
    this.updatedAt = LocalDateTime.now();
  }

  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
    this.updatedAt = LocalDateTime.now();
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getWarehouseAddress() {
    return warehouseAddress;
  }

  public void setWarehouseAddress(String warehouseAddress) {
    this.warehouseAddress = warehouseAddress;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}
