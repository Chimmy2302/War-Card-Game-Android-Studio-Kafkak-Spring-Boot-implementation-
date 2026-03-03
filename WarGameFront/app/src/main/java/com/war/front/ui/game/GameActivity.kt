package com.war.front.ui.game

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.war.front.R

class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
    }

    private val viewModel: GameViewModel by viewModels()

    private lateinit var tvStatus: TextView
    private lateinit var tvPlayer1Card: TextView
    private lateinit var tvPlayer2Card: TextView
    private lateinit var tvRoundResult: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnNextRound: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvStatus      = findViewById(R.id.tvStatus)
        tvPlayer1Card = findViewById(R.id.tvPlayer1Card)
        tvPlayer2Card = findViewById(R.id.tvPlayer2Card)
        tvRoundResult = findViewById(R.id.tvRoundResult)
        tvScore       = findViewById(R.id.tvScore)
        btnNextRound  = findViewById(R.id.btnNextRound)

        btnNextRound.isEnabled = false

        val mode = intent.getStringExtra(EXTRA_MODE) ?: "CREATE"  // default to CREATE if null

        tvStatus.text = "Starting $mode mode..."  // show something immediately

        when (mode) {
            "CREATE" -> viewModel.createGame()
            "JOIN"   -> promptForGameId()
            else     -> {
                tvStatus.text = "Unknown mode: $mode"
            }
        }

        observeViewModel()

        btnNextRound.setOnClickListener {
            viewModel.requestNextRound()
        }
    }

    private fun promptForGameId() {
        val input = EditText(this)
        input.hint = "Paste Game ID here"
        AlertDialog.Builder(this)
            .setTitle("Join Game")
            .setView(input)
            .setPositiveButton("Join") { _, _ ->
                val id = input.text.toString().trim()
                if (id.isNotEmpty()) viewModel.joinGame(id)
                else Toast.makeText(this, "Game ID cannot be empty", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun observeViewModel() {

        viewModel.uiMessage.observe(this) { msg ->
            tvStatus.text = msg
        }

        viewModel.gameState.observe(this) { event ->
            when (event.type) {

                "WAITING" -> {
                    tvStatus.text = "Waiting for opponent..."
                    btnNextRound.isEnabled = false
                }

                "READY" -> {
                    tvStatus.text = "Opponent joined! Game starting..."
                    btnNextRound.isEnabled = true
                }

                "ROUND_RESULT" -> {
                    btnNextRound.isEnabled = true
                    tvPlayer1Card.text = "P1: ${event.player1Card ?: "—"}"
                    tvPlayer2Card.text = "P2: ${event.player2Card ?: "—"}"
                    tvRoundResult.text = "Round Winner: ${event.roundWinner ?: "TIE"}"
                    tvScore.text =
                        "Score — P1: ${event.player1Score}  " +
                                "P2: ${event.player2Score}  " +
                                "Cards left: ${event.remainingCards}"
                }

                "GAME_OVER" -> {
                    btnNextRound.isEnabled = false
                    tvRoundResult.text = "🏆 GAME OVER! Winner: ${event.gameWinner}"
                    if (event.gameWinner == viewModel.playerId) {
                        promptForWinnerName()
                    }
                }
            }
        }
    }

    private fun promptForWinnerName() {
        val input = EditText(this)
        input.hint = "Your name"
        AlertDialog.Builder(this)
            .setTitle("🎉 You Won!")
            .setMessage("Enter your name for the Hall of Fame:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) viewModel.submitWinner(name)
            }
            .setCancelable(false)
            .show()
    }
}