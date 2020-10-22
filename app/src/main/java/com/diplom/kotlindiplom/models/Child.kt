package com.diplom.kotlindiplom.models

import com.google.firebase.database.Exclude

data class Child(
    var userUid: String = "",
    var username: String = "",
    var email: String = "",
    var parentUid: String = "",
    var point: Int = 0,
    var city: String = "",
    var cityId: Int = -1,
    var educationalInstitution: String = "",
    var educationalInstitutionId: Int = -1,
    var id: Int = -1,
    var acceptName: String = "",
    var acceptUid: String? = "",
    val role: String = "child"
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userUid" to userUid,
            "username" to username,
            "email" to email,
            "parentUid" to parentUid,
            "point" to point,
            "city" to city,
            "cityId" to cityId,
            "educationalInstitution" to educationalInstitution,
            "educationalInstitutionId" to educationalInstitutionId,
            "id" to id,
            "acceptName" to acceptName,
            "acceptUid" to acceptUid,
            "role" to role
        )
    }

}
