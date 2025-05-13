package itb.ac.id.purrytify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val serviceIntent = Intent(context, NotificationService::class.java).apply {
            action = intent.action
        }
        context.startService(serviceIntent)
    }
}