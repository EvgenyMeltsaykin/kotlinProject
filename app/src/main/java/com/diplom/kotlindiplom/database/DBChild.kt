package com.diplom.kotlindiplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class DBChild (
    var username: String,
    var email: String,
    var point: Int = 0,
    var city: String = "",
    var educationalInstitution: String = "",
    var id: Int = -1
){
    @PrimaryKey(autoGenerate = true)
    var uid : Int = 0
}