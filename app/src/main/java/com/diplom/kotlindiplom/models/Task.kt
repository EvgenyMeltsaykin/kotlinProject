package com.diplom.kotlindiplom.models
data class Task(
    var taskId:String,
    var title:String,
    var description:String,
    var cost: Int,
    var parentUid:String,
    var time:String,
    var status: Int = -1,
    var childUid : String = "",
    var showNotification : Int = 0)

