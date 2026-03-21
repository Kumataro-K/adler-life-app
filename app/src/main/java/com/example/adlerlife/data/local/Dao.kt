package com.example.adlerlife.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.adlerlife.data.model.TraceLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AdlerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraceLog(log: TraceLog)

    @Query("SELECT * FROM trace_logs ORDER BY timestamp DESC")
    fun observeTraceLogs(): Flow<List<TraceLog>>

    @Query("SELECT * FROM trace_logs WHERE timestamp BETWEEN :startInclusive AND :endExclusive ORDER BY timestamp DESC")
    fun observeTraceLogsBetween(startInclusive: Long, endExclusive: Long): Flow<List<TraceLog>>

    @Query("SELECT * FROM trace_logs WHERE timestamp BETWEEN :startInclusive AND :endExclusive ORDER BY timestamp DESC")
    suspend fun getTraceLogsBetween(startInclusive: Long, endExclusive: Long): List<TraceLog>
}
