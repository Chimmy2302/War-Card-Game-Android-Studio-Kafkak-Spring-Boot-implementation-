package com.WarProject.usual.controller;

import com.WarProject.usual.model.GameEvent;
import com.WarProject.usual.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalEventController {

    private final GameService gameService;

    @PostMapping("/game-event")
    public ResponseEntity<Void> handleEvent(@RequestBody GameEvent event) {
        log.info("Internal callback received: type={} gameId={}", event.getType(), event.getGameId());
        switch (event.getType()) {
            case "GAME_CREATED":    gameService.onGameCreated(event);    break;
            case "GAME_JOINED":     gameService.onGameJoined(event);     break;
            case "ROUND_REQUESTED": gameService.onRoundRequested(event); break;
            case "ROUND_RESULT":    gameService.onRoundResult(event);    break;
            case "GAME_OVER":       gameService.onGameOver(event);       break;
            default: log.warn("Unknown event type: {}", event.getType());
        }
        return ResponseEntity.ok().build();
    }
}