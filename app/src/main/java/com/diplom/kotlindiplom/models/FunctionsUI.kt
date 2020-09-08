package com.diplom.kotlindiplom.models

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import java.util.Date.from

class FunctionsUI {
    val NOTIFY_ID = 101
    val CHANNEL_ID = "Task channel"

    fun createNotificationChannel(context:Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }
            val notificationManager:NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context:Context, title:String,text:String,contentIntent: PendingIntent){
        val builder = NotificationCompat.Builder(context,CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setWhen(System.currentTimeMillis())
            .setColor(context.resources.getColor(R.color.colorNotificationIco))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)){
            notify(NOTIFY_ID,builder.build())
        }
    }
    fun createNotificationChild(
        context: Context,
        intentClass: Class<*>,
        contentTitle: String,
        contentText: String,
        task: Task
    ) {
        val firebase = FunctionsFirebase()
        firebase.getFieldUserDatabase(
            firebase.uidUser!!,
            "parentUid",
            object : FirebaseCallback<String> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onComplete(value: String) {
                    if (task.parentUid == value) {
                        createNotificationChannel(context)
                        val intent = Intent(context, intentClass)
                        intent.putExtra("role","child")
                        intent.putExtra("taskId",task.taskId)
                        intent.putExtra("title",task.title)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                        sendNotification(context,contentTitle,contentText,pendingIntent)
                        firebase.setFieldDatabaseTask(task.taskId, "showNotification", 1)
                    }
                }
            })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationParent(
        context: Context,
        intentClass: Class<*>,
        contentTitle: String,
        contentText: String,
        task: Task
    ) {
        val firebase = FunctionsFirebase()
        if (task.parentUid != firebase.uidUser) return
        createNotificationChannel(context)

        val intent = Intent(context, intentClass)
        intent.putExtra("role","parent")
        intent.putExtra("taskId",task.taskId)
        intent.putExtra("title",task.title)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        sendNotification(context,contentTitle,contentText,pendingIntent)
        firebase.setFieldDatabaseTask(task.taskId, "showNotification", 1)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChildTakeAward(
        context: Context,
        intentClass: Class<*>,
        contentTitle: String,
        contentText: String,
        award: Award
    ){
        val firebase = FunctionsFirebase()
        if (award.parentUid != firebase.uidUser) return
        createNotificationChannel(context)
        val intent = Intent(context, intentClass)
        intent.putExtra("role","parent")
        intent.putExtra("awardId",award.awardId)
        intent.putExtra("nameAward",award.name)
        intent.putExtra("costAward",award.cost)
        intent.putExtra("status",1)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        sendNotification(context,contentTitle,contentText,pendingIntent)
        firebase.setFieldAward(award.awardId,"showNotification",0)

    }

}