package com.war.front.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.war.front.R
import com.war.front.ui.game.GameActivity
import com.war.front.ui.history.HistoryActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStartGame).setOnClickListener {
            showJoinOrCreateDialog()
        }

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun showJoinOrCreateDialog() {
        val options = arrayOf("Create New Game", "Join Existing Game")
        AlertDialog.Builder(this)
            .setTitle("War Card Game")
            .setItems(options) { _, which ->
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra(
                    GameActivity.EXTRA_MODE,
                    if (which == 0) "CREATE" else "JOIN"
                )
                startActivity(intent)
            }
            .show()
    }
}