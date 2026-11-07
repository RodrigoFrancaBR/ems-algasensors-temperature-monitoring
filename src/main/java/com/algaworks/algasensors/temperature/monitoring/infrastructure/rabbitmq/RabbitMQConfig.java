package com.algaworks.algasensors.temperature.monitoring.infrastructure.rabbitmq;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;


@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_PROCESS_TEMPERATURE = "temperature-monitoring.process-temperature.v1.q";
    public static final String QUEUE_ALERTING = "temperature-monitoring.alerting.v1.q";
    public static final String QUEUE_SMS = "temperature-monitoring.notification-sms.v1.q";
    public static final String QUEUE_EMAIL = "temperature-monitoring.notification-email.v1.q";
    public static final String FANOUT_EXCHANGE_NAME = "temperature-processing.temperature-received.v1.e";
    public static final String DIRECT_EXCHANGE_NAME = "temperature-processing.notification.v1.e";

    public FanoutExchange exchange() {
        return ExchangeBuilder.fanoutExchange(FANOUT_EXCHANGE_NAME).build();
    }

    @Bean
    public Queue queueProcessTemperature() {
        return QueueBuilder.durable(QUEUE_PROCESS_TEMPERATURE).build();
    }

    @Bean
    public Queue queueAlerting() {
        return QueueBuilder.durable(QUEUE_ALERTING).build();
    }

    @Bean
    public Binding bindingProcessTemperature() {
        return BindingBuilder.bind(queueProcessTemperature()).to(exchange());
    }

    @Bean
    public Binding bindingAlerting() {
        return BindingBuilder.bind(queueAlerting()).to(exchange());
    }

    @Bean
    public Queue queueSms() {
        return QueueBuilder.durable(QUEUE_SMS).build();
    }

    @Bean
    public Queue queueEmail() {
        return QueueBuilder.durable(QUEUE_EMAIL).build();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return ExchangeBuilder.directExchange(DIRECT_EXCHANGE_NAME).build();
    }

    @Bean
    public Binding bindingSms() {
        return BindingBuilder.bind(queueSms())
                .to(notificationExchange())
                .with("notification.sms");
    }

    @Bean
    public Binding bindingEmail() {
        return BindingBuilder.bind(queueEmail())
                .to(notificationExchange())
                .with("notification.email");
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter(
            JsonMapper mapper
    ) {
        return new JacksonJsonMessageConverter( mapper);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

}
