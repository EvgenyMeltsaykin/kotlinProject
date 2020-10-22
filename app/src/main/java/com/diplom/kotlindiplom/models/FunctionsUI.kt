package com.diplom.kotlindiplom.models

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FunctionsUI {
    private val NOTIFY_ID = 101
    private val CHANNEL_ID = "Task channel"
    val APP_PREFERENCES_MODE = "onlyDiary"
    val APP_PREFERENCES_ROLE = "role"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        text: String,
        contentIntent: PendingIntent
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setWhen(System.currentTimeMillis())
            .setColor(context.resources.getColor(R.color.colorNotificationIco))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFY_ID, builder.build())
        }
    }

    fun createNotificationNewMessage(
        context: Context,
        intentClass: Class<*>,
        contentTitle: String,
        contentText: String,
        feedback: Feedback
    ) {
        createNotificationChannel(context)
        val intent = Intent(context, intentClass)
        val firebase = FunctionsFirebase()
        firebase.getRoleByUid(object :Callback<String>{
            override fun onComplete(value: String) {
                intent.putExtra("role", value)
                intent.putExtra("feedbackId", feedback.id)
                intent.putExtra("topic", feedback.topic)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                sendNotification(context, contentTitle, contentText, pendingIntent)
            }
        })

    }

}