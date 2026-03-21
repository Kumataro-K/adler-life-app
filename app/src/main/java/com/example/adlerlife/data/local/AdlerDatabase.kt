package com.example.adlerlife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.adlerlife.data.model.ActionLogEntity
import com.example.adlerlife.data.model.ImpulseLogEntity
import com.example.adlerlife.data.model.ReflectionLogEntity

@Database(
    entities = [ImpulseLogEntity::class, ActionLogEntity::class, ReflectionLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
                ).build().also { instance = it }
            }
        }
    }
}
