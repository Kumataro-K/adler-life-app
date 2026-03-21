package com.example.adlerlife.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.adlerlife.`data`.model.ActionCategory
import com.example.adlerlife.`data`.model.ActionLogEntity
import com.example.adlerlife.`data`.model.ImpulseLogEntity
import com.example.adlerlife.`data`.model.ReflectionLogEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
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

  private val __insertAdapterOfImpulseLogEntity: EntityInsertAdapter<ImpulseLogEntity>

  private val __insertAdapterOfActionLogEntity: EntityInsertAdapter<ActionLogEntity>

  private val __converters: Converters = Converters()

  private val __insertAdapterOfReflectionLogEntity: EntityInsertAdapter<ReflectionLogEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfImpulseLogEntity = object : EntityInsertAdapter<ImpulseLogEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `impulse_logs` (`id`,`desire`,`mood`,`energyLevel`,`suggestion`,`createdAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ImpulseLogEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.desire)
        statement.bindLong(3, entity.mood.toLong())
        statement.bindLong(4, entity.energyLevel.toLong())
        statement.bindText(5, entity.suggestion)
        statement.bindText(6, entity.createdAt)
      }
    }
    this.__insertAdapterOfActionLogEntity = object : EntityInsertAdapter<ActionLogEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `action_logs` (`id`,`action`,`feeling`,`category`,`createdAt`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ActionLogEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.action)
        statement.bindText(3, entity.feeling)
        val _tmp: String = __converters.fromCategory(entity.category)
        statement.bindText(4, _tmp)
        statement.bindText(5, entity.createdAt)
      }
    }
    this.__insertAdapterOfReflectionLogEntity = object : EntityInsertAdapter<ReflectionLogEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `reflection_logs` (`id`,`actionsSummary`,`memorableMoment`,`smallJoy`,`aiFeedback`,`createdAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReflectionLogEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.actionsSummary)
        statement.bindText(3, entity.memorableMoment)
        statement.bindText(4, entity.smallJoy)
        statement.bindText(5, entity.aiFeedback)
        statement.bindText(6, entity.createdAt)
      }
    }
  }

  public override suspend fun insertImpulseLog(log: ImpulseLogEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfImpulseLogEntity.insert(_connection, log)
  }

  public override suspend fun insertActionLog(log: ActionLogEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfActionLogEntity.insert(_connection, log)
  }

  public override suspend fun insertReflectionLog(log: ReflectionLogEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfReflectionLogEntity.insert(_connection, log)
  }

  public override fun observeLatestImpulseLog(): Flow<ImpulseLogEntity?> {
    val _sql: String = "SELECT * FROM impulse_logs ORDER BY createdAt DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("impulse_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDesire: Int = getColumnIndexOrThrow(_stmt, "desire")
        val _columnIndexOfMood: Int = getColumnIndexOrThrow(_stmt, "mood")
        val _columnIndexOfEnergyLevel: Int = getColumnIndexOrThrow(_stmt, "energyLevel")
        val _columnIndexOfSuggestion: Int = getColumnIndexOrThrow(_stmt, "suggestion")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ImpulseLogEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpDesire: String
          _tmpDesire = _stmt.getText(_columnIndexOfDesire)
          val _tmpMood: Int
          _tmpMood = _stmt.getLong(_columnIndexOfMood).toInt()
          val _tmpEnergyLevel: Int
          _tmpEnergyLevel = _stmt.getLong(_columnIndexOfEnergyLevel).toInt()
          val _tmpSuggestion: String
          _tmpSuggestion = _stmt.getText(_columnIndexOfSuggestion)
          val _tmpCreatedAt: String
          _tmpCreatedAt = _stmt.getText(_columnIndexOfCreatedAt)
          _result = ImpulseLogEntity(_tmpId,_tmpDesire,_tmpMood,_tmpEnergyLevel,_tmpSuggestion,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeActionLogs(): Flow<List<ActionLogEntity>> {
    val _sql: String = "SELECT * FROM action_logs ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("action_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAction: Int = getColumnIndexOrThrow(_stmt, "action")
        val _columnIndexOfFeeling: Int = getColumnIndexOrThrow(_stmt, "feeling")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ActionLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ActionLogEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAction: String
          _tmpAction = _stmt.getText(_columnIndexOfAction)
          val _tmpFeeling: String
          _tmpFeeling = _stmt.getText(_columnIndexOfFeeling)
          val _tmpCategory: ActionCategory
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfCategory)
          _tmpCategory = __converters.toCategory(_tmp)
          val _tmpCreatedAt: String
          _tmpCreatedAt = _stmt.getText(_columnIndexOfCreatedAt)
          _item = ActionLogEntity(_tmpId,_tmpAction,_tmpFeeling,_tmpCategory,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeLatestReflection(): Flow<ReflectionLogEntity?> {
    val _sql: String = "SELECT * FROM reflection_logs ORDER BY createdAt DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("reflection_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfActionsSummary: Int = getColumnIndexOrThrow(_stmt, "actionsSummary")
        val _columnIndexOfMemorableMoment: Int = getColumnIndexOrThrow(_stmt, "memorableMoment")
        val _columnIndexOfSmallJoy: Int = getColumnIndexOrThrow(_stmt, "smallJoy")
        val _columnIndexOfAiFeedback: Int = getColumnIndexOrThrow(_stmt, "aiFeedback")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ReflectionLogEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpActionsSummary: String
          _tmpActionsSummary = _stmt.getText(_columnIndexOfActionsSummary)
          val _tmpMemorableMoment: String
          _tmpMemorableMoment = _stmt.getText(_columnIndexOfMemorableMoment)
          val _tmpSmallJoy: String
          _tmpSmallJoy = _stmt.getText(_columnIndexOfSmallJoy)
          val _tmpAiFeedback: String
          _tmpAiFeedback = _stmt.getText(_columnIndexOfAiFeedback)
          val _tmpCreatedAt: String
          _tmpCreatedAt = _stmt.getText(_columnIndexOfCreatedAt)
          _result = ReflectionLogEntity(_tmpId,_tmpActionsSummary,_tmpMemorableMoment,_tmpSmallJoy,_tmpAiFeedback,_tmpCreatedAt)
        } else {
          _result = null
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
