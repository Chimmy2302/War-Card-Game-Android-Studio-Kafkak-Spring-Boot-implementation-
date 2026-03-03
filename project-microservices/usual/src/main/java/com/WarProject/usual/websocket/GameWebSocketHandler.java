package com.WarProject.usual.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.WarProject.usual.model.WsGameStateEvent;
import com.WarProject.usual.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final GameRepository gameRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String gameId = extractGameId(session);
        sessions.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WS connected: session={} gameId={}", session.getId(), gameId);

        // Immediately tell this client the current game status
        // so it doesn't matter if Kafka fired before WS connected
        try {
            gameRepository.findByGameUuid(gameId).ifPresent(game -> {
                WsGameStateEvent event = new WsGameStateEvent();

                if ("WAITING".equals(game.getStatus())) {
                    event.setType("WAITING");
                } else if ("IN_PROGRESS".equals(game.getStatus())) {
                    event.setType("READY"); // second player just joined
                } else {
                    return; // FINISHED — do nothing
                }

                try {
                    String json = mapper.writeValueAsString(event);
                    session.sendMessage(new TextMessage(json));
                } catch (Exception e) {
                    log.error("Failed to send initial state", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to look up game on WS connect", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String gameId = extractGameId(session);
        Set<WebSocketSession> set = sessions.get(gameId);
        if (set != null) set.remove(session);
        log.info("WS disconnected: session={} gameId={}", session.getId(), gameId);
    }

    public void broadcast(String gameId, WsGameStateEvent event) {
        Set<WebSocketSession> set = sessions.get(gameId);
        if (set == null || set.isEmpty()) {
            log.warn("No WS sessions for gameId={}", gameId);
            return;
        }
        try {
            String json = mapper.writeValueAsString(event);
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession s : set) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(msg);
                    } catch (Exception e) {
                        log.error("Failed to send to session {}", s.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize WsGameStateEvent", e);
        }
    }

    private String extractGameId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        return path.substring(path.lastIndexOf('/') + 1);
    }
}