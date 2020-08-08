package com.diplom.kotlindiplom

interface FirebaseCallback<T> {

    fun onComplete(value: T)
    //fun onCallBackTasks(value: List<Task>)
    //fun onCallBackTask(value: Task)
}