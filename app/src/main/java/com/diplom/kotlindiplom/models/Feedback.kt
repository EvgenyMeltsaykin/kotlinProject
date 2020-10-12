package com.diplom.kotlindiplom.models

data class Feedback(
    var id :String = "",
    var topic:String = "",
    var status:Int = -1,
    var codeQuestion:Int = 0,
    var messages:List<MessageFeedback> = listOf()
)