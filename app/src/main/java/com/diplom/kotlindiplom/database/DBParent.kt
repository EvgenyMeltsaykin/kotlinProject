package com.diplom.kotlindiplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DBParent (
    val username:String,
    val city:String =""
){
    @PrimaryKey(autoGenerate = true)
    var uid : Int = 0
}