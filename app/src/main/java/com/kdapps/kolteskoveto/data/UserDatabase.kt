package com.kdapps.kolteskoveto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SpendNode::class, ArchiveNode::class], version = 1)

abstract class UserDatabase : RoomDatabase() {

    abstract fun databaseDao(): DatabaseDao

    companion object {
        private var instance: UserDatabase? = null

        @Synchronized
        fun getDatabase(ctx: Context): UserDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, UserDatabase::class.java,
                    "user-database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            return instance!!
        }
    }
}