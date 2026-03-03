package com.war.front.network

import com.google.gson.Gson
import com.war.front.network.models.GameStateEvent
import com.war.front.util.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketManager {

    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private var webSocket: WebSocket? = null

    private val _events = MutableSharedFlow<GameStateEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<GameStateEvent> = _events

    fun connect(gameId: String) {
        val request = Request.Builder()
            .url("${Constants.WS_URL}/$gameId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                android.util.Log.d("WS", "CONNECTED: ${response.message}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                android.util.Log.d("WS", "RECEIVED: $text")
                try {
                    val event = gson.fromJson(text, GameStateEvent::class.java)
                    _events.tryEmit(event)
                } catch (e: Exception) {
                    android.util.Log.e("WS", "PARSE ERROR: $text", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                android.util.Log.e("WS", "FAILURE: ${response?.message}", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // closed cleanly
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Game ended")
        webSocket = null
    }
}