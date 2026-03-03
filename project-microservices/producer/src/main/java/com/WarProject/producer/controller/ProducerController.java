package com.WarProject.producer.controller;

import com.WarProject.producer.kafka.WarEventProducer;
import com.WarProject.usual.model.GameEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/producer")
@RequiredArgsConstructor
public class ProducerController {

    private final WarEventProducer producer;

    @PostMapping("/publish")
    public ResponseEntity<Void> publish(@RequestBody GameEvent event) {
        producer.publish(event);
        return ResponseEntity.ok().build();
    }
}