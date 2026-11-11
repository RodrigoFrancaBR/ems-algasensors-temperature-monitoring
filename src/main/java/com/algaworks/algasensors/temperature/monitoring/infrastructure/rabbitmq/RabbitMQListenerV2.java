package com.algaworks.algasensors.temperature.monitoring.infrastructure.rabbitmq;

import com.algaworks.algasensors.temperature.monitoring.api.model.TemperatureLogData;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

import static com.algaworks.algasensors.temperature.monitoring.infrastructure.rabbitmq.RabbitMQConfigV2.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQListenerV2 {

    @RabbitListener(queues = QUEUE_AUDIT, concurrency = "2-3")
    @SneakyThrows
    public void handleQueueAudit(@Payload TemperatureLogData temperatureLogData) {
        log.info("QUEUE: {} SensorId {} Temp {}", QUEUE_AUDIT, temperatureLogData.getSensorId(), temperatureLogData.getValue());
        Thread.sleep(Duration.ofSeconds(5));
    }

    @RabbitListener(queues = QUEUE_DASHBOARD, concurrency = "2-3")
    @SneakyThrows
    public void handleQueueDashboard(@Payload TemperatureLogData temperatureLogData) {
        log.info("QUEUE: {} SensorId {} Temp {}", QUEUE_DASHBOARD, temperatureLogData.getSensorId(), temperatureLogData.getValue());
        Thread.sleep(Duration.ofSeconds(5));
    }

    @RabbitListener(queues = QUEUE_MAINTENANCE, concurrency = "2-3")
    @SneakyThrows
    public void handleQueueMaintenance(@Payload TemperatureLogData temperatureLogData) {
        if (temperatureLogData.getValue() != null) {
            throw new RuntimeException("Test error");
        }
        log.info("QUEUE: {} SensorId {} Temp {}", QUEUE_MAINTENANCE, temperatureLogData.getSensorId(), temperatureLogData.getValue());
        Thread.sleep(Duration.ofSeconds(5));
    }

    @RabbitListener(queues = QUEUE_REGIONAL_BR, concurrency = "2-3")
    @SneakyThrows
    public void handleQueueRegionalBR(@Payload TemperatureLogData temperatureLogData) {
        log.info("QUEUE: {} SensorId {} Temp {}", QUEUE_REGIONAL_BR, temperatureLogData.getSensorId(), temperatureLogData.getValue());
        Thread.sleep(Duration.ofSeconds(5));
    }

    @RabbitListener(queues = QUEUE_GLOBAL_ALERTS, concurrency = "2-3")
    @SneakyThrows
    public void handleQueueGlobalAlerts(@Payload TemperatureLogData temperatureLogData) {
        log.info("QUEUE: {} SensorId {} Temp {}", QUEUE_GLOBAL_ALERTS, temperatureLogData.getSensorId(), temperatureLogData.getValue());
        Thread.sleep(Duration.ofSeconds(5));
    }

    @RabbitListener(queues = QUEUE_CERTIFIED_METRICS, concurrency = "2-3")
    @SneakyThrows
    public void handleQueueCertifiedMetrics(
            @Payload TemperatureLogData temperatureLogData) {
        log.info("QUEUE: {} SensorId {} Temp {}", QUEUE_CERTIFIED_METRICS, temperatureLogData.getSensorId(), temperatureLogData.getValue());
        Thread.sleep(Duration.ofSeconds(5));
    }
}
