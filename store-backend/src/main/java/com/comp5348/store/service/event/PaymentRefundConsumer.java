package com.comp5348.store.service.event;

import com.comp5348.store.config.RabbitMQConfig;
import com.comp5348.store.dto.event.PaymentRefundEvent;
import com.comp5348.store.dto.event.ProductUpdateEvent;
import com.comp5348.store.model.Product;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_REFUND_QUEUE)
    @Transactional
    public void handleProductUpdate(PaymentRefundEvent event) {
        try {
            log.info("Received payment refund: orderId={}", event.getOrderId());

            paymentService.requestRefund(
                event.getOrderId(),
                event.getReason(),
                event.getUserId()
            );

            log.info(
                "Successfully request payment refund for orderId={}",
                event.getOrderId()
            );
        } catch (Exception e) {
            log.error(
                "Failed to process payment refund on orderId={}",
                event.getOrderId(),
                e.getMessage(),
                e
            );
            // Reject and don't requeue - likely a data issue
            throw new ListenerExecutionFailedException(
                "Failed to process request payment refund",
                e
            );
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
        log.info(
            "Creating new product from warehouse sync: productId={}",
            event.getProductId()
        );
        return Product.builder()
            .id(event.getProductId())
            .name(event.getName())
            .price(event.getPrice())
            .quantity(event.getStock()) // Map stock to quantity
            .imageUrl(event.getImageUrl())
            .build();
    }
}
