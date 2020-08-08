package com.diplom.kotlindiplom.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DBChild::class, DBParent::class],
    version = 2
)
abstract class ChildParentDatabase : RoomDatabase() {

    abstract fun getChildParentDao(): ChildParentDao

    companion object{

       @Volatile private var instance : ChildParentDatabase? = null
        private  val LOCK = Any()

        operator  fun invoke (context: Context) = instance ?: synchronized(LOCK){
            instance?: buildDatebase(context).also {
                instance = it
            }
        }

        private fun buildDatebase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ChildParentDatabase::class.java,
            "childparentdatabase"
        ).fallbackToDestructiveMigration()
            .build()

    }

}
