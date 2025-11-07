package com.comp5348.store.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.comp5348.store.dto.ProductResponseDto;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.service.ProductService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private ProductResponseDto product1;
    private ProductResponseDto product2;

    @BeforeEach
    void setUp() {
        product1 = ProductResponseDto.builder()
            .id(1L)
            .name("Wireless Mouse")
            .description("Ergonomic wireless mouse")
            .price(new BigDecimal("49.99"))
            .imageUrl("https://example.com/mouse.jpg")
            .stock(25)
            .published(true)
            .build();

        product2 = ProductResponseDto.builder()
            .id(2L)
            .name("Mechanical Keyboard")
            .description("RGB backlit keyboard")
            .price(new BigDecimal("129.99"))
            .imageUrl("https://example.com/keyboard.jpg")
            .stock(15)
            .published(true)
            .build();
    }

    @Test
    void whenGetAllProducts_thenReturn200WithProductList() throws Exception {
        // Given
        List<ProductResponseDto> products = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc
            .perform(
                get("/api/products").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].name", is("Wireless Mouse")))
            .andExpect(jsonPath("$[0].price", is(49.99)))
            .andExpect(jsonPath("$[0].stock", is(25)))
            .andExpect(jsonPath("$[0].published", is(true)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].name", is("Mechanical Keyboard")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void whenGetAllProducts_withEmptyDatabase_thenReturn200WithEmptyList()
        throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(
            Collections.emptyList()
        );

        // When & Then
        mockMvc
            .perform(
                get("/api/products").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void whenGetProductById_withValidId_thenReturn200WithProduct()
        throws Exception {
        // Given
        Long productId = 1L;
        when(productService.getProductById(productId)).thenReturn(product1);

        // When & Then
        mockMvc
            .perform(
                get("/api/products/{id}", productId).contentType(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Wireless Mouse")))
            .andExpect(
                jsonPath("$.description", is("Ergonomic wireless mouse"))
            )
            .andExpect(jsonPath("$.price", is(49.99)))
            .andExpect(
                jsonPath("$.imageUrl", is("https://example.com/mouse.jpg"))
            )
            .andExpect(jsonPath("$.stock", is(25)))
            .andExpect(jsonPath("$.published", is(true)));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void whenGetProductById_withInvalidId_thenReturn404() throws Exception {
        // Given
        Long invalidId = 999L;
        when(productService.getProductById(invalidId)).thenThrow(
            new ProductNotFoundException(invalidId)
        );

        // When & Then
        mockMvc
            .perform(
                get("/api/products/{id}", invalidId).contentType(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error", is("Not Found")))
            .andExpect(
                jsonPath("$.message", is("Product not found with id: 999"))
            );

        verify(productService, times(1)).getProductById(invalidId);
    }

    @Test
    void whenGetProductById_thenResponseMatchesApiSpecification()
        throws Exception {
        // Given
        Long productId = 1L;
        when(productService.getProductById(productId)).thenReturn(product1);

        // When & Then - Verify response structure matches API spec
        mockMvc
            .perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.price").exists())
            .andExpect(jsonPath("$.imageUrl").exists())
            .andExpect(jsonPath("$.stock").exists())
            .andExpect(jsonPath("$.published").exists())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").isString())
            .andExpect(jsonPath("$.price").isNumber())
            .andExpect(jsonPath("$.stock").isNumber())
            .andExpect(jsonPath("$.published").isBoolean());
    }

    @Test
    void whenGetAllProducts_thenResponseMatchesApiSpecification()
        throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(
            Arrays.asList(product1)
        );

        // When & Then - Verify list response structure
        mockMvc
            .perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[0].price").exists())
            .andExpect(jsonPath("$[0].stock").exists())
            .andExpect(jsonPath("$[0].published").exists());
    }
    
    
}
