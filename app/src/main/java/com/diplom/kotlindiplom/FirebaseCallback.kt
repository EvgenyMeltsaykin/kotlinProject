package com.diplom.kotlindiplom

import com.diplom.kotlindiplom.models.Task

interface FirebaseCallback {
    fun onCallBackString(value: String)
    fun onCallBackTasks(value: List<Task>)
    fun onCallBackTask(value: Task)
}