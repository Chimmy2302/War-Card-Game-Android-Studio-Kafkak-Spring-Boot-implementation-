package com.WarProject.usual.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsGameStateEvent {
    String type;
    String player1Card;
    String player2Card;
    String roundWinner;
    int    player1Score;
    int    player2Score;
    int    remainingCards;
    String gameWinner;
}