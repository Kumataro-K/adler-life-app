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
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3,
        "604f623dbb43f767e36d8bdd6877efba", "c934c307c0304d6833a062b87aaa8a20") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `trace_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mood` REAL NOT NULL, `energy` REAL NOT NULL, `whatHappened` TEXT NOT NULL, `feeling` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '604f623dbb43f767e36d8bdd6877efba')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `trace_logs`")
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

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsTraceLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTraceLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTraceLogs.put("mood", TableInfo.Column("mood", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTraceLogs.put("energy", TableInfo.Column("energy", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTraceLogs.put("whatHappened", TableInfo.Column("whatHappened", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTraceLogs.put("feeling", TableInfo.Column("feeling", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTraceLogs.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTraceLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTraceLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTraceLogs: TableInfo = TableInfo("trace_logs", _columnsTraceLogs,
            _foreignKeysTraceLogs, _indicesTraceLogs)
        val _existingTraceLogs: TableInfo = read(connection, "trace_logs")
        if (!_infoTraceLogs.equals(_existingTraceLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |trace_logs(com.example.adlerlife.data.model.TraceLog).
              | Expected:
              |""".trimMargin() + _infoTraceLogs + """
              |
              | Found:
              |""".trimMargin() + _existingTraceLogs)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "trace_logs")
  }

  public override fun clearAllTables() {
    super.performClear(false, "trace_logs")
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

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun adlerDao(): AdlerDao = _adlerDao.value
}
