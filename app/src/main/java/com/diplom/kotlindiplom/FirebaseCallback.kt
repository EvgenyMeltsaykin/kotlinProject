package com.diplom.kotlindiplom

import com.diplom.kotlindiplom.models.Task

interface FirebaseCallback<T> {

    fun onComplete(value: T)
    //fun onCallBackTasks(value: List<Task>)
    //fun onCallBackTask(value: Task)
}