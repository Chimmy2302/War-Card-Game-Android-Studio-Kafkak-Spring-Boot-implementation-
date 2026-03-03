package com.war.front.network.models

data class CreateGameResponse(
    val gameId: String,
    val playerId: String
)

data class JoinGameRequest(
    val gameId: String
)

data class JoinGameResponse(
    val gameId: String,
    val playerId: String
)

data class NextRoundRequest(
    val gameId: String,
    val playerId: String
)

data class SubmitWinnerRequest(
    val gameId: String,
    val winnerName: String
)

data class HistoryEntry(
    val id: Long,
    val winnerName: String,
    val wonAt: String
)

data class GameStateEvent(
    val type: String,
    val player1Card: String?,
    val player2Card: String?,
    val roundWinner: String?,
    val player1Score: Int,
    val player2Score: Int,
    val remainingCards: Int,
    val gameWinner: String?
)