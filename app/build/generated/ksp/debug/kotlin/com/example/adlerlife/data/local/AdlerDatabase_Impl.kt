package com.example.adlerlife.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AdlerDatabase_Impl : AdlerDatabase() {
  private val _adlerDao: Lazy<AdlerDao> = lazy {
    AdlerDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "23df151dba862c2d538cb0f68db985ab", "67d07733b56602f7d7831f523a45dd8a") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `impulse_logs` (`id` TEXT NOT NULL, `desire` TEXT NOT NULL, `mood` INTEGER NOT NULL, `energyLevel` INTEGER NOT NULL, `suggestion` TEXT NOT NULL, `createdAt` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `action_logs` (`id` TEXT NOT NULL, `action` TEXT NOT NULL, `feeling` TEXT NOT NULL, `category` TEXT NOT NULL, `createdAt` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reflection_logs` (`id` TEXT NOT NULL, `actionsSummary` TEXT NOT NULL, `memorableMoment` TEXT NOT NULL, `smallJoy` TEXT NOT NULL, `aiFeedback` TEXT NOT NULL, `createdAt` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '23df151dba862c2d538cb0f68db985ab')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `impulse_logs`")
        connection.execSQL("DROP TABLE IF EXISTS `action_logs`")
        connection.execSQL("DROP TABLE IF EXISTS `reflection_logs`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsImpulseLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsImpulseLogs.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsImpulseLogs.put("desire", TableInfo.Column("desire", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsImpulseLogs.put("mood", TableInfo.Column("mood", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsImpulseLogs.put("energyLevel", TableInfo.Column("energyLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsImpulseLogs.put("suggestion", TableInfo.Column("suggestion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsImpulseLogs.put("createdAt", TableInfo.Column("createdAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysImpulseLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesImpulseLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoImpulseLogs: TableInfo = TableInfo("impulse_logs", _columnsImpulseLogs, _foreignKeysImpulseLogs, _indicesImpulseLogs)
        val _existingImpulseLogs: TableInfo = read(connection, "impulse_logs")
        if (!_infoImpulseLogs.equals(_existingImpulseLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |impulse_logs(com.example.adlerlife.data.model.ImpulseLogEntity).
              | Expected:
              |""".trimMargin() + _infoImpulseLogs + """
              |
              | Found:
              |""".trimMargin() + _existingImpulseLogs)
        }
        val _columnsActionLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsActionLogs.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActionLogs.put("action", TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActionLogs.put("feeling", TableInfo.Column("feeling", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActionLogs.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsActionLogs.put("createdAt", TableInfo.Column("createdAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysActionLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesActionLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoActionLogs: TableInfo = TableInfo("action_logs", _columnsActionLogs, _foreignKeysActionLogs, _indicesActionLogs)
        val _existingActionLogs: TableInfo = read(connection, "action_logs")
        if (!_infoActionLogs.equals(_existingActionLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |action_logs(com.example.adlerlife.data.model.ActionLogEntity).
              | Expected:
              |""".trimMargin() + _infoActionLogs + """
              |
              | Found:
              |""".trimMargin() + _existingActionLogs)
        }
        val _columnsReflectionLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReflectionLogs.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReflectionLogs.put("actionsSummary", TableInfo.Column("actionsSummary", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReflectionLogs.put("memorableMoment", TableInfo.Column("memorableMoment", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReflectionLogs.put("smallJoy", TableInfo.Column("smallJoy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReflectionLogs.put("aiFeedback", TableInfo.Column("aiFeedback", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReflectionLogs.put("createdAt", TableInfo.Column("createdAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReflectionLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesReflectionLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoReflectionLogs: TableInfo = TableInfo("reflection_logs", _columnsReflectionLogs, _foreignKeysReflectionLogs, _indicesReflectionLogs)
        val _existingReflectionLogs: TableInfo = read(connection, "reflection_logs")
        if (!_infoReflectionLogs.equals(_existingReflectionLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reflection_logs(com.example.adlerlife.data.model.ReflectionLogEntity).
              | Expected:
              |""".trimMargin() + _infoReflectionLogs + """
              |
              | Found:
              |""".trimMargin() + _existingReflectionLogs)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "impulse_logs", "action_logs", "reflection_logs")
  }

  public override fun clearAllTables() {
    super.performClear(false, "impulse_logs", "action_logs", "reflection_logs")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AdlerDao::class, AdlerDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun adlerDao(): AdlerDao = _adlerDao.value
}
