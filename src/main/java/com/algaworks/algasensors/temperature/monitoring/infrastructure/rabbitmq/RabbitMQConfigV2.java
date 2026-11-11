package com.algaworks.algasensors.temperature.monitoring.infrastructure.rabbitmq;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMQConfigV2 {

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter(
            JsonMapper jsonMapper
    ) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // --- 1. CONFIGURAÇÃO DA FANOUT (Auditoria e Dashboard) ---
    public static final String FANOUT_EXCHANGE = "sensor.temperature.fanout";
    public static final String QUEUE_AUDIT = "sensor.audit.queue";
    public static final String QUEUE_DASHBOARD = "sensor.dashboard.queue";

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(QUEUE_AUDIT)
                .build();
    }

    @Bean
    public Queue dashboardQueue() {
        return QueueBuilder.durable(QUEUE_DASHBOARD)
                .build();
    }

    @Bean
    public Binding bindAudit(Queue auditQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(auditQueue).to(fanoutExchange);
    }

    @Bean
    public Binding bindDashboard(Queue dashboardQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(dashboardQueue).to(fanoutExchange);
    }


    // --- 2. CONFIGURAÇÃO DA DIRECT (Manutenção Baseada em Severidade) ---
    public static final String DIRECT_EXCHANGE = "sensor.temperature.direct";
    public static final String QUEUE_MAINTENANCE = "sensor.maintenance.queue";
    private static final String ROUTING_KEY_MAINTENANCE = "alerta.critico";
    public static final String QUEUE_MAINTENANCE_DLQ = "sensor.maintenance.queue.dlq";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    public Queue maintenanceQueue() {
        return QueueBuilder.durable(QUEUE_MAINTENANCE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_MAINTENANCE_DLQ)
                .build();
    }

    @Bean
    public Queue deadLetterQueueMaintenanceQueue() {
        return QueueBuilder.durable(QUEUE_MAINTENANCE_DLQ).build();
    }

    @Bean
    public Binding bindMaintenance(Queue maintenanceQueue, DirectExchange directExchange) {
        // Vincula a fila apenas se a chave for exatamente "alerta.critico"
        return BindingBuilder.bind(maintenanceQueue).to(directExchange).with(ROUTING_KEY_MAINTENANCE);
    }


    // --- 3. CONFIGURAÇÃO DA TOPIC (Filtros Regionais / Hierárquicos) ---
    public static final String TOPIC_EXCHANGE = "sensor.temperature.topic";
    public static final String QUEUE_REGIONAL_BR = "sensor.regional.brasil.queue";
    private static final String ROUTING_KEY_REGIONAL_BR = "brasil.#";
    public static final String QUEUE_GLOBAL_ALERTS = "sensor.global.alerts.queue";
    private static final String ROUTING_KEY_GLOBAL_ALERTS = "#.critico";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Queue regionalBrQueue() {
        return QueueBuilder.durable(QUEUE_REGIONAL_BR).build();
    }

    @Bean
    public Queue globalAlertsQueue() {
        return QueueBuilder.durable(QUEUE_GLOBAL_ALERTS).build();
    }

    @Bean
    public Binding bindRegionalBr(Queue regionalBrQueue, TopicExchange topicExchange) {
        // Pega tudo que começa com "brasil" (ex: brasil.setorA.estavel)
        return BindingBuilder.bind(regionalBrQueue).to(topicExchange).with(ROUTING_KEY_REGIONAL_BR);
    }

    @Bean
    public Binding bindGlobalAlerts(Queue globalAlertsQueue, TopicExchange topicExchange) {
        // Pega qualquer país, qualquer setor, desde que termine com "critico" (ex: *.# .critico)
        return BindingBuilder.bind(globalAlertsQueue).to(topicExchange).with(ROUTING_KEY_GLOBAL_ALERTS);
    }

    // --- 4. CONFIGURAÇÃO DA HEADERS (Roteamento por Metadados no Cabeçalho) ---
    public static final String HEADERS_EXCHANGE = "sensor.temperature.headers";
    public static final String QUEUE_CERTIFIED_METRICS = "sensor.certified.metrics.queue";

    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }

    @Bean
    public Queue certifiedMetricsQueue() {
        return QueueBuilder.durable(QUEUE_CERTIFIED_METRICS).build();
    }

    @Bean
    public Binding bindHeadersCertified(Queue certifiedMetricsQueue, HeadersExchange headersExchange) {
        // Forma correta no Spring AMQP usando Map para o x-match (all / and)
        Map<String, Object> headerValues = new HashMap<>();
        headerValues.put("formato", "json");
        headerValues.put("severidade", "critico");

        return BindingBuilder.bind(certifiedMetricsQueue)
                .to(headersExchange)
                .whereAll(headerValues) // Equivalente a x-match = all
                .match();
    }

}
