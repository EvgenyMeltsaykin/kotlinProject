package com.diplom.kotlindiplom.models

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.diplom.kotlindiplom.ChooseActivity
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R

class FunctionsUI {
    fun createNotificationChild(
        context: Context,
        intentClass: Class<*>,
        smallIcon: Int,
        ticker: String,
        contentTitle: String,
        contentText: String,
        task: Task
    ) {
        val NOTIFY_ID = 101
        val CHANNEL_ID = "Task channel"
        val firebase = FunctionsFirebase()
        firebase.getFieldDatabaseChild(
            firebase.uidUser!!,
            "parentUid",
            object : FirebaseCallback {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onCallBackString(value: String) {
                    if (task.parentUid == value) {
                        val channel1 = NotificationChannel(
                            CHANNEL_ID,
                            "Task channel",
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        val nm = context.getSystemService(NotificationManager::class.java)
                        nm.createNotificationChannel(channel1)
                        val intent = Intent(context, intentClass)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                        val builder = Notification.Builder(context, CHANNEL_ID)
                        builder.setContentIntent(pendingIntent)
                            .setSmallIcon(smallIcon)
                            .setTicker(ticker)
                            .setContentTitle(contentTitle)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentText(contentText)
                        val notification = builder.build()
                        nm.notify(NOTIFY_ID, notification)
                        firebase.setFieldDatabaseTask(task.taskId, "showNotification", 1)
                    }
                }

                override fun onCallBackTasks(value: List<Task>) {
                    TODO("Not yet implemented")
                }

                override fun onCallBackTask(value: Task) {
                    TODO("Not yet implemented")
                }

            })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationParent(
        context: Context,
        intentClass: Class<*>,
        smallIcon: Int,
        ticker: String,
        contentTitle: String,
        contentText: String,
        task: Task
    ) {
        val NOTIFY_ID = 101
        val CHANNEL_ID = "Task channel"
        val firebase = FunctionsFirebase()
        if (task.parentUid != firebase.uidUser) return
        val channel1 = NotificationChannel(
            CHANNEL_ID,
            "Task channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel1)
        val intent = Intent(context, intentClass)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val builder = Notification.Builder(context, CHANNEL_ID)
        builder.setContentIntent(pendingIntent)
            .setSmallIcon(smallIcon)
            .setTicker(ticker)
            .setContentTitle(contentTitle)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentText(contentText)
        val notification = builder.build()
        nm.notify(NOTIFY_ID, notification)
        firebase.setFieldDatabaseTask(task.taskId, "showNotification", 1)
    }
}