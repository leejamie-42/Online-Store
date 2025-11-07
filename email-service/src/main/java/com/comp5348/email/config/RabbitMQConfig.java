package com.comp5348.email.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ setup for Email service.
 * 
 * Consumes email events from store.events exchange and sends email notifications.
 * Aligned with store-backend queue configuration including DLX support.
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Exchange names (aligned with store-backend)
    public static final String EXCHANGE_NAME = "store.events";
    public static final String DLX_EXCHANGE = "store.dlx.exchange";

    // Queue names
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_DLQ = "email.dlq";

    // Routing keys
    public static final String EMAIL_ROUTING_KEY = "email.*";

    /**
     * Topic exchange for flexible message routing.
     * Shared with store-backend.
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
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
     * MUST match store-backend configuration to avoid PRECONDITION_FAILED errors.
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
            .build();
    }

    /**
     * Dead letter queue for failed email messages.
     */
    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    /**
     * Bind email queue to topic exchange with email.* routing pattern.
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
            .to(exchange())
            .with(EMAIL_ROUTING_KEY);
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
