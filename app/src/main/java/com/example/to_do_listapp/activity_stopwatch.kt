package com.example.to_do_listapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class StopwatchActivity : AppCompatActivity() {

    private var elapsedTime = 0
    private var highScore = 0

    private lateinit var timerDisplay: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private lateinit var dailyScoreDisplay: TextView
    private lateinit var highScoreDisplay: TextView

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            elapsedTime = intent?.getIntExtra("elapsedTime", 0) ?: 0
            updateTimerDisplay(elapsedTime)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        timerDisplay = findViewById(R.id.timerDisplay)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)
        dailyScoreDisplay = findViewById(R.id.dailyScoreDisplay)
        highScoreDisplay = findViewById(R.id.highScoreDisplay)

        startButton.setOnClickListener { startTimer() }
        stopButton.setOnClickListener { stopTimer() }
        resetButton.setOnClickListener { resetTimer() }

        loadHighScore()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(timerReceiver, IntentFilter("UPDATE_TIMER"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(timerReceiver)
    }

    private fun startTimer() {
        Log.d("StopwatchActivity", "Start button clicked.")
        startService(Intent(this, StopwatchService::class.java))
        startButton.isEnabled = false
        stopButton.isEnabled = true
        resetButton.isEnabled = false
    }

    private fun stopTimer() {
        Log.d("StopwatchActivity", "Stop button clicked.")
        stopService(Intent(this, StopwatchService::class.java))
        startButton.isEnabled = true
        stopButton.isEnabled = false
        resetButton.isEnabled = true

        checkAndUpdateHighScore(elapsedTime) // Check for new high score
    }

    private fun resetTimer() {
        Log.d("StopwatchActivity", "Reset button clicked.")
        stopService(Intent(this, StopwatchService::class.java))
        updateTimerDisplay(0)
        startButton.isEnabled = true
        stopButton.isEnabled = false
        resetButton.isEnabled = false
    }

    private fun updateTimerDisplay(elapsedTime: Int) {
        val hours = (elapsedTime / 3600) % 24
        val minutes = (elapsedTime / 60) % 60
        val seconds = elapsedTime % 60
        timerDisplay.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun checkAndUpdateHighScore(currentScore: Int) {
        if (currentScore > highScore) {
            highScore = currentScore
            saveHighScore(highScore)
            showCongratulatoryMessage()
        }
        dailyScoreDisplay.text = currentScore.toString()
        highScoreDisplay.text = highScore.toString()
    }

    private fun saveHighScore(score: Int) {
        val sharedPreferences = getSharedPreferences("StopwatchPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("highScore", score)
            apply()
        }
    }

    private fun loadHighScore() {
        val sharedPreferences = getSharedPreferences("StopwatchPrefs", Context.MODE_PRIVATE)
        highScore = sharedPreferences.getInt("highScore", 0)
        highScoreDisplay.text = highScore.toString()
    }

    private fun showCongratulatoryMessage() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Congratulations!")
        dialogBuilder.setMessage("You've set a new high score!")
        dialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.show()
    }
}
