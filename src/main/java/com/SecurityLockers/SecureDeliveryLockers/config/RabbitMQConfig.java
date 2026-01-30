package com.SecurityLockers.SecureDeliveryLockers.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";
    public static final String SCHEDULED_TASK_QUEUE = "scheduled.task.queue";

    // Exchange names
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String FILE_UPLOAD_EXCHANGE = "file.upload.exchange";
    public static final String SCHEDULED_TASK_EXCHANGE = "scheduled.task.exchange";

    // Routing keys
    public static final String EMAIL_ROUTING_KEY = "email.routing.key";
    public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload.routing.key";
    public static final String SCHEDULED_TASK_ROUTING_KEY = "scheduled.task.routing.key";

    // Message converter for JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Listener container factory with JSON converter
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    // ========== EMAIL QUEUE CONFIGURATION ==========
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    // ========== FILE UPLOAD QUEUE CONFIGURATION ==========
    @Bean
    public Queue fileUploadQueue() {
        return QueueBuilder.durable(FILE_UPLOAD_QUEUE).build();
    }

    @Bean
    public TopicExchange fileUploadExchange() {
        return new TopicExchange(FILE_UPLOAD_EXCHANGE);
    }

    @Bean
    public Binding fileUploadBinding() {
        return BindingBuilder
                .bind(fileUploadQueue())
                .to(fileUploadExchange())
                .with(FILE_UPLOAD_ROUTING_KEY);
    }

    // ========== SCHEDULED TASK QUEUE CONFIGURATION ==========
    @Bean
    public Queue scheduledTaskQueue() {
        return QueueBuilder.durable(SCHEDULED_TASK_QUEUE).build();
    }

    @Bean
    public TopicExchange scheduledTaskExchange() {
        return new TopicExchange(SCHEDULED_TASK_EXCHANGE);
    }

    @Bean
    public Binding scheduledTaskBinding() {
        return BindingBuilder
                .bind(scheduledTaskQueue())
                .to(scheduledTaskExchange())
                .with(SCHEDULED_TASK_ROUTING_KEY);
    }
}

