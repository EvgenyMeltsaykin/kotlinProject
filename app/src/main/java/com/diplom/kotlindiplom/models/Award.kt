package com.diplom.kotlindiplom.models

data class Award (
    var awardId :String ="",
    var childUid:String = "",
    var cost :String = "",
    var name:String = "",
    var parentUid :String = "",
    var status:Int = 0,
    var showNotification : Int = 0
)