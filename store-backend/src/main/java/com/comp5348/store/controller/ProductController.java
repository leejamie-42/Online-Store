package com.comp5348.store.controller;

import com.comp5348.store.dto.ProductResponseDto;
import com.comp5348.store.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product", description = "Product-related operations")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products
     * @return List of all products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        log.info("GET /api/products - Fetching all products");
        List<ProductResponseDto> products = productService.getAllProducts();
        log.info("Returning {} products", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     * @param id Product ID
     * @return Product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(
        @PathVariable Long id
    ) {
        log.info("GET /api/products/{} - Fetching product by ID", id);
        ProductResponseDto product = productService.getProductById(id);
        log.info("Returning product: {}", product.getName());
        return ResponseEntity.ok(product);
    }
}
