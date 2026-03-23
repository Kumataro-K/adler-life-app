package com.forestmood.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.forestmood.app.data.model.TraceLog

@Database(
    entities = [TraceLog::class],
    version = 4,
    exportSchema = false
)
abstract class AdlerDatabase : RoomDatabase() {
    abstract fun adlerDao(): AdlerDao

    companion object {
        @Volatile
        private var instance: AdlerDatabase? = null

        fun getInstance(context: Context): AdlerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AdlerDatabase::class.java,
                    "adler_life.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
