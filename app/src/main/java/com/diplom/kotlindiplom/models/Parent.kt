package com.diplom.kotlindiplom.models

data class Parent (
    var userUid:String = "",
    var username:String = "",
    var email:String = "",
    var acceptAnswer: String = "",
    var city:String ="",
    var cityId : Int = -1,
    val role: String = "parent"
)