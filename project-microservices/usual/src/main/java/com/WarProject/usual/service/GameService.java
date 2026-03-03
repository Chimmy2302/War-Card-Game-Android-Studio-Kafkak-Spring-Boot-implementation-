package com.WarProject.usual.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.WarProject.usual.entity.GameEntity;
import com.WarProject.usual.model.GameEvent;
import com.WarProject.usual.model.WsGameStateEvent;
import com.WarProject.usual.repository.GameRepository;
import com.WarProject.usual.transformer.GameTransformer;
import com.WarProject.usual.websocket.GameWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository          gameRepository;
    private final GameTransformer         gameTransformer;
    private final GameWebSocketHandler    webSocketHandler;
    private final KafkaTemplate<String, GameEvent> kafkaTemplate;

    @Value("${war.kafka.topic}")
    private String topic;

    // one lock per gameUuid — prevents duplicate round processing
    private final ConcurrentHashMap<String, ReentrantLock> gameLocks = new ConcurrentHashMap<>();

    // ── REST entry points ────────────────────────────────────────────────────

    public String[] createGame() {
        String gameUuid = UUID.randomUUID().toString();
        String playerId = UUID.randomUUID().toString();

        List<String> deck     = buildShuffledDeck();
        String       deckJson = gameTransformer.deckToJson(deck);

        GameEntity entity = new GameEntity();
        entity.setGameUuid(gameUuid);
        entity.setPlayer1Id(playerId);
        entity.setDeck(deckJson);
        entity.setStatus("WAITING");
        entity.setP1Score(0);
        entity.setP2Score(0);
        gameRepository.save(entity);

        GameEvent event = new GameEvent();
        event.setType("GAME_CREATED");
        event.setGameId(gameUuid);
        event.setPlayerId(playerId);
        kafkaTemplate.send(topic, gameUuid, event);

        return new String[]{gameUuid, playerId};
    }

    @Transactional
    public String[] joinGame(String gameUuid) {
        GameEntity entity = gameRepository.findByGameUuid(gameUuid)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameUuid));

        if (!"WAITING".equals(entity.getStatus()))
            throw new IllegalStateException("Game is not waiting for players");
        if (entity.getPlayer2Id() != null)
            throw new IllegalStateException("Game already has 2 players");

        String playerId = UUID.randomUUID().toString();
        entity.setPlayer2Id(playerId);
        entity.setStatus("IN_PROGRESS");
        gameRepository.save(entity);

        GameEvent event = new GameEvent();
        event.setType("GAME_JOINED");
        event.setGameId(gameUuid);
        event.setPlayerId(playerId);
        kafkaTemplate.send(topic, gameUuid, event);

        return new String[]{gameUuid, playerId};
    }

    public void requestNextRound(String gameUuid, String playerId) {
        ReentrantLock lock = gameLocks.computeIfAbsent(gameUuid, k -> new ReentrantLock());

        if (!lock.tryLock()) {
            log.info("Round already processing for game {}, dropping duplicate", gameUuid);
            return;
        }
        try {
            GameEvent event = new GameEvent();
            event.setType("ROUND_REQUESTED");
            event.setGameId(gameUuid);
            event.setPlayerId(playerId);
            kafkaTemplate.send(topic, gameUuid, event);
        } finally {
            lock.unlock();
        }
    }

    // Kafka consumer callbacks ============================================

    public void onGameCreated(GameEvent event) {
        WsGameStateEvent ws = new WsGameStateEvent();
        ws.setType("WAITING");
        webSocketHandler.broadcast(event.getGameId(), ws);
    }

    public void onGameJoined(GameEvent event) {
        WsGameStateEvent ws = new WsGameStateEvent();
        ws.setType("READY");   //tell clients game is ready
        webSocketHandler.broadcast(event.getGameId(), ws);
    }

    @Transactional
    public void onRoundRequested(GameEvent event) {
        GameEntity entity = gameRepository.findByGameUuid(event.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + event.getGameId()));

        if (!"IN_PROGRESS".equals(entity.getStatus())) return;

        List<String> deck = gameTransformer.deckFromEntity(entity);
        if (deck.size() < 2) return;

        String p1Card = deck.remove(0);
        String p2Card = deck.remove(0);

        int    p1Val = cardValue(p1Card);
        int    p2Val = cardValue(p2Card);
        String roundWinner;

        if (p1Val > p2Val) {
            entity.setP1Score(entity.getP1Score() + 1);
            roundWinner = "player1";
        } else if (p2Val > p1Val) {
            entity.setP2Score(entity.getP2Score() + 1);
            roundWinner = "player2";
        } else {
            roundWinner = "TIE";
        }

        entity.setDeck(gameTransformer.deckToJson(deck));

        boolean gameOver  = deck.isEmpty();
        String  eventType = gameOver ? "GAME_OVER" : "ROUND_RESULT";

        if (gameOver) {
            entity.setStatus("FINISHED");
            String winnerId = entity.getP1Score() >= entity.getP2Score()
                    ? entity.getPlayer1Id()
                    : entity.getPlayer2Id();
            entity.setWinnerId(winnerId);
        }

        gameRepository.save(entity);

        GameEvent result = new GameEvent();
        result.setType(eventType);
        result.setGameId(event.getGameId());
        result.setPlayer1Card(p1Card);
        result.setPlayer2Card(p2Card);
        result.setRoundWinner(roundWinner);
        result.setPlayer1Score(entity.getP1Score());
        result.setPlayer2Score(entity.getP2Score());
        result.setRemainingCards(deck.size());
        result.setGameWinner(gameOver ? entity.getWinnerId() : null);
        kafkaTemplate.send(topic, event.getGameId(), result);
    }

    public void onRoundResult(GameEvent event) {
        WsGameStateEvent ws = new WsGameStateEvent();
        ws.setType("ROUND_RESULT");
        ws.setPlayer1Card(event.getPlayer1Card());
        ws.setPlayer2Card(event.getPlayer2Card());
        ws.setRoundWinner(event.getRoundWinner());
        ws.setPlayer1Score(event.getPlayer1Score());
        ws.setPlayer2Score(event.getPlayer2Score());
        ws.setRemainingCards(event.getRemainingCards());
        webSocketHandler.broadcast(event.getGameId(), ws);
    }

    public void onGameOver(GameEvent event) {
        WsGameStateEvent ws = new WsGameStateEvent();
        ws.setType("GAME_OVER");
        ws.setPlayer1Card(event.getPlayer1Card());
        ws.setPlayer2Card(event.getPlayer2Card());
        ws.setRoundWinner(event.getRoundWinner());
        ws.setPlayer1Score(event.getPlayer1Score());
        ws.setPlayer2Score(event.getPlayer2Score());
        ws.setRemainingCards(0);
        ws.setGameWinner(event.getGameWinner());
        webSocketHandler.broadcast(event.getGameId(), ws);
        gameLocks.remove(event.getGameId());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<String> buildShuffledDeck() {
        String[] suits = {"SPADES", "HEARTS", "DIAMONDS", "CLUBS"};
        String[] ranks = {"2","3","4","5","6","7","8","9","10","JACK","QUEEN","KING","ACE"};
        List<String> deck = new ArrayList<>();
        for (String suit : suits)
            for (String rank : ranks)
                deck.add(rank + "_" + suit);
        Collections.shuffle(deck);
        return deck;
    }

    private int cardValue(String card) {
        String rank = card.contains("_") ? card.substring(0, card.indexOf('_')) : card;
        switch (rank) {
            case "2":  return 2;   case "3":  return 3;
            case "4":  return 4;   case "5":  return 5;
            case "6":  return 6;   case "7":  return 7;
            case "8":  return 8;   case "9":  return 9;
            case "10": return 10;  case "JACK":  return 11;
            case "QUEEN": return 12; case "KING": return 13;
            case "ACE":   return 14; default:     return 0;
        }
    }
}