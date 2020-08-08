package com.diplom.kotlindiplom.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChildDao {

    @Insert
    suspend fun addChild (child: DBChild)

    @Query("SELECT * FROM dbchild ORDER BY id DESC")
    suspend fun getAllChild() : List<DBChild>


}