package com.example.adlerlife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.adlerlife.data.model.TraceLog

@Database(
    entities = [TraceLog::class],
    version = 2,
    exportSchema = false
)
abstract class AdlerDatabase : RoomDatabase() {
    abstract fun adlerDao(): AdlerDao

    companion object {
        @Volatile
        private var instance: AdlerDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS trace_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        mood INTEGER NOT NULL,
                        energy INTEGER NOT NULL,
                        tags TEXT NOT NULL DEFAULT '',
                        feeling TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO trace_logs_new (id, mood, energy, tags, feeling, timestamp)
                    SELECT
                        id,
                        CASE
                            WHEN mood <= 1 THEN CAST(ROUND(mood * 100.0) AS INTEGER)
                            ELSE CAST(ROUND(mood) AS INTEGER)
                        END,
                        CASE
                            WHEN energy <= 1 THEN CAST(ROUND(energy * 100.0) AS INTEGER)
                            ELSE CAST(ROUND(energy) AS INTEGER)
                        END,
                        '',
                        feeling,
                        timestamp
                    FROM trace_logs
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE trace_logs")
                database.execSQL("ALTER TABLE trace_logs_new RENAME TO trace_logs")
            }
        }

        fun getInstance(context: Context): AdlerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AdlerDatabase::class.java,
                    "adler_life.db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
