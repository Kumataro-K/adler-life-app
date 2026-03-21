package com.example.adlerlife.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.adlerlife.`data`.model.TraceLog
import javax.`annotation`.processing.Generated
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AdlerDao_Impl(
  __db: RoomDatabase,
) : AdlerDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTraceLog: EntityInsertAdapter<TraceLog>
  init {
    this.__db = __db
    this.__insertAdapterOfTraceLog = object : EntityInsertAdapter<TraceLog>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `trace_logs` (`id`,`mood`,`energy`,`whatHappened`,`feeling`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TraceLog) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindDouble(2, entity.mood.toDouble())
        statement.bindDouble(3, entity.energy.toDouble())
        statement.bindText(4, entity.whatHappened)
        statement.bindText(5, entity.feeling)
        statement.bindLong(6, entity.timestamp)
      }
    }
  }

  public override suspend fun insertTraceLog(log: TraceLog): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfTraceLog.insert(_connection, log)
  }

  public override fun observeTraceLogs(): Flow<List<TraceLog>> {
    val _sql: String = "SELECT * FROM trace_logs ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("trace_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMood: Int = getColumnIndexOrThrow(_stmt, "mood")
        val _columnIndexOfEnergy: Int = getColumnIndexOrThrow(_stmt, "energy")
        val _columnIndexOfWhatHappened: Int = getColumnIndexOrThrow(_stmt, "whatHappened")
        val _columnIndexOfFeeling: Int = getColumnIndexOrThrow(_stmt, "feeling")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<TraceLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: TraceLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMood: Float
          _tmpMood = _stmt.getDouble(_columnIndexOfMood).toFloat()
          val _tmpEnergy: Float
          _tmpEnergy = _stmt.getDouble(_columnIndexOfEnergy).toFloat()
          val _tmpWhatHappened: String
          _tmpWhatHappened = _stmt.getText(_columnIndexOfWhatHappened)
          val _tmpFeeling: String
          _tmpFeeling = _stmt.getText(_columnIndexOfFeeling)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = TraceLog(_tmpId,_tmpMood,_tmpEnergy,_tmpWhatHappened,_tmpFeeling,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeTraceLogsBetween(startInclusive: Long, endExclusive: Long):
      Flow<List<TraceLog>> {
    val _sql: String =
        "SELECT * FROM trace_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("trace_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startInclusive)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endExclusive)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMood: Int = getColumnIndexOrThrow(_stmt, "mood")
        val _columnIndexOfEnergy: Int = getColumnIndexOrThrow(_stmt, "energy")
        val _columnIndexOfWhatHappened: Int = getColumnIndexOrThrow(_stmt, "whatHappened")
        val _columnIndexOfFeeling: Int = getColumnIndexOrThrow(_stmt, "feeling")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<TraceLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: TraceLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMood: Float
          _tmpMood = _stmt.getDouble(_columnIndexOfMood).toFloat()
          val _tmpEnergy: Float
          _tmpEnergy = _stmt.getDouble(_columnIndexOfEnergy).toFloat()
          val _tmpWhatHappened: String
          _tmpWhatHappened = _stmt.getText(_columnIndexOfWhatHappened)
          val _tmpFeeling: String
          _tmpFeeling = _stmt.getText(_columnIndexOfFeeling)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = TraceLog(_tmpId,_tmpMood,_tmpEnergy,_tmpWhatHappened,_tmpFeeling,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTraceLogsBetween(startInclusive: Long, endExclusive: Long):
      List<TraceLog> {
    val _sql: String =
        "SELECT * FROM trace_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startInclusive)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endExclusive)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMood: Int = getColumnIndexOrThrow(_stmt, "mood")
        val _columnIndexOfEnergy: Int = getColumnIndexOrThrow(_stmt, "energy")
        val _columnIndexOfWhatHappened: Int = getColumnIndexOrThrow(_stmt, "whatHappened")
        val _columnIndexOfFeeling: Int = getColumnIndexOrThrow(_stmt, "feeling")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<TraceLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: TraceLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMood: Float
          _tmpMood = _stmt.getDouble(_columnIndexOfMood).toFloat()
          val _tmpEnergy: Float
          _tmpEnergy = _stmt.getDouble(_columnIndexOfEnergy).toFloat()
          val _tmpWhatHappened: String
          _tmpWhatHappened = _stmt.getText(_columnIndexOfWhatHappened)
          val _tmpFeeling: String
          _tmpFeeling = _stmt.getText(_columnIndexOfFeeling)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = TraceLog(_tmpId,_tmpMood,_tmpEnergy,_tmpWhatHappened,_tmpFeeling,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
