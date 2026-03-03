package com.war.front.network

import com.war.front.network.models.CreateGameResponse
import com.war.front.network.models.HistoryEntry
import com.war.front.network.models.JoinGameRequest
import com.war.front.network.models.JoinGameResponse
import com.war.front.network.models.NextRoundRequest
import com.war.front.network.models.SubmitWinnerRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/games")
    suspend fun createGame(): Response<CreateGameResponse>

    @POST("api/games/join")
    suspend fun joinGame(@Body request: JoinGameRequest): Response<JoinGameResponse>

    @POST("api/games/next-round")
    suspend fun requestNextRound(@Body request: NextRoundRequest): Response<Unit>

    @POST("api/games/winner")
    suspend fun submitWinner(@Body request: SubmitWinnerRequest): Response<Unit>

    @GET("api/history")
    suspend fun getHistory(): Response<List<HistoryEntry>>
}