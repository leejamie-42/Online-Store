package com.comp5348.store.service.event;

import com.comp5348.store.config.RabbitMQConfig;
import com.comp5348.store.dto.event.ProductUpdateEvent;
import com.comp5348.store.model.Product;
import com.comp5348.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer for product update events from Warehouse service.
 *
 * <p>
 * This service listens to the product.updates.queue and synchronizes
 * product catalog data from the Warehouse (source of truth) to Store Backend.
 *
 * <p>
 * Synchronization includes:
 * <ul>
 * <li>Product name, price, stock levels</li>
 * <li>Image URLs and availability status</li>
 * <li>Creates new products if they don't exist locally</li>
 * </ul>
 *
 * <p>
 * Error Handling:
 * Failed updates are rejected and NOT requeued
 * (ListenerExecutionFailedException).
 * This prevents infinite retry loops for data issues. Failed messages go to DLQ
 * if configured.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductUpdateConsumer {

  private final ProductRepository productRepository;

  /**
   * Handle product update event from Warehouse.
   *
   * This method is transactional - updates are committed atomically.
   * If the product doesn't exist, it creates a new one.
   *
   * @param event ProductUpdateEvent from Warehouse service
   * @throws ListenerExecutionFailedException if processing fails (message goes to
   *                                          DLQ)
   */
  @RabbitListener(queues = RabbitMQConfig.PRODUCT_UPDATES_QUEUE)
  @Transactional
  public void handleProductUpdate(ProductUpdateEvent event) {
    try {
      log.info("Received product update: productId={}, stock={}",
          event.getProductId(), event.getStock());

      Product product = productRepository.findById(event.getProductId())
          .orElseGet(() -> createNewProduct(event));

      // Update product details from warehouse
      product.setName(event.getName());
      product.setPrice(event.getPrice());
      product.setQuantity(event.getStock()); // Map stock to quantity
      product.setImageUrl(event.getImageUrl());
      // Note: Product model doesn't have 'published' field, skipping

      productRepository.save(product);

      log.info("Successfully updated product: {}", event.getProductId());

    } catch (Exception e) {
      log.error("Failed to process product update for productId={}: {}",
          event.getProductId(), e.getMessage(), e);
      // Reject and don't requeue - likely a data issue
      throw new ListenerExecutionFailedException(
          "Failed to process product update", e);
    }
  }

  /**
   * Create a new product from warehouse sync event.
   *
   * <p>
   * This is called when Warehouse publishes an update for a product
   * that doesn't exist in Store Backend yet.
   *
   * @param event ProductUpdateEvent
   * @return new Product instance
   */
  private Product createNewProduct(ProductUpdateEvent event) {
    log.info("Creating new product from warehouse sync: productId={}", event.getProductId());
    return Product.builder()
        .id(event.getProductId())
        .name(event.getName())
        .price(event.getPrice())
        .quantity(event.getStock()) // Map stock to quantity
        .imageUrl(event.getImageUrl())
        .build();
  }
}
