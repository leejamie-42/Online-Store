package com.comp5348.store.service;

import com.comp5348.store.dto.ProductResponseDto;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.model.Product;
import com.comp5348.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
                .id(1L)
                .name("Wireless Mouse")
                .description("Ergonomic wireless mouse")
                .price(new BigDecimal("49.99"))
                .imageUrl("https://example.com/mouse.jpg")
                .quantity(25)
                .build();

        testProduct2 = Product.builder()
                .id(2L)
                .name("Mechanical Keyboard")
                .description("RGB backlit keyboard")
                .price(new BigDecimal("129.99"))
                .imageUrl("https://example.com/keyboard.jpg")
                .quantity(15)
                .build();
    }

    @Test
    void whenGetAllProducts_thenReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Wireless Mouse");
        assertThat(result.get(1).getName()).isEqualTo("Mechanical Keyboard");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void whenGetAllProducts_withEmptyDatabase_thenReturnEmptyList() {
        // Given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void whenGetProductById_withValidId_thenReturnProduct() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // When
        ProductResponseDto result = productService.getProductById(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Wireless Mouse");
        assertThat(result.getDescription()).isEqualTo("Ergonomic wireless mouse");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("49.99"));
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/mouse.jpg");
        assertThat(result.getStock()).isEqualTo(25);
        assertThat(result.getPublished()).isTrue();
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void whenGetProductById_withInvalidId_thenThrowProductNotFoundException() {
        // Given
        Long invalidId = 999L;
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(invalidId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 999");
        verify(productRepository, times(1)).findById(invalidId);
    }

    @Test
    void whenMapEntityToDto_thenAllFieldsMappedCorrectly() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct1));

        // When
        ProductResponseDto result = productService.getProductById(1L);

        // Then - verify all fields are mapped correctly
        assertThat(result.getId()).isEqualTo(testProduct1.getId());
        assertThat(result.getName()).isEqualTo(testProduct1.getName());
        assertThat(result.getDescription()).isEqualTo(testProduct1.getDescription());
        assertThat(result.getPrice()).isEqualByComparingTo(testProduct1.getPrice());
        assertThat(result.getImageUrl()).isEqualTo(testProduct1.getImageUrl());
        assertThat(result.getStock()).isEqualTo(testProduct1.getQuantity());
        assertThat(result.getPublished()).isTrue();
    }

    @Test
    void whenGetAllProducts_thenPublishedIsAlwaysTrue() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertThat(result).allMatch(ProductResponseDto::getPublished);
    }

    @Test
    void whenGetProductById_withNullDescription_thenHandleGracefully() {
        // Given
        Product productWithoutDescription = Product.builder()
                .id(3L)
                .name("Test Product")
                .description(null)
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
        when(productRepository.findById(3L)).thenReturn(Optional.of(productWithoutDescription));

        // When
        ProductResponseDto result = productService.getProductById(3L);

        // Then
        assertThat(result.getDescription()).isNull();
        assertThat(result.getName()).isEqualTo("Test Product");
    }
}
