package com.diplom.kotlindiplom.database

import androidx.room.*

@Dao
interface ChildParentDao {

    @Insert
    suspend fun addChild (child: DBChild)

    @Query("SELECT * FROM dbchild ORDER BY uid DESC")
    suspend fun getAllChild() : List<DBChild>

    @Update
    suspend fun updateChild(child:DBChild)

    @Delete
    suspend fun deleteChild(child:DBChild)

    @Insert
    suspend fun addParent(parent: DBParent)

    @Query("SELECT * FROM dbparent ORDER BY uid DESC")
    suspend fun getAllParent() : List<DBParent>

    @Update
    suspend fun updateParent(child:DBParent)



}