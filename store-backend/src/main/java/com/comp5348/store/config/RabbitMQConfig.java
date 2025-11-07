package com.comp5348.store.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for asynchronous messaging.
 *
 * This configuration sets up the complete RabbitMQ topology including:
 * Topic Exchange for flexible routing
 * Dead Letter Exchange (DLX) for failed message handling
 * 4 main queues: email, inventory rollback, product updates, payment refund
 * Dead letter queues for email and inventory and refund operations
 *
 *
 * Routing Strategy:
 *
 * email.* → email.queue
 * inventory.rollback.* → inventory.rollback.queue
 * product.update.* → product.updates.queue
 * payment.refund.* → payment.refund.queue
 *
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String TOPIC_EXCHANGE = "store.events";
    public static final String DLX_EXCHANGE = "store.dlx.exchange";

    // Queue names
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String INVENTORY_ROLLBACK_QUEUE =
        "inventory.rollback.queue";
    public static final String PRODUCT_UPDATES_QUEUE = "product.updates.queue";
    public static final String PAYMENT_REFUND_QUEUE = "payment.refund.queue";

    // Dead letter queues
    public static final String EMAIL_DLQ = "email.dlq";
    public static final String INVENTORY_ROLLBACK_DLQ =
        "inventory.rollback.dlq";
    public static final String PAYMENT_REFUND_DLQ = "payment.refund.dlq";

    // Routing keys
    public static final String EMAIL_ROUTING_KEY = "email.*";
    public static final String INVENTORY_ROUTING_KEY = "inventory.rollback.*";
    public static final String PRODUCT_ROUTING_KEY = "product.update.*";
    public static final String PAYMENT_REFUND_ROUTING_KEY = "payment.refund.*";

    /**
     * Topic exchange for flexible message routing with patterns.
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    /**
     * Dead letter exchange for handling failed messages.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    /**
     * Email queue with dead letter configuration.
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
            .build();
    }

    /**
     * Inventory rollback queue with dead letter configuration.
     */
    @Bean
    public Queue inventoryRollbackQueue() {
        return QueueBuilder.durable(INVENTORY_ROLLBACK_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", INVENTORY_ROLLBACK_DLQ)
            .build();
    }

    /**
     * Product updates queue for warehouse synchronization.
     */
    @Bean
    public Queue productUpdatesQueue() {
        return QueueBuilder.durable(PRODUCT_UPDATES_QUEUE).build();
    }

    @Bean
    public Queue paymentRefundQueue() {
        return QueueBuilder.durable(PAYMENT_REFUND_QUEUE).build();
    }

    /**
     * Dead letter queue for failed email messages.
     */
    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    /**
     * Dead letter queue for failed inventory rollback messages.
     */
    @Bean
    public Queue inventoryRollbackDlq() {
        return QueueBuilder.durable(INVENTORY_ROLLBACK_DLQ).build();
    }

    @Bean
    public Queue paymentRefundDlq() {
        return QueueBuilder.durable(PAYMENT_REFUND_DLQ).build();
    }

    // Bindings

    /**
     * Bind email queue to topic exchange with email.* routing pattern.
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
            .to(topicExchange())
            .with(EMAIL_ROUTING_KEY);
    }

    /**
     * Bind inventory rollback queue to topic exchange.
     */
    @Bean
    public Binding inventoryRollbackBinding() {
        return BindingBuilder.bind(inventoryRollbackQueue())
            .to(topicExchange())
            .with(INVENTORY_ROUTING_KEY);
    }

    /**
     * Bind product updates queue to topic exchange.
     */
    @Bean
    public Binding productUpdatesBinding() {
        return BindingBuilder.bind(productUpdatesQueue())
            .to(topicExchange())
            .with(PRODUCT_ROUTING_KEY);
    }

    @Bean
    public Binding paymentRefundBinding() {
        return BindingBuilder.bind(paymentRefundQueue())
            .to(topicExchange())
            .with(PAYMENT_REFUND_ROUTING_KEY);
    }

    /**
     * Bind email DLQ to dead letter exchange.
     */
    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq())
            .to(deadLetterExchange())
            .with(EMAIL_DLQ);
    }

    /**
     * Bind inventory rollback DLQ to dead letter exchange.
     */
    @Bean
    public Binding inventoryRollbackDlqBinding() {
        return BindingBuilder.bind(inventoryRollbackDlq())
            .to(deadLetterExchange())
            .with(INVENTORY_ROLLBACK_DLQ);
    }

    @Bean
    public Binding paymentRefundDlqBinding() {
        return BindingBuilder.bind(paymentRefundDlq())
            .to(deadLetterExchange())
            .with(PAYMENT_REFUND_DLQ);
    }

    /**
     * JSON message converter for RabbitMQ.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configured with JSON message converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
