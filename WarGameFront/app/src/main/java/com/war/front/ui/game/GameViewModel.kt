package com.war.front.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.war.front.network.RetrofitClient
import com.war.front.network.WebSocketManager
import com.war.front.network.models.GameStateEvent
import com.war.front.network.models.JoinGameRequest
import com.war.front.network.models.NextRoundRequest
import com.war.front.network.models.SubmitWinnerRequest
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val api = RetrofitClient.api
    private val wsManager = WebSocketManager()

    var gameId: String = ""
    var playerId: String = ""

    private val _gameState = MutableLiveData<GameStateEvent>()
    val gameState: LiveData<GameStateEvent> = _gameState

    private val _uiMessage = MutableLiveData<String>()
    val uiMessage: LiveData<String> = _uiMessage

    private var wsStarted = false   // prevents duplicate collectors

    fun createGame() {
        viewModelScope.launch {
            try {
                _uiMessage.value = "Creating game..."
                val response = api.createGame()
                if (response.isSuccessful) {
                    val body = response.body()!!
                    gameId   = body.gameId
                    playerId = body.playerId

                    _uiMessage.value =
                        "Game ID:\n${body.gameId}\n\nShare this with your opponent"

                    startListeningToWebSocket()
                    wsManager.connect(gameId)

                } else {
                    _uiMessage.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun joinGame(gameIdInput: String) {
        viewModelScope.launch {
            try {
                _uiMessage.value = "Joining..."
                val response = api.joinGame(JoinGameRequest(gameIdInput))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    gameId   = body.gameId
                    playerId = body.playerId

                    _uiMessage.value = "Joined! Waiting for game to start..."

                    startListeningToWebSocket()
                    wsManager.connect(gameId)

                } else {
                    _uiMessage.value = "Join error: ${response.code()}"
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun startListeningToWebSocket() {
        if (wsStarted) return
        wsStarted = true

        viewModelScope.launch {
            wsManager.events.collect { event ->

                when (event.type) {

                    "WAITING" -> {
                        _gameState.postValue(event)
                        if (gameId.isNotEmpty()) {
                            _uiMessage.postValue(
                                "Waiting for opponent...\n\nGame ID:\n$gameId"
                            )
                        }
                    }

                    "READY" -> {
                        _gameState.postValue(event)
                        _uiMessage.postValue("Opponent joined! Game starting...")
                    }

                    "ROUND_RESULT" -> {
                        _gameState.postValue(event)
                    }

                    "GAME_OVER" -> {
                        _gameState.postValue(event)
                    }

                    else -> {
                        // Optional debug logging
                        // _uiMessage.postValue("Unknown event: ${event.type}")
                    }
                }
            }
        }
    }

    fun requestNextRound() {
        viewModelScope.launch {
            try {
                api.requestNextRound(NextRoundRequest(gameId, playerId))
            } catch (e: Exception) {
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun submitWinner(winnerName: String) {
        viewModelScope.launch {
            try {
                api.submitWinner(SubmitWinnerRequest(gameId, winnerName))
                _uiMessage.value = "Score saved!"
            } catch (e: Exception) {
                _uiMessage.value = "Error saving: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsManager.disconnect()
    }
}