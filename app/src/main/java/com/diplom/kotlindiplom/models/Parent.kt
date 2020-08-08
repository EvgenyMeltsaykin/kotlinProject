package com.diplom.kotlindiplom.models

data class Parent (
    val parentUid:String,
    val username:String,
    val email:String,
    val profileImageUrl: String = "",
    val acceptAnswer: String = "",
    val city:String ="",
    val cityId : Int = -1)