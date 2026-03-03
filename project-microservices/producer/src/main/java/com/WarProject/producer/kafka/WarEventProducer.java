package com.WarProject.producer.kafka;

import com.WarProject.usual.model.GameEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarEventProducer {

    private final KafkaTemplate<String, GameEvent> kafkaTemplate;

    @Value("${war.kafka.topic}")
    private String topic;

    public void publish(GameEvent event) {
        log.info("Publishing: type={} gameId={}", event.getType(), event.getGameId());
        kafkaTemplate.send(topic, event.getGameId(), event);
    }
}