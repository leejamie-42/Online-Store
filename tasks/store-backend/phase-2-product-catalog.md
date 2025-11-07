# Phase 2: Product Catalog Implementation Plan

**Feature**: Product Catalog REST API
**Service**: Store Backend
**Approach**: Test-Driven Development (TDD)
**Version**: 1.0
**Created**: 2025-10-20

---

## Overview

Implement the Product Catalog feature for store-backend following **Test-Driven Development (TDD)** approach with clean commit practices. This feature provides REST APIs for the frontend to display and query product information.

## Implementation Strategy

- **TDD Cycle**: Red (Write failing test) → Green (Make test pass) → Refactor (Clean up)
- **Git Workflow**: Feature branch `feat/backend/product-catalog` with atomic commits
- **Quality Gates**: Clean build + all tests passing before each commit
- **Commit Convention**: Follow Conventional Commits format

---

## API Specification Reference

### Endpoints to Implement

#### 1. Get All Products
```
GET /api/products
Response: 200 OK
[
  {
    "id": "p123",
    "name": "Wireless Mouse",
    "price": 49.99,
    "stock": 12,
    "image_url": "...",
    "published": true
  }
]
```

#### 2. Get Product by ID
```
GET /api/products/{id}
Response: 200 OK
{
  "id": "p123",
  "name": "Wireless Mouse",
  "description": "Ergonomic and precise",
  "price": 49.99,
  "stock": 25,
  "image_url": "https://cdn.com/images/wireless-mouse.png",
  "published": true
}

Response: 404 Not Found (when product doesn't exist)
{
  "timestamp": "2025-10-20T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: p123"
}
```

---

## Database Schema Reference

### Product Entity

| Field       | Type          | Notes                                |
| ----------- | ------------- | ------------------------------------ |
| id          | bigint        | PK, auto-increment                   |
| name        | varchar(255)  | NOT NULL                             |
| description | text          | Nullable                             |
| price       | decimal(10,2) | NOT NULL, must be positive           |
| image_url   | varchar(500)  | Nullable                             |
| quantity    | integer       | NOT NULL, default 0, must be >= 0    |
| created_at  | timestamp     | Auto-generated                       |
| updated_at  | timestamp     | Auto-updated                         |

---

## Task Breakdown

### Task 1: Setup & Data Model Layer

**Objective**: Create Product entity and repository with database schema

**Duration**: 1-2 hours

#### Subtasks

**1.1. Create Product Entity**
- File: `src/main/java/com/comp5348/store/domain/Product.java`
- Requirements:
  - JPA annotations: `@Entity`, `@Table(name = "product")`
  - Primary key: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
  - Fields: id, name, description, price, imageUrl, quantity, createdAt, updatedAt
  - Validation: `@NotNull`, `@NotBlank`, `@Positive`, `@PositiveOrZero`
  - Timestamps: `@CreatedDate`, `@LastModifiedDate` (enable JPA Auditing)
  - Lombok: `@Entity`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
  - Column naming: Use snake_case in DB (`@Column(name = "image_url")`)

**1.2. Create ProductRepository Interface**
- File: `src/main/java/com/comp5348/store/repository/ProductRepository.java`
- Requirements:
  - Extend `JpaRepository<Product, Long>`
  - Add custom query if needed: `Optional<Product> findByIdAndQuantityGreaterThan(Long id, Integer quantity)`

**1.3. Write Repository Integration Tests**
- File: `src/test/java/com/comp5348/store/repository/ProductRepositoryTest.java`
- Test cases:
  ```java
  @DataJpaTest
  @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
  class ProductRepositoryTest {

      @Test
      void whenSaveProduct_thenProductIsPersisted()

      @Test
      void whenFindById_thenReturnProduct()

      @Test
      void whenFindById_withInvalidId_thenReturnEmpty()

      @Test
      void whenFindAll_thenReturnAllProducts()

      @Test
      void whenUpdateProductQuantity_thenQuantityIsUpdated()

      @Test
      void whenDeleteProduct_thenProductIsRemoved()
  }
  ```

**1.4. Run Tests and Build**
```bash
./gradlew :store-backend:test
./gradlew :store-backend:build
```

**1.5. Git Commit**
```bash
git add .
git commit -m "feat(product): add Product entity and repository with tests

- Create Product entity with JPA annotations and validation
- Create ProductRepository extending JpaRepository
- Add comprehensive repository integration tests
- Enable JPA auditing for timestamp management
- All tests passing, clean build"
```

---

### Task 2: Service Layer - Business Logic

**Objective**: Create ProductService with business operations and DTOs

**Duration**: 1-2 hours

#### Subtasks

**2.1. Create DTOs**

- File: `src/main/java/com/comp5348/store/dto/ProductResponseDto.java`
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock; // Maps to quantity
    private Boolean published; // Always true for now
}
```

- File: `src/main/java/com/comp5348/store/dto/ProductListResponseDto.java`
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponseDto {
    private List<ProductResponseDto> products;
    private Integer total;
}
```

**2.2. Write ProductService Tests First (TDD)**

- File: `src/test/java/com/comp5348/store/service/ProductServiceTest.java`
- Test cases:
  ```java
  @ExtendWith(MockitoExtension.class)
  class ProductServiceTest {

      @Mock
      private ProductRepository productRepository;

      @InjectMocks
      private ProductService productService;

      @Test
      void whenGetAllProducts_thenReturnAllProducts()

      @Test
      void whenGetAllProducts_withEmptyDatabase_thenReturnEmptyList()

      @Test
      void whenGetProductById_withValidId_thenReturnProduct()

      @Test
      void whenGetProductById_withInvalidId_thenThrowProductNotFoundException()

      @Test
      void whenMapEntityToDto_thenAllFieldsMappedCorrectly()
  }
  ```

**2.3. Create Custom Exception**

- File: `src/main/java/com/comp5348/store/exception/ProductNotFoundException.java`
```java
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
```

**2.4. Implement ProductService**

- File: `src/main/java/com/comp5348/store/service/ProductService.java`
- Methods:
  ```java
  @Service
  @RequiredArgsConstructor
  public class ProductService {

      private final ProductRepository productRepository;

      public List<ProductResponseDto> getAllProducts()

      public ProductResponseDto getProductById(Long id)

      private ProductResponseDto mapToDto(Product product)
  }
  ```

**2.5. Run Tests and Build**
```bash
./gradlew :store-backend:test
./gradlew :store-backend:build
```

**2.6. Git Commit**
```bash
git add .
git commit -m "feat(product): add ProductService with business logic and DTOs

- Create ProductResponseDto and ProductListResponseDto
- Implement ProductService with getAllProducts and getProductById
- Add ProductNotFoundException for error handling
- Add comprehensive unit tests with Mockito
- All tests passing, clean build"
```

---

### Task 3: Controller Layer - REST API Endpoints

**Objective**: Create ProductController exposing REST endpoints

**Duration**: 2-3 hours

#### Subtasks

**3.1. Write Controller Integration Tests First (TDD)**

- File: `src/test/java/com/comp5348/store/controller/ProductControllerTest.java`
- Test cases:
  ```java
  @WebMvcTest(ProductController.class)
  class ProductControllerTest {

      @Autowired
      private MockMvc mockMvc;

      @MockBean
      private ProductService productService;

      @Test
      void whenGetAllProducts_thenReturn200WithProductList() throws Exception

      @Test
      void whenGetAllProducts_withEmptyDatabase_thenReturn200WithEmptyList() throws Exception

      @Test
      void whenGetProductById_withValidId_thenReturn200WithProduct() throws Exception

      @Test
      void whenGetProductById_withInvalidId_thenReturn404() throws Exception

      @Test
      void whenGetProductById_thenResponseMatchesApiSpecification() throws Exception
  }
  ```

**3.2. Implement ProductController**

- File: `src/main/java/com/comp5348/store/controller/ProductController.java`
```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
}
```

**3.3. Create GlobalExceptionHandler**

- File: `src/main/java/com/comp5348/store/exception/GlobalExceptionHandler.java`
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
```

- File: `src/main/java/com/comp5348/store/dto/ErrorResponse.java`
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
}
```

**3.4. Run Tests and Build**
```bash
./gradlew :store-backend:test
./gradlew :store-backend:build
```

**3.5. Git Commit**
```bash
git add .
git commit -m "feat(product): add ProductController REST endpoints with exception handling

- Implement GET /api/products endpoint
- Implement GET /api/products/{id} endpoint
- Add GlobalExceptionHandler for consistent error responses
- Create ErrorResponse DTO for error formatting
- Add comprehensive controller integration tests
- All tests passing, clean build"
```

---

### Task 4: Seed Data Configuration

**Objective**: Add initial product data for development/testing

**Duration**: 1 hour

#### Subtasks

**4.1. Create products.json**

- File: `src/main/resources/data/products.json`
- Content: Minimum 5 diverse products
```json
[
  {
    "id": 1,
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse with 2.4GHz connectivity and rechargeable battery. Features adjustable DPI settings for precision control.",
    "price": 49.99,
    "imageUrl": "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400",
    "quantity": 25
  },
  {
    "id": 2,
    "name": "Mechanical Keyboard",
    "description": "RGB backlit mechanical keyboard with Cherry MX Blue switches. Full-size layout with dedicated media controls.",
    "price": 129.99,
    "imageUrl": "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=400",
    "quantity": 15
  },
  {
    "id": 3,
    "name": "27-inch Monitor",
    "description": "4K UHD IPS monitor with HDR support and 60Hz refresh rate. Perfect for productivity and content creation.",
    "price": 399.99,
    "imageUrl": "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400",
    "quantity": 8
  },
  {
    "id": 4,
    "name": "USB-C Hub",
    "description": "7-in-1 USB-C hub with HDMI, USB 3.0 ports, SD card reader, and 100W power delivery pass-through.",
    "price": 59.99,
    "imageUrl": "https://images.unsplash.com/photo-1625948515291-69613efd103f?w=400",
    "quantity": 40
  },
  {
    "id": 5,
    "name": "Laptop Stand",
    "description": "Adjustable aluminum laptop stand with ergonomic design. Fits laptops from 10 to 17 inches.",
    "price": 39.99,
    "imageUrl": "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400",
    "quantity": 30
  },
  {
    "id": 6,
    "name": "Noise-Cancelling Headphones",
    "description": "Over-ear wireless headphones with active noise cancellation and 30-hour battery life.",
    "price": 249.99,
    "imageUrl": "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400",
    "quantity": 12
  },
  {
    "id": 7,
    "name": "Webcam 1080p",
    "description": "Full HD webcam with auto-focus and built-in microphone. Ideal for video conferencing.",
    "price": 79.99,
    "imageUrl": "https://images.unsplash.com/photo-1588516903720-8ceb67f9ef84?w=400",
    "quantity": 20
  },
  {
    "id": 8,
    "name": "External SSD 1TB",
    "description": "Portable external SSD with USB 3.2 Gen 2 for fast data transfer up to 1050MB/s.",
    "price": 149.99,
    "imageUrl": "https://images.unsplash.com/photo-1531492746076-161ca9bcad58?w=400",
    "quantity": 18
  }
]
```

**4.2. Create DataLoader Component**

- File: `src/main/java/com/comp5348/store/config/DataLoader.java`
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.data.seed:true}")
    private boolean seedData;

    @Override
    public void run(String... args) throws Exception {
        if (!seedData) {
            log.info("Data seeding is disabled");
            return;
        }

        long productCount = productRepository.count();
        if (productCount > 0) {
            log.info("Database already contains {} products, skipping seed data", productCount);
            return;
        }

        log.info("Loading seed data for products...");
        loadProducts();
        log.info("Seed data loaded successfully");
    }

    private void loadProducts() throws IOException {
        InputStream inputStream = getClass()
                .getResourceAsStream("/data/products.json");

        if (inputStream == null) {
            throw new FileNotFoundException("products.json not found in resources/data/");
        }

        Product[] products = objectMapper.readValue(inputStream, Product[].class);
        List<Product> savedProducts = productRepository.saveAll(Arrays.asList(products));

        log.info("Loaded {} products", savedProducts.size());
    }
}
```

**4.3. Write DataLoader Tests**

- File: `src/test/java/com/comp5348/store/config/DataLoaderTest.java`
```java
@SpringBootTest
class DataLoaderTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void whenApplicationStarts_thenProductsAreLoaded() {
        long count = productRepository.count();
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void whenApplicationStarts_thenProductsHaveValidData() {
        List<Product> products = productRepository.findAll();

        products.forEach(product -> {
            assertThat(product.getName()).isNotBlank();
            assertThat(product.getPrice()).isPositive();
            assertThat(product.getQuantity()).isGreaterThanOrEqualTo(0);
        });
    }
}
```

**4.4. Update application-local.yml**
```yaml
app:
  data:
    seed: true  # Set to false in production
```

**4.5. Run Tests and Build**
```bash
./gradlew :store-backend:test
./gradlew :store-backend:build
```

**4.6. Git Commit**
```bash
git add .
git commit -m "feat(product): add seed data loader with sample products

- Create products.json with 8 diverse products
- Implement DataLoader component to populate database on startup
- Add configuration flag to enable/disable seeding
- Prevent duplicate loading if data exists
- Add tests for data loading functionality
- All tests passing, clean build"
```

---

### Task 5: Integration Testing & Documentation

**Objective**: End-to-end testing and validation

**Duration**: 1 hour

#### Subtasks

**5.1. Create Comprehensive Integration Tests**

- File: `src/test/java/com/comp5348/store/integration/ProductCatalogIntegrationTest.java`
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"app.data.seed=true"})
class ProductCatalogIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void whenGetAllProducts_thenReturnProductList()

    @Test
    void whenGetProductById_withSeedData_thenReturnProduct()

    @Test
    void whenGetProductById_withInvalidId_thenReturn404()

    @Test
    void productResponseMatchesApiSpecification()
}
```

**5.2. Manual API Testing**

Start the application:
```bash
./gradlew :store-backend:bootRun
```

Test endpoints with curl:
```bash
# Get all products
curl -X GET http://localhost:8080/api/products | jq

# Get product by ID
curl -X GET http://localhost:8080/api/products/1 | jq

# Test 404 error
curl -X GET http://localhost:8080/api/products/999 -i
```

**5.3. Verify API Specification Compliance**
- Check response JSON structure matches SYSTEM_INTERFACE_SPEC.md
- Verify HTTP status codes (200, 404)
- Confirm error response format
- Validate data types (price as decimal, stock as integer)

**5.4. Update Documentation**

Create or update:
- `store-backend/README.md`: Document Product API endpoints
- API examples with curl commands
- Note any deviations from specification

**5.5. Final Build and Test**
```bash
./gradlew clean build
./gradlew :store-backend:test --info
```

**5.6. Git Commit**
```bash
git add .
git commit -m "test(product): add integration tests and API documentation

- Create comprehensive integration test suite
- Add manual testing documentation
- Verify API specification compliance
- Update README with Product API examples
- All tests passing, clean build verified"
```

---

## File Structure (Expected Outcome)

```
store-backend/
├── src/
│   ├── main/
│   │   ├── java/com/comp5348/store/
│   │   │   ├── config/
│   │   │   │   └── DataLoader.java
│   │   │   ├── controller/
│   │   │   │   └── ProductController.java
│   │   │   ├── domain/
│   │   │   │   └── Product.java
│   │   │   ├── dto/
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── ProductListResponseDto.java
│   │   │   │   └── ProductResponseDto.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ProductNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java
│   │   │   └── service/
│   │   │       └── ProductService.java
│   │   └── resources/
│   │       ├── data/
│   │       │   └── products.json
│   │       └── application-local.yml
│   └── test/
│       └── java/com/comp5348/store/
│           ├── config/
│           │   └── DataLoaderTest.java
│           ├── controller/
│           │   └── ProductControllerTest.java
│           ├── integration/
│           │   └── ProductCatalogIntegrationTest.java
│           ├── repository/
│           │   └── ProductRepositoryTest.java
│           └── service/
│               └── ProductServiceTest.java
```

---

## Quality Checklist

### Before Each Commit
- [ ] All tests passing: `./gradlew :store-backend:test`
- [ ] Clean build successful: `./gradlew :store-backend:build`
- [ ] Code follows Java naming conventions (camelCase, PascalCase)
- [ ] No compilation warnings
- [ ] Test coverage for new code (aim for >80%)
- [ ] Lombok annotations used appropriately
- [ ] No hardcoded values (use configuration)

### Code Quality Standards
- [ ] Constructor injection for dependencies (Spring Boot 3 default)
- [ ] Use `@RequiredArgsConstructor` for dependency injection
- [ ] Proper exception handling
- [ ] Input validation on DTOs
- [ ] Meaningful variable and method names
- [ ] Comments only where necessary (code should be self-documenting)

### Final Validation
- [ ] API responses match SYSTEM_INTERFACE_SPEC.md
- [ ] Database schema matches ERD.md
- [ ] All REST endpoints functional
- [ ] Seed data loads successfully (8 products)
- [ ] Exception handling works correctly (404 for missing products)
- [ ] CORS configuration allows frontend access
- [ ] No security vulnerabilities introduced

---

## Testing Strategy

### Test Pyramid

```
                    /\
                   /  \
                  / E2E \
                 /______\
                /        \
               / Integration\
              /______________\
             /                \
            /   Unit Tests     \
           /____________________\
```

**Unit Tests (60%)**: Service layer, mappers, utilities
**Integration Tests (30%)**: Repository, API endpoints
**E2E Tests (10%)**: Full workflow with running application

### Test Coverage Goals
- **Overall**: >80%
- **Service Layer**: >90%
- **Controller Layer**: >85%
- **Repository Layer**: >75%

---

## Common Issues & Solutions

### Issue 1: Products.json Not Found
**Error**: `FileNotFoundException: products.json not found`
**Solution**: Ensure file is in `src/main/resources/data/` directory

### Issue 2: Data Loading on Every Startup
**Error**: Duplicate key violation
**Solution**: DataLoader checks `productRepository.count()` before loading

### Issue 3: Test Fails with H2 Database
**Error**: SQL syntax error in tests
**Solution**: Use `@AutoConfigureTestDatabase(replace = Replace.NONE)` for PostgreSQL

### Issue 4: CORS Error in Frontend
**Error**: CORS policy blocking requests
**Solution**: Verify `@CrossOrigin` annotation on controller

### Issue 5: 404 Not Returned Properly
**Error**: Generic error instead of custom error response
**Solution**: Ensure GlobalExceptionHandler has `@RestControllerAdvice`

---

## Dependencies Required

Verify these are in `store-backend/build.gradle`:

```gradle
dependencies {
    // Core Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Database
    runtimeOnly 'org.postgresql:postgresql'

    // JSON Processing (included in spring-boot-starter-web)
    // implementation 'com.fasterxml.jackson.core:jackson-databind'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'com.h2database:h2'
}
```

---

## Estimated Effort

| Task | Duration | Complexity |
|------|----------|------------|
| Task 1: Entity & Repository | 1-2 hours | Medium |
| Task 2: Service Layer | 1-2 hours | Medium |
| Task 3: Controller Layer | 2-3 hours | Medium-High |
| Task 4: Seed Data | 1 hour | Low |
| Task 5: Integration Testing | 1 hour | Medium |
| **Total** | **6-9 hours** | **Medium** |

---

## Success Criteria

### Functional Requirements
✅ `GET /api/products` returns product list matching API spec
✅ `GET /api/products/{id}` returns single product with all fields
✅ `GET /api/products/{id}` returns 404 for non-existent products
✅ Error responses include timestamp, status, error, message, path
✅ 8 products loaded from seed data on application startup

### Non-Functional Requirements
✅ All unit and integration tests passing (>80% coverage)
✅ Clean commit history with meaningful Conventional Commit messages
✅ No compilation errors or warnings
✅ API responses in <200ms for product queries
✅ Code follows Spring Boot best practices

### Quality Requirements
✅ Proper exception handling with GlobalExceptionHandler
✅ Input validation on all endpoints
✅ Database constraints enforced (NOT NULL, POSITIVE)
✅ Proper use of DTOs (no entity exposure in API)
✅ Logging for important operations

---

## Next Steps (Phase 3 Preview)

After completing Product Catalog:

1. **Order Management**: Create order placement workflow
2. **Payment Integration**: Integrate with Bank Service for BPAY
3. **Inventory Management**: Implement gRPC client for Warehouse Service
4. **User Management**: Enhance user profile and order history
5. **Admin Features**: Product management CRUD operations

---

## References

- [SYSTEM_INTERFACE_SPEC.md](../../docs/SYSTEM_INTERFACE_SPEC.md)
- [ERD.md](../../docs/ERD.md)
- [CLAUDE.md](../../CLAUDE.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Document Version**: 1.0
**Last Updated**: 2025-10-20
**Status**: Ready for Implementation
