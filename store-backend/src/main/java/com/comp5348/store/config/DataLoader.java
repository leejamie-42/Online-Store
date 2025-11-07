package com.comp5348.store.config;

import com.comp5348.store.model.Product;
import com.comp5348.store.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Data loader component that seeds the database with initial product data
 * Runs on application startup and loads products from JSON file
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    // private final ProductRepository productRepository;
    // private final ObjectMapper objectMapper;

    @Value("${app.data.seed:true}")
    private boolean seedData;

    @Override
    public void run(String... args) throws Exception {
        if (!seedData) {
            log.info("Data seeding is disabled");
            return;
        }
    }

    // private void loadProducts() {
    //     try {
    //         ClassPathResource resource = new ClassPathResource(
    //             "data/products.json"
    //         );
    //         InputStream inputStream = resource.getInputStream();

    //         List<Product> products = objectMapper.readValue(
    //             inputStream,
    //             new TypeReference<List<Product>>() {}
    //         );

    //         List<Product> savedProducts = productRepository.saveAll(products);
    //         log.info("Loaded {} products into database", savedProducts.size());

    //         // Log each product for debugging
    //         savedProducts.forEach(product ->
    //             log.debug(
    //                 "Loaded product: {} - ${}",
    //                 product.getName(),
    //                 product.getPrice()
    //             )
    //         );
    //     } catch (IOException e) {
    //         log.error("Failed to load product seed data", e);
    //         throw new RuntimeException("Could not load product seed data", e);
    //     }
    // }
}
