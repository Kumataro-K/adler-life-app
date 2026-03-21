package com.example.adlerlife.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.adlerlife.data.model.ActionLogEntity
import com.example.adlerlife.data.model.ImpulseLogEntity
import com.example.adlerlife.data.model.ReflectionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdlerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImpulseLog(log: ImpulseLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionLog(log: ActionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflectionLog(log: ReflectionLogEntity)

    @Query("SELECT * FROM impulse_logs ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestImpulseLog(): Flow<ImpulseLogEntity?>

    @Query("SELECT * FROM action_logs ORDER BY createdAt DESC")
    fun observeActionLogs(): Flow<List<ActionLogEntity>>

    @Query("SELECT * FROM reflection_logs ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestReflection(): Flow<ReflectionLogEntity?>
}
