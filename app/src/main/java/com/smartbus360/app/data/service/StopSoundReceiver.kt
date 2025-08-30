package com.smartbus360.app.data.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartbus360.app.ui.screens.mediaPlayer

class StopSoundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Stop the MediaPlayer if it's playing
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Dismiss the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }
}