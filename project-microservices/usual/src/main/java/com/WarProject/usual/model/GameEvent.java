package com.WarProject.usual.model;

import lombok.Data;

@Data
public class GameEvent {
    String type;            // GAME_CREATED | GAME_JOINED | ROUND_REQUESTED | ROUND_RESULT | GAME_OVER
    String gameId;          // gameUuid
    String playerId;
    String player1Card;
    String player2Card;
    String roundWinner;     // "player1" | "player2" | "TIE"
    int    player1Score;
    int    player2Score;
    int    remainingCards;
    String gameWinner;      // playerId of overall winner, null if ongoing
}