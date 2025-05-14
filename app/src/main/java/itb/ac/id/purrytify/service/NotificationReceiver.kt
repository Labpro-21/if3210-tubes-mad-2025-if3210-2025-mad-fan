package itb.ac.id.purrytify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        Log.d("NotificationReceiver", "Received action: ${intent.action}")

        val serviceIntent = Intent(context, NotificationService::class.java).apply {
            action = intent.action

            putExtras(intent)

            putExtra("timestamp", System.currentTimeMillis())
        }

        context.startService(serviceIntent)
    }
}