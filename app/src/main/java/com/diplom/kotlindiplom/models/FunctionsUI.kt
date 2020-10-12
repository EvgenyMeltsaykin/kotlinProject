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
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.R
import com.google.android.material.navigation.NavigationView

class FunctionsUI {
    private val NOTIFY_ID = 101
    private val CHANNEL_ID = "Task channel"
    val APP_PREFERENCES_MODE = "onlyDiary"


    fun changeMode(activity: Activity){
        val prefs:SharedPreferences = activity.getSharedPreferences("settings",Context.MODE_PRIVATE)
        if(prefs.contains(APP_PREFERENCES_MODE)){
            val onlyDiary = prefs.getBoolean(APP_PREFERENCES_MODE,false)
            val navigationView: NavigationView = activity.findViewById(R.id.navView)
            val menuNavigationView = navigationView.menu
            menuNavigationView.findItem(R.id.childTasksFragment)?.isVisible = !onlyDiary
            menuNavigationView.findItem(R.id.listAwardsFragment)?.isVisible = !onlyDiary
            menuNavigationView.findItem(R.id.parentTasksFragment)?.isVisible = !onlyDiary
            menuNavigationView.findItem(R.id.listAwardsFragment)?.isVisible = !onlyDiary
            menuNavigationView.findItem(R.id.parentNodeChildrenFragment)?.isVisible = !onlyDiary
            activity.invalidateOptionsMenu()
        }
    }

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
            firebase.uidUser,
            "parentUid",
            object : Callback<String> {
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