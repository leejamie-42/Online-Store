package com.comp5348.store.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers
 * Provides consistent error response format across the application
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle UserAlreadyExistsException
     * Returns 409 Conflict when user tries to register with existing email
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(
        UserAlreadyExistsException ex
    ) {
        log.warn("User already exists: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Conflict");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle BadCredentialsException
     * Returns 401 Unauthorized when authentication fails
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
        BadCredentialsException ex
    ) {
        log.warn("Authentication failed: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handle validation errors
     * Returns 400 Bad Request with field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
        MethodArgumentNotValidException ex
    ) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Bad Request");

        Map<String, String> fieldErrors = new HashMap<>();
        ex
            .getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
            );
        errors.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle generic exceptions
     * Returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(
        Exception ex
    ) {
        log.error("Unexpected error occurred", ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            error
        );
    }

    /**
     * Handle ProductNotFoundException
     * Returns 404 Not Found when product is not found
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(
        ProductNotFoundException ex
    ) {
        log.warn("Product not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle OrderNotFoundException
     * Returns 404 Not Found when order is not found
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFound(
        OrderNotFoundException ex
    ) {
        log.warn("Order not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle InsufficientStockException
     * Returns 409 Conflict when there is insufficient stock to fulfill order
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(
        InsufficientStockException ex
    ) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Conflict");
        error.put("message", ex.getMessage());

        // Include detailed stock information if available
        if (ex.getProductId() != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("productId", ex.getProductId());
            details.put("requestedQuantity", ex.getRequestedQuantity());
            details.put("availableQuantity", ex.getAvailableQuantity());
            error.put("details", details);
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle PaymentNotFoundException
     * Returns 404 Not Found when payment is not found
     */
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePaymentNotFound(
        PaymentNotFoundException ex
    ) {
        log.warn("Payment not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle RefundNotFoundException
     * Returns 404 Not Found when refund is not found
     */
    @ExceptionHandler(RefundNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRefundNotFound(
        RefundNotFoundException ex
    ) {
        log.warn("Refund not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle DuplicatePaymentException
     * Returns 400 Bad Request when payment already exists for order
     */
    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicatePayment(
        DuplicatePaymentException ex
    ) {
        log.warn("Duplicate payment: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle DuplicateRefundException
     * Returns 400 Bad Request when refund already exists for payment
     */
    @ExceptionHandler(DuplicateRefundException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateRefund(
        DuplicateRefundException ex
    ) {
        log.warn("Duplicate refund: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle InvalidPaymentStatusException
     * Returns 400 Bad Request when payment status transition is invalid
     */
    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPaymentStatus(
        InvalidPaymentStatusException ex
    ) {
        log.warn("Invalid payment status: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle InvalidPaymentMethodException
     * Returns 400 Bad Request when payment method is invalid
     */
    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPaymentMethod(
        InvalidPaymentMethodException ex
    ) {
        log.warn("Invalid payment method: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle UnsupportedPaymentMethodException
     * Returns 400 Bad Request when payment method is not supported
     */
    @ExceptionHandler(UnsupportedPaymentMethodException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedPaymentMethod(
        UnsupportedPaymentMethodException ex
    ) {
        log.warn("Unsupported payment method: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle UnauthorizedOrderAccessException
     * Returns 403 Forbidden when user tries to access order they don't own
     */
    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedOrderAccess(
        UnauthorizedOrderAccessException ex
    ) {
        log.warn("Unauthorized order access: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle BankServiceException
     * Returns 502 Bad Gateway when Bank Service communication fails
     */
    @ExceptionHandler(BankServiceException.class)
    public ResponseEntity<Map<String, String>> handleBankServiceError(
        BankServiceException ex
    ) {
        log.error("Bank Service error: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Gateway");
        error.put("message", "Payment service temporarily unavailable");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    /**
     * Handle AccessDeniedException (from Spring Security)
     * Returns 403 Forbidden when user tries to access unauthorized resources
     */
    @ExceptionHandler(
        org.springframework.security.access.AccessDeniedException.class
    )
    public ResponseEntity<Map<String, String>> handleAccessDenied(
        org.springframework.security.access.AccessDeniedException ex
    ) {
        log.warn("Access denied: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle IllegalArgumentException
     * Returns 400 Bad Request for invalid arguments
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        log.warn("Invalid argument: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle IllegalStateException
     * Returns 500 Internal Server Error for illegal state operations
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(
        IllegalStateException ex
    ) {
        log.error("Illegal state: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            error
        );
    }

    /**
     * Handle DeliveryServiceException
     * Returns 503 Service Unavailable when delivery service is unavailable
     */
    @ExceptionHandler(DeliveryServiceException.class)
    public ResponseEntity<Map<String, String>> handleDeliveryServiceException(
        DeliveryServiceException ex
    ) {
        log.error("Delivery service error: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Service Unavailable");
        error.put("message", "Delivery service is currently unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handle ShipmentNotFoundException
     * Returns 404 Not Found when shipment is not found
     */
    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleShipmentNotFoundException(
        ShipmentNotFoundException ex
    ) {
        log.warn("Shipment not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
