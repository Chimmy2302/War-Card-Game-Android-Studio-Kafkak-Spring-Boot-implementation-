package com.WarProject.consumer.kafka;

import com.WarProject.usual.model.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class WarEventConsumer {

    private final RestTemplate restTemplate = new RestTemplate();

    public static final Logger log = LoggerFactory.getLogger(WarEventConsumer.class);

    // Forwards every consumed event back to 'usual' module
    // so usual can handle game logic and broadcast via WebSocket
    private static final String USUAL_CALLBACK = "http://localhost:8080/internal/game-event";

    @KafkaListener(
            topics  = "${war.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(GameEvent event) {
        log.info("Consumed: type={} gameId={}", event.getType(), event.getGameId());
        try {
            restTemplate.postForEntity(USUAL_CALLBACK, event, Void.class);
        } catch (Exception e) {
            log.error("Failed to forward event to usual: {}", e.getMessage());
        }
    }
}