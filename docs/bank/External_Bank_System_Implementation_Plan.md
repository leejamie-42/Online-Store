# External Bank System Implementation Plan

## Architecture Overview

**Technology Stack:**

- Spring Boot 3.x (matching store-backend)
- PostgreSQL with Flyway migrations
- RestTemplate for webhook callbacks

**Port:** `8083` (external bank system)

---

## Phase 1: Project Setup & Database Schema

### 1.1 Gradle Project Structure

Create new Spring Boot module at `/bank` (already exists based on git status):

- `build.gradle` - Dependencies: Spring Web, Data JPA, PostgreSQL, Flyway, Lombok, Validation
- `application.yml` / `application-local.yml` - Port 8083, PostgreSQL connection
- Package structure: `com.comp5348.bank`

### 1.2 Flyway Migrations (tables from ERD + stores)

Use timestamped filenames. For example: V202510300001__create_customers_table.sql

Note: the above numbers is example, use actual current timestamp.



**V202510300001__create_customers_table.sql:**

```sql
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**V202510300002__create_accounts_table.sql:**

```sql
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('Personal', 'Business', 'INTERNAL_REVENUE')),
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE CASCADE
);
CREATE INDEX idx_accounts_customer ON accounts(customer_id);
```

**V202510300003__create_transaction_records_table.sql:**

```sql
CREATE TABLE transaction_records (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    memo TEXT,
    to_account BIGINT,
    from_account BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'initiated' 
        CHECK (status IN ('initiated', 'processing', 'completed', 'failed')),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_to_account FOREIGN KEY (to_account) 
        REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_from_account FOREIGN KEY (from_account) 
        REFERENCES accounts(id) ON DELETE SET NULL
);
CREATE INDEX idx_transaction_to_account ON transaction_records(to_account);
CREATE INDEX idx_transaction_from_account ON transaction_records(from_account);
```

**V202510300004__create_merchants_table.sql:**

```sql
CREATE TABLE merchants (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    biller_code VARCHAR(10) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_merchants_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_merchants_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX ux_merchants_biller_code ON merchants(biller_code);
CREATE INDEX idx_merchants_account_id ON merchants(account_id);
CREATE INDEX idx_merchants_customer_id ON merchants(customer_id);
```

**V202510300005__create_bpay_transaction_information_table.sql:**

```sql
CREATE TABLE bpay_transaction_information (
    id BIGSERIAL PRIMARY KEY,
    reference_id VARCHAR(100) NOT NULL UNIQUE,
    biller_code VARCHAR(10) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' 
        CHECK (status IN ('pending', 'paid', 'expired', 'cancelled')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP
);
CREATE INDEX idx_bpay_reference ON bpay_transaction_information(reference_id);
CREATE INDEX idx_bpay_status ON bpay_transaction_information(status);
CREATE INDEX idx_bpay_biller_code ON bpay_transaction_information(biller_code);
```

**V202510300006__seed_data.sql:**

```sql
INSERT INTO customers (id, first_name, last_name) VALUES (1, 'E-Store', 'Business');
INSERT INTO accounts (id, customer_id, name, type, balance) VALUES (1, 1, 'E-Store Business Account', 'Business', 0.00);
-- Register merchant with unique biller code
INSERT INTO merchants (id, customer_id, biller_code, account_id) VALUES (1, 1, '93242', 1);

-- Test customer accounts
INSERT INTO customers (id, first_name, last_name) VALUES (2, 'John', 'Doe');
INSERT INTO accounts (id, customer_id, name, type, balance) VALUES (2, 2, 'John Personal Account', 'Personal', 1000.00);

-- Reset sequences
SELECT setval('customers_id_seq', 2);
SELECT setval('accounts_id_seq', 2);
SELECT setval('merchants_id_seq', 1);
```

**V202510300007__create_webhook_registrations_table.sql:**

```sql
CREATE TABLE webhook_registrations (
    id BIGSERIAL PRIMARY KEY,
    event VARCHAR(100) NOT NULL,
    callback_url VARCHAR(500) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_webhook_event ON webhook_registrations(event);
```

---

## Phase 2: Domain Models & Enums

### 2.1 Entity Classes

**Customer.java:**

```java
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Custom constructor
    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
```

**Account.java:**

```java
@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Version
    @Column(nullable = false)
    private Integer version;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Custom constructor
    public Account(Customer customer, String name, AccountType type) {
        this.customer = customer;
        this.name = name;
        this.type = type;
        this.balance = BigDecimal.ZERO;
    }
}
```

**TransactionRecord.java:**

```java
@Entity
@Table(name = "transaction_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(columnDefinition = "TEXT")
    private String memo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account")
    private Account toAccount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account")
    private Account fromAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Version
    private Integer version;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Custom constructor
    public TransactionRecord(BigDecimal amount, String memo, Account fromAccount, 
                           Account toAccount, TransactionStatus status) {
        this.amount = amount;
        this.memo = memo;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.status = status;
    }
}
```

**BpayTransactionInformation.java:**

```java
@Entity
@Table(name = "bpay_transaction_information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpayTransactionInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reference_id", nullable = false, unique = true)
    private String referenceId;
    
    @Column(name = "biller_code", nullable = false)
    private String billerCode;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BpayStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
```

**Merchant.java:**

```java
@Entity
@Table(name = "merchants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "biller_code", nullable = false, unique = true)
    private String billerCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**WebhookRegistration.java:**

```java
@Entity
@Table(name = "webhook_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String event;
    
    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;
    
    @CreationTimestamp
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
    
    // Custom constructor
    public WebhookRegistration(String event, String callbackUrl) {
        this.event = event;
        this.callbackUrl = callbackUrl;
    }
}
```

### 2.2 Enums

**AccountType.java:**

```java
public enum AccountType {
    Personal, Business, INTERNAL_REVENUE
}
```

**TransactionStatus.java:**

```java
public enum TransactionStatus {
    initiated, processing, completed, failed
}
```

**BpayStatus.java:**

```java
public enum BpayStatus {
    pending, paid, expired, cancelled
}
```

---

## Phase 3: DTOs & Request/Response Objects

### 3.1 Customer Transaction DTOs (based on your controller)

**TransferRequest.java:**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotNull
    private Long toCustomerId;
    @NotNull
    private Long toAccountId;
    @Positive
    private BigDecimal amount;
}
```

**DepositWithdrawRequest.java:**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositWithdrawRequest {
    @Positive
    private BigDecimal amount;
}
```

**TransactionRecordDTO.java:**

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionRecordDTO {
    private Long id;
    private BigDecimal amount;
    private String memo;
    private Long toAccountId;
    private Long fromAccountId;
    private String status;
    private LocalDateTime createdAt;
    
    // Constructor from entity
    public TransactionRecordDTO(TransactionRecord transaction) {
        this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.memo = transaction.getMemo();
        this.toAccountId = transaction.getToAccount() != null ? transaction.getToAccount().getId() : null;
        this.fromAccountId = transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null;
        this.status = transaction.getStatus().name();
        this.createdAt = transaction.getCreatedAt();
    }
}
```

### 3.2 BPAY Integration DTOs (Bank-facing)

**BpayRequest.java:**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpayRequest {
    @NotNull
    private Long accountId;  // Merchant's bank account ID; bank resolves billerCode
    @NotBlank
    private String orderId;
    @Positive
    private BigDecimal amount;
}
```

**BpayResponse.java:**

```java
@Data
@Builder
public class BpayResponse {
    private String billerCode;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime expiresAt;
}
```

**BpayPaymentRequest.java:**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpayPaymentRequest {
    @NotBlank
    private String referenceId;  // e.g., "BP-ORD-123"
    @NotNull
    private Long customerId;  // payer customer id
    @NotNull
    private Long customerAccountId;  // payer account id
}
```

### 3.3 Webhook DTOs

**WebhookRegistrationRequest.java:**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRegistrationRequest {
    @NotBlank
    private String event;  // "PAYMENT_EVENT"
    @NotBlank
    private String callbackUrl;  // e.g., "http://localhost:8081/api/webhooks/payment"
}
```

**PaymentWebhookPayload.java:**

```java
@Data
@Builder
public class PaymentWebhookPayload {
    private String type;  // "BPAY_PAYMENT_COMPLETED"
    private String orderId;
    private String paymentId;  // BPAY reference
    private BigDecimal amount;
    private LocalDateTime paidAt;
}
```

### 3.4 Basic DTOs (Bank Core)

**AccountDTO.java:**

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    private Long id;
    private Long customerId;
    private String name;
    private String type;
    private BigDecimal balance;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor from entity
    public AccountDTO(Account account) {
        this.id = account.getId();
        this.customerId = account.getCustomer().getId();
        this.name = account.getName();
        this.type = account.getType().name();
        this.balance = account.getBalance();
        this.version = account.getVersion();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }
}
```

**CustomerDTO.java:**

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor from entity
    public CustomerDTO(Customer customer) {
        this.id = customer.getId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.createdAt = customer.getCreatedAt();
        this.updatedAt = customer.getUpdatedAt();
    }
}
```

- TransactionRecordDTO (above)

---

## Phase 4: Repository Layer

**CustomerRepository.java:**

```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
```

**AccountRepository.java:**

```java
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerId(Long customerId);
}
```

**TransactionRecordRepository.java:**

```java
@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByToAccountIdOrFromAccountId(Long toAccountId, Long fromAccountId);
}
```

**BpayTransactionRepository.java:**

```java
@Repository
public interface BpayTransactionRepository extends JpaRepository<BpayTransactionInformation, Long> {
    Optional<BpayTransactionInformation> findByReferenceId(String referenceId);
    List<BpayTransactionInformation> findByStatus(BpayStatus status);
}
```

**MerchantRepository.java:**

```java
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByBillerCode(String billerCode);
    Optional<Merchant> findByAccountId(Long accountId);
}
```

**WebhookRegistrationRepository.java:**

```java
@Repository
public interface WebhookRegistrationRepository extends JpaRepository<WebhookRegistration, Long> {
    Optional<WebhookRegistration> findByEvent(String event);
}
```

Additional repositories for core services (if needed):

**AccountTierHistoryRepository.java:**

```java
@Repository
public interface AccountTierHistoryRepository extends JpaRepository<AccountTierHistory, Long> {
    List<AccountTierHistory> findByAccountIdOrderByChangedAtDesc(Long accountId);
}
```

**RevenueAccountRepository.java:**

```java
@Repository
public interface RevenueAccountRepository extends JpaRepository<RevenueAccount, Long> {
    Optional<RevenueAccount> findTopByOrderByIdAsc();
}
```

---

## Phase 5: Business Logic Services

### 5.1 Basic Services (Account, Customer, Transactions)

- AccountService
  - createAccount(customerId, name, type)
  - getAccount(customerId, accountId)
- CustomerService
  - createCustomer(firstName, lastName)
- TransactionRecordService
  - performTransaction(fromCustomerId, fromAccountId, toCustomerId, toAccountId, amount, memo)
  - getTransactionHistory(customerId, accountId)

### 5.2 TransactionRecordService (Customer Operations)

**TransactionRecordService.java:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionRecordService {
    
    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRepository;
    
    @Transactional
    public TransactionRecordDTO performTransaction(
            Long fromCustomerId, Long fromAccountId,
            Long toCustomerId, Long toAccountId,
            BigDecimal amount, String memo) {
        
        // Validate accounts
        Account fromAccount = null;
        Account toAccount = null;
        
        if (fromAccountId != null) {
            fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("From account not found"));
            if (!fromAccount.getCustomer().getId().equals(fromCustomerId)) {
                throw new UnauthorizedAccessException("Customer does not own from account");
            }
        }
        
        if (toAccountId != null) {
            toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("To account not found"));
            if (toCustomerId != null && !toAccount.getCustomer().getId().equals(toCustomerId)) {
                throw new UnauthorizedAccessException("Customer does not own to account");
            }
        }
        
        // Perform transaction
        TransactionRecord transaction = TransactionRecord.builder()
            .amount(amount)
            .memo(memo)
            .fromAccount(fromAccount)
            .toAccount(toAccount)
            .status(TransactionStatus.processing)
            .build();
        
        // Update balances
        if (fromAccount != null) {
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance");
            }
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountRepository.save(fromAccount);
        }
        
        if (toAccount != null) {
            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountRepository.save(toAccount);
        }
        
        transaction.setStatus(TransactionStatus.completed);
        transaction = transactionRepository.save(transaction);
        
        return toDTO(transaction);
    }
    
    private TransactionRecordDTO toDTO(TransactionRecord transaction) {
        return TransactionRecordDTO.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .memo(transaction.getMemo())
            .toAccountId(transaction.getToAccount() != null ? transaction.getToAccount().getId() : null)
            .fromAccountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null)
            .status(transaction.getStatus().name())
            .createdAt(transaction.getCreatedAt())
            .build();
    }
}
```

### 5.3 BpayService (Store Integration)

**BpayService.java:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class BpayService {
    
    private final BpayTransactionRepository bpayRepository;
    private final AccountRepository accountRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRecordService transactionService;
    private final WebhookService webhookService;
    
    private static final int EXPIRY_HOURS = 24;
    
    /**
 * Generate BPAY payment instructions for Store order
     */
    @Transactional
    public BpayResponse createBpayPayment(BpayRequest request) {
        log.info("Creating BPAY for order {} amount {}", request.getOrderId(), request.getAmount());
        
        // Resolve merchant by account id
        Merchant merchant = merchantRepository.findByAccountId(request.getAccountId())
            .orElseThrow(() -> new MerchantNotFoundException("Merchant not found for accountId"));
        
        
        // Generate unique reference
        String referenceId = "BP-" + request.getOrderId();
        
        // Check for duplicates
        if (bpayRepository.findByReferenceId(referenceId).isPresent()) {
            throw new DuplicateBpayException("BPAY already exists for order: " + request.getOrderId());
        }
        
        // Create BPAY record
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(EXPIRY_HOURS);
        
        BpayTransactionInformation bpay = BpayTransactionInformation.builder()
            .referenceId(referenceId)
            .billerCode(merchant.getBillerCode())
            .amount(request.getAmount())
            .status(BpayStatus.pending)
            .expiredAt(expiresAt)
            .build();
        
        bpay = bpayRepository.save(bpay);
        
        log.info("BPAY created: biller={}, ref={}", merchant.getBillerCode(), referenceId);
        
        return BpayResponse.builder()
            .billerCode(merchant.getBillerCode())
            .referenceNumber(referenceId)
            .amount(request.getAmount())
            .expiresAt(expiresAt)
            .build();
    }
    
    /**
 * Process customer payment (triggered via API)
     */
    @Transactional
    public void processBpayPayment(String referenceId, Long customerId, Long customerAccount) {
        log.info("Processing BPAY payment for reference: {}", referenceId);
        
        BpayTransactionInformation bpay = bpayRepository.findByReferenceId(referenceId)
            .orElseThrow(() -> new BpayNotFoundException("BPAY not found: " + referenceId));
        
        if (bpay.getStatus() != BpayStatus.pending) {
            throw new InvalidBpayStatusException("BPAY already processed: " + bpay.getStatus());
        }
        
        // Update BPAY status
        bpay.setStatus(BpayStatus.paid);
        bpay.setPaidAt(LocalDateTime.now());
        bpayRepository.save(bpay);
        
        // Create transaction record (deposit to merchant account)
        Merchant merchant = merchantRepository.findByBillerCode(bpay.getBillerCode())
            .orElseThrow(() -> new MerchantNotFoundException("Merchant not found for billerCode"));
        transactionService.performTransaction(
            customerId, customerAccount,  // From: external source
            merchant.getCustomer(),
            merchant.getAccount(),
            bpay.getAmount(),
            "BPAY payment: " + referenceId
        );
        
        // Send webhook to Store
        webhookService.sendPaymentCompletedWebhook(bpay);
        
        log.info("BPAY payment processed successfully: {}", referenceId);
    }
}
```

### 5.4 WebhookService (Callback to Store)

**WebhookService.java:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final RestTemplate restTemplate;
    private final WebhookRegistrationRepository webhookRegistrationRepository;
    
    /**
     * Register webhook callback URL from Store (persisted to database)
     */
    @Transactional
    public void registerWebhook(String event, String callbackUrl) {
        WebhookRegistration existing = webhookRegistrationRepository.findByEvent(event).orElse(null);
        
        if (existing != null) {
            // Update existing webhook
            existing.setCallbackUrl(callbackUrl);
            webhookRegistrationRepository.save(existing);
            log.info("Webhook updated for event={}: {}", event, callbackUrl);
        } else {
            // Create new webhook registration
            WebhookRegistration registration = new WebhookRegistration(event, callbackUrl);
            webhookRegistrationRepository.save(registration);
            log.info("Webhook registered for event={}: {}", event, callbackUrl);
        }
    }
    
    public void sendPaymentCompletedWebhook(BpayTransactionInformation bpay) {
        // Fetch the registered webhook URL for PAYMENT_EVENT
        WebhookRegistration registration = webhookRegistrationRepository
                .findByEvent("PAYMENT_EVENT").orElse(null);
        
        if (registration == null) {
            log.warn("No webhook registered for PAYMENT_EVENT, skipping payment notification");
            return;
        }
        
        String webhookUrl = registration.getCallbackUrl();
        
        PaymentWebhookPayload payload = PaymentWebhookPayload.builder()
            .type("BPAY_PAYMENT_COMPLETED")
            .orderId(bpay.getReferenceId().replace("BP-", ""))  // "BP-ORD-1" → "ORD-1"
            .paymentId(bpay.getReferenceId())
            .amount(bpay.getAmount())
            .paidAt(bpay.getPaidAt())
            .build();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentWebhookPayload> entity = new HttpEntity<>(payload, headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                Void.class
            );
            
            log.info("Webhook sent successfully to {}: status={}", webhookUrl, response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to send webhook to {}: {}", webhookUrl, e.getMessage());
        }
    }
}
```


---

## Phase 6: REST Controllers

### 6.1 TransactionRecordController (Customer Operations)

Use the controller structure you provided with enhancements:

```java
@RestController
@RequestMapping("/api/customer/{fromCustomerId}/account/{accountId}/transaction_record")
@RequiredArgsConstructor
public class TransactionRecordController {

    private final TransactionRecordService transactionRecordService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionRecordDTO> transfer(
            @PathVariable Long fromCustomerId,
            @PathVariable("accountId") Long fromAccountId,
            @Valid @RequestBody TransferRequest request) {
        
        TransactionRecordDTO transaction = transactionRecordService.performTransaction(
            fromCustomerId, fromAccountId,
            request.getToCustomerId(), request.getToAccountId(),
            request.getAmount(), "Transfer"
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionRecordDTO> deposit(
            @PathVariable("fromCustomerId") Long toCustomerId,
            @PathVariable("accountId") Long toAccountId,
            @Valid @RequestBody DepositWithdrawRequest request) {
        
        TransactionRecordDTO transaction = transactionRecordService.performTransaction(
            null, null,
            toCustomerId, toAccountId,
            request.getAmount(), "Deposit"
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionRecordDTO> withdraw(
            @PathVariable Long fromCustomerId,
            @PathVariable("accountId") Long fromAccountId,
            @Valid @RequestBody DepositWithdrawRequest request) {
        
        TransactionRecordDTO transaction = transactionRecordService.performTransaction(
            fromCustomerId, fromAccountId,
            null, null,
            request.getAmount(), "Withdraw"
        );
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping
    public ResponseEntity<List<TransactionRecordDTO>> getTransactionHistory(
            @PathVariable("fromCustomerId") Long customerId,
            @PathVariable("accountId") Long accountId) {
        
        log.info("Getting transaction history for customer={}, account={}", customerId, accountId);
        List<TransactionRecordDTO> transactions = transactionRecordService.getTransactionHistory(customerId, accountId);
        return ResponseEntity.ok(transactions);
    }
}
```

### 6.2 Account & Customer Controllers (Core)

- AccountController
  - POST /api/customer/{customerId}/account
  - GET /api/customer/{customerId}/account/{accountId}
  - GET /api/customer/{customerId}/account/{accountId}/transactions
- CustomerController
  - POST /api/customer

### 6.3 BpayController (Store Integration)

**BpayController.java:**

```java
@RestController
@RequestMapping("/bank/api")
@RequiredArgsConstructor
@Slf4j
public class BpayController {
    
    private final BpayService bpayService;
    
    /**
 * Generate BPAY payment instructions
 * POST /bank/api/bpay
     */
    @PostMapping("/bpay")
    public ResponseEntity<BpayResponse> createBpayPayment(
            @Valid @RequestBody BpayRequest request) {
        
        BpayResponse response = bpayService.createBpayPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

Add payment processing endpoint:

```java
@PostMapping("/bpay/pay")
public ResponseEntity<Void> pay(@Valid @RequestBody BpayPaymentRequest request) {
    bpayService.processBpayPayment(request.getReferenceId(), request.getCustomer(), request.getCustomerAccount());
    return ResponseEntity.noContent().build();
}
```

### 6.4 WebhookController (Webhook Registration)

**WebhookController.java:**

```java
@RestController
@RequestMapping("/bank/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    
    private final WebhookService webhookService;
    
    /**
     * Register webhook callback URL (persisted to database)
     * POST /bank/api/webhooks/register
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerWebhook(
            @Valid @RequestBody WebhookRegistrationRequest request) {
        
        log.info("Registering webhook: event={}, url={}", request.getEvent(), request.getCallbackUrl());
        webhookService.registerWebhook(request.getEvent(), request.getCallbackUrl());
        return ResponseEntity.ok().build();
    }
}
```

<!-- Admin simulation endpoint removed: replaced by public /bank/api/bpay/pay -->

---

## Phase 7: Configuration & Exception Handling

### 7.1 Application Configuration

**application.yml:**

```yaml
server:
  port: 8083
spring:
  application:
    name: bank-service
  profiles:
    active: local
```

**application-local.yml:**

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/bank
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### 7.2 RestTemplate Bean

**BankConfig.java:**

```java
@Configuration
public class BankConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
```

<!-- Scheduling not required: auto-payment removed -->

### 7.4 Exception Handling

**Custom Exceptions:**

- `AccountNotFoundException`
- `InsufficientFundsException`
- `UnauthorizedAccessException`
- `InsufficientBalanceException`
- `NegativeTransferAmountException`
- `BpayNotFoundException`
- `DuplicateBpayException`
- `InvalidBpayStatusException`

**GlobalExceptionHandler.java:**

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    // Similar handlers for other exceptions...
    
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        ErrorResponse error = ErrorResponse.builder()
            .message(message)
            .status(status.value())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(status).body(error);
    }
}
```

Add handlers for basic service errors:

```java
@ExceptionHandler(InsufficientBalanceException.class)
public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
    return buildErrorResponse("Insufficient balance", HttpStatus.BAD_REQUEST);
}

@ExceptionHandler(NegativeTransferAmountException.class)
public ResponseEntity<ErrorResponse> handleNegativeAmount(NegativeTransferAmountException ex) {
    return buildErrorResponse("Amount must be positive", HttpStatus.BAD_REQUEST);
}
```

---

## Testing Strategy

### Integration Tests

1. **BPAY Creation Test**: Call `POST /bank/api/bpay` → verify DB row created with status `pending`
2. **Payment API Test**: Call `POST /bank/api/bpay/pay` with `{ referenceId }` → verify status `paid`, merchant balance increased, transaction record created
3. **Webhook Test**: Register webhook → create BPAY → call `/bpay/pay` → verify webhook received with expected payload
4. **Transaction Test**: Transfer between accounts → verify balances updated correctly

### Manual Testing with Postman

1. Create BPAY: `POST localhost:8083/bank/api/bpay`
2. Trigger payment: `POST localhost:8083/bank/api/bpay/pay` with `{ "referenceId": "BP-ORD-1" }`
3. Verify Store receives webhook at `localhost:8081/api/webhooks/payment`

---

## Key Implementation Files Summary

**Database (7 migrations):**

- V20241031000001__create_customers_table.sql
- V20241031000002__create_accounts_table.sql (with name field)
- V20241031000003__create_transaction_records_table.sql
- V20241031000004__create_merchants_table.sql
- V20241031000005__create_bpay_transaction_information_table.sql
- V20241031000006__seed_data.sql
- V20241031000007__create_webhook_registrations_table.sql

**Models (9 entities + 3 enums = 12 files):**

Entities:
- Customer.java (with @CreationTimestamp/@UpdateTimestamp)
- Account.java (with name field, custom constructor, @CreationTimestamp/@UpdateTimestamp)
- TransactionRecord.java (with custom constructor, @CreationTimestamp)
- BpayTransactionInformation.java (with @CreationTimestamp)
- Merchant.java (with @CreationTimestamp/@UpdateTimestamp)
- WebhookRegistration.java (new, with custom constructor)

Enums:
- AccountType.java
- TransactionStatus.java (without 'reversed')
- BpayStatus.java

**DTOs (11 files):**

- TransferRequest.java, DepositWithdrawRequest.java
- TransactionRecordDTO.java (with entity constructor)
- BpayRequest.java (uses accountId)
- BpayPaymentRequest.java (new, for payment API)
- BpayResponse.java
- WebhookRegistrationRequest.java
- PaymentWebhookPayload.java
- AccountDTO.java (with entity constructor)
- CustomerDTO.java (with entity constructor)
- ErrorResponse.java

**Repositories (6 files):**

- CustomerRepository.java
- AccountRepository.java
- TransactionRecordRepository.java
- BpayTransactionRepository.java
- MerchantRepository.java
- WebhookRegistrationRepository.java (new)

**Services (5 files):**

- AccountService.java (returns DTOs, custom constructor usage)
- CustomerService.java (returns DTOs, custom constructor usage)
- TransactionRecordService.java (with getTransactionHistory)
- BpayService.java (BPAY generation & payment processing)
- WebhookService.java (with database-persisted webhook registration)

**Controllers (5 files):**

- AccountController.java (create/get account, returns DTOs)
- CustomerController.java (create customer, returns DTOs)
- TransactionRecordController.java (transfer/deposit/withdraw/getHistory)
- BpayController.java (POST /bank/api/bpay, POST /bank/api/bpay/pay)
- WebhookController.java (webhook registration with event parameter)

**Exceptions (9 files):**

- AccountNotFoundException.java
- BpayNotFoundException.java
- DuplicateBpayException.java
- InsufficientFundsException.java
- InvalidBpayStatusException.java
- MerchantNotFoundException.java
- NegativeTransferAmountException.java
- UnauthorizedAccessException.java
- GlobalExceptionHandler.java

**Configuration (2 files):**

- BankApplication.java (main class)
- BankConfig.java (RestTemplate bean with updated timeout methods)

**Total: ~59 files**

please generate this markdown file 