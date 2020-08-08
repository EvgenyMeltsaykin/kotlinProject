package com.diplom.kotlindiplom.models

data class Parent (
    var parentUid:String,
    var username:String,
    var email:String,
    var profileImageUrl: String = "",
    var acceptAnswer: String = "",
    var city:String ="",
    var cityId : Int = -1)