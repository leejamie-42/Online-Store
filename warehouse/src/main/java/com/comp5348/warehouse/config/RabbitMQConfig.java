package com.comp5348.warehouse.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Warehouse service.
 *
 * This configuration sets up:
 * - Topic exchange (aligned with store-backend: store.events)
 * - Inventory rollback queue for consuming rollback events from store-backend
 * - Dead letter queue for failed rollback messages
 * - Product update publishing (to store-backend)
 *
 * Message Flows:
 * - CONSUME: inventory.rollback.* from store-backend
 * - PUBLISH: product.update.sync to store-backend
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names (aligned with store-backend)
    public static final String TOPIC_EXCHANGE = "store.events";
    public static final String DLX_EXCHANGE = "store.dlx.exchange";

    // Queue names
    public static final String INVENTORY_ROLLBACK_QUEUE =
        "inventory.rollback.queue";
    public static final String INVENTORY_ROLLBACK_DLQ =
        "inventory.rollback.dlq";

    // Routing keys
    public static final String PRODUCT_ROUTING_KEY = "product.update.sync";
    public static final String INVENTORY_ROUTING_KEY = "inventory.rollback.*";

    /**
     * Topic exchange for flexible message routing.
     * Shared with store-backend.
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
     * Inventory rollback queue with dead letter configuration.
     * Consumes rollback events from store-backend.
     */
    @Bean
    public Queue inventoryRollbackQueue() {
        return QueueBuilder.durable(INVENTORY_ROLLBACK_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", INVENTORY_ROLLBACK_DLQ)
            .build();
    }

    /**
     * Dead letter queue for failed inventory rollback messages.
     */
    @Bean
    public Queue inventoryRollbackDlq() {
        return QueueBuilder.durable(INVENTORY_ROLLBACK_DLQ).build();
    }

    /**
     * Bind inventory rollback queue to topic exchange.
     * Listens to inventory.rollback.* routing pattern.
     */
    @Bean
    public Binding inventoryRollbackBinding() {
        return BindingBuilder.bind(inventoryRollbackQueue())
            .to(topicExchange())
            .with(INVENTORY_ROUTING_KEY);
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
