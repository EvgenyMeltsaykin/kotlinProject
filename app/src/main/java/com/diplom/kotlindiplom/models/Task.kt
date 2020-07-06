package com.diplom.kotlindiplom.models

import java.sql.Time

class Task(
    val taskId:String,
    val title:String,
    val description:String,
    val cost: Int,
    val parentUid:String,
    val time:String,
    var status: Int = -1,
    var childUid : String = "",
    var showNotification : Int = 0)

