package com.diplom.kotlindiplom.models

class Child(
    val childUid: String,
    val username: String,
    val email: String,
    var profileImageUrl: String = "",
    var parentUid: String = "",
    var point: Int = 0,
    var city: String = "",
    var cityId: Int = -1,
    var educationalInstitution: String = "",
    var educationalInstitutionId: Int = -1,
    var id: Int = -1,
    var acceptName: String = "",
    var acceptUid: String? = "")

