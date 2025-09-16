package com.example.lilifly

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

const val notoficationID =1
const val channelID ="channel 1"
const val titleExtra ="title 1"
const val messageExtra ="message 1"




class Notification: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val notification: Notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.vav9) // Required
            .setContentTitle("title")
            .setContentText(intent?.getStringExtra(titleExtra))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)


    }
}