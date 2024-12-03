
package com.example.to_do_listapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MarkDoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("taskName")

        Toast.makeText(context, "Task '$taskName' marked as done", Toast.LENGTH_SHORT).show()


    }
}