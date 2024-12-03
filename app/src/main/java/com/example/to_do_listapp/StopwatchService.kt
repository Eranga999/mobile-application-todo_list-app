package com.example.to_do_listapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class StopwatchService : Service() {

    private var elapsedTime = 0
    private val handler = Handler()
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startForeground(1, createNotification())
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "stopwatch_channel",
                "Stopwatch Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, StopwatchActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, "stopwatch_channel")
            .setContentTitle("Stopwatch Running")
            .setContentText("Elapsed time: ${formatElapsedTime(elapsedTime)}")
            .setSmallIcon(R.drawable.ic_stopwatch)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun formatElapsedTime(elapsed: Int): String {
        val hours = (elapsed / 3600) % 24
        val minutes = (elapsed / 60) % 60
        val seconds = elapsed % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("StopwatchService", "Service started.")
        startTimer()
        return START_STICKY
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                elapsedTime++
                notificationManager.notify(1, createNotification())
                sendBroadcast(Intent("UPDATE_TIMER").putExtra("elapsedTime", elapsedTime))
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
