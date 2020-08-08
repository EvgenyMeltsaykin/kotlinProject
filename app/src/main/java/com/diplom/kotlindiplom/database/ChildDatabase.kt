package com.diplom.kotlindiplom.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DBChild::class],
    version = 1
)
abstract class ChildDatabase : RoomDatabase() {

    abstract fun getChildDao(): ChildDao

    companion object{

       @Volatile private var instance : ChildDatabase? = null
        private  val LOCK = Any()

        operator  fun invoke (context: Context) = instance ?: synchronized(LOCK){
            instance?: buildDatebase(context).also {
                instance = it
            }
        }

        private fun buildDatebase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ChildDatabase::class.java,
            "childdatabase"
        ).build()

    }
}
