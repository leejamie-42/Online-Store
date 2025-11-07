package com.comp5348.store.repository;

import com.comp5348.store.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/image.jpg")
                .quantity(10)
                .build();
    }

    @Test
    void whenSaveProduct_thenProductIsPersisted() {
        // Given - testProduct already set up in @BeforeEach

        // When
        Product savedProduct = productRepository.save(testProduct);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Test Product");
        assertThat(savedProduct.getDescription()).isEqualTo("Test Description");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(savedProduct.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(savedProduct.getQuantity()).isEqualTo(10);
        assertThat(savedProduct.getCreatedAt()).isNotNull();
        assertThat(savedProduct.getUpdatedAt()).isNotNull();
    }

    @Test
    void whenFindById_thenReturnProduct() {
        // Given
        Product savedProduct = productRepository.save(testProduct);

        // When
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
        assertThat(foundProduct.get().getName()).isEqualTo("Test Product");
    }

    @Test
    void whenFindById_withInvalidId_thenReturnEmpty() {
        // Given
        Long invalidId = 999L;

        // When
        Optional<Product> foundProduct = productRepository.findById(invalidId);

        // Then
        assertThat(foundProduct).isEmpty();
    }

    @Test
    void whenFindAll_thenReturnAllProducts() {
        // Given
        Product product1 = Product.builder()
                .name("Product 1")
                .price(new BigDecimal("10.00"))
                .quantity(5)
                .build();

        Product product2 = Product.builder()
                .name("Product 2")
                .price(new BigDecimal("20.00"))
                .quantity(15)
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        // When
        List<Product> products = productRepository.findAll();

        // Then
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Product 1", "Product 2");
    }

    @Test
    void whenUpdateProductQuantity_thenQuantityIsUpdated() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        Long productId = savedProduct.getId();

        // When
        savedProduct.setQuantity(25);
        Product updatedProduct = productRepository.save(savedProduct);

        // Then
        Optional<Product> foundProduct = productRepository.findById(productId);
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getQuantity()).isEqualTo(25);
        assertThat(foundProduct.get().getUpdatedAt()).isAfter(foundProduct.get().getCreatedAt());
    }

    @Test
    void whenDeleteProduct_thenProductIsRemoved() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        Long productId = savedProduct.getId();

        // When
        productRepository.deleteById(productId);

        // Then
        Optional<Product> foundProduct = productRepository.findById(productId);
        assertThat(foundProduct).isEmpty();
    }

    @Test
    void whenSaveProductWithNullQuantity_thenDefaultToZero() {
        // Given
        Product productWithNullQuantity = Product.builder()
                .name("Product Without Quantity")
                .price(new BigDecimal("50.00"))
                .quantity(null)
                .build();

        // When
        Product savedProduct = productRepository.save(productWithNullQuantity);

        // Then
        assertThat(savedProduct.getQuantity()).isEqualTo(0);
    }

    @Test
    void whenFindAll_withEmptyDatabase_thenReturnEmptyList() {
        // Given - database is already cleared in @BeforeEach

        // When
        List<Product> products = productRepository.findAll();

        // Then
        assertThat(products).isEmpty();
    }
}
