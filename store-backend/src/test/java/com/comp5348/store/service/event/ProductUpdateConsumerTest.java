package com.comp5348.store.service.event;

import com.comp5348.store.dto.event.ProductUpdateEvent;
import com.comp5348.store.model.Product;
import com.comp5348.store.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductUpdateConsumer service.
 */
@ExtendWith(MockitoExtension.class)
class ProductUpdateConsumerTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private ProductUpdateConsumer consumer;

  @Test
  void handleProductUpdate_ExistingProduct_UpdatesSuccessfully() {
    // Given
    ProductUpdateEvent event = ProductUpdateEvent.builder()
        .productId(1L)
        .name("Updated Product")
        .price(new BigDecimal("99.99"))
        .stock(50)
        .published(true)
        .imageUrl("https://example.com/image.jpg")
        .timestamp(LocalDateTime.now())
        .build();

    Product existingProduct = Product.builder()
        .id(1L)
        .name("Old Product")
        .price(new BigDecimal("49.99"))
        .quantity(10)
        .imageUrl("https://example.com/old.jpg")
        .build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

    // When
    consumer.handleProductUpdate(event);

    // Then
    verify(productRepository).save(argThat(p -> p.getName().equals("Updated Product") &&
        p.getPrice().compareTo(new BigDecimal("99.99")) == 0 &&
        p.getQuantity().equals(50) &&
        p.getImageUrl().equals("https://example.com/image.jpg")));
  }

  @Test
  void handleProductUpdate_NewProduct_CreatesSuccessfully() {
    // Given
    ProductUpdateEvent event = ProductUpdateEvent.builder()
        .productId(999L)
        .name("New Product")
        .price(new BigDecimal("149.99"))
        .stock(100)
        .published(true)
        .imageUrl("https://example.com/new.jpg")
        .timestamp(LocalDateTime.now())
        .build();

    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    // When
    consumer.handleProductUpdate(event);

    // Then
    verify(productRepository).save(argThat(p -> p.getId().equals(999L) &&
        p.getName().equals("New Product") &&
        p.getPrice().compareTo(new BigDecimal("149.99")) == 0 &&
        p.getQuantity().equals(100)));
  }

  @Test
  void handleProductUpdate_RepositoryException_ThrowsListenerException() {
    // Given
    ProductUpdateEvent event = ProductUpdateEvent.builder()
        .productId(1L)
        .name("Product")
        .price(new BigDecimal("99.99"))
        .stock(50)
        .build();

    Product existingProduct = Product.builder()
        .id(1L)
        .name("Old Product")
        .build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
    when(productRepository.save(any(Product.class)))
        .thenThrow(new RuntimeException("Database error"));

    // When / Then
    assertThrows(ListenerExecutionFailedException.class, () -> consumer.handleProductUpdate(event));

    verify(productRepository).save(any(Product.class));
  }
}
