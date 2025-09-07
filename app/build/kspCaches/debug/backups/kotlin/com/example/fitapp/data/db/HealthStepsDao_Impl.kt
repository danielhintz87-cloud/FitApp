package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class HealthStepsDao_Impl(
  __db: RoomDatabase,
) : HealthStepsDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHealthStepsEntity: EntityInsertAdapter<HealthStepsEntity>

  private val __updateAdapterOfHealthStepsEntity: EntityDeleteOrUpdateAdapter<HealthStepsEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfHealthStepsEntity = object : EntityInsertAdapter<HealthStepsEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `health_connect_steps` (`id`,`date`,`steps`,`source`,`syncedAt`,`lastModified`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HealthStepsEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindLong(3, entity.steps.toLong())
        statement.bindText(4, entity.source)
        statement.bindLong(5, entity.syncedAt)
        statement.bindLong(6, entity.lastModified)
      }
    }
    this.__updateAdapterOfHealthStepsEntity = object :
        EntityDeleteOrUpdateAdapter<HealthStepsEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `health_connect_steps` SET `id` = ?,`date` = ?,`steps` = ?,`source` = ?,`syncedAt` = ?,`lastModified` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HealthStepsEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindLong(3, entity.steps.toLong())
        statement.bindText(4, entity.source)
        statement.bindLong(5, entity.syncedAt)
        statement.bindLong(6, entity.lastModified)
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun insert(steps: HealthStepsEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfHealthStepsEntity.insert(_connection, steps)
  }

  public override suspend fun update(steps: HealthStepsEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfHealthStepsEntity.handle(_connection, steps)
  }

  public override suspend fun getByDate(date: String): List<HealthStepsEntity> {
    val _sql: String = "SELECT * FROM health_connect_steps WHERE date = ? ORDER BY syncedAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSteps: Int = getColumnIndexOrThrow(_stmt, "steps")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthStepsEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthStepsEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpSteps: Int
          _tmpSteps = _stmt.getLong(_columnIndexOfSteps).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthStepsEntity(_tmpId,_tmpDate,_tmpSteps,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateAndSource(date: String, source: String): HealthStepsEntity? {
    val _sql: String = "SELECT * FROM health_connect_steps WHERE date = ? AND source = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindText(_argIndex, source)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSteps: Int = getColumnIndexOrThrow(_stmt, "steps")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: HealthStepsEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpSteps: Int
          _tmpSteps = _stmt.getLong(_columnIndexOfSteps).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _result =
              HealthStepsEntity(_tmpId,_tmpDate,_tmpSteps,_tmpSource,_tmpSyncedAt,_tmpLastModified)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalStepsForDate(date: String): Int? {
    val _sql: String = "SELECT SUM(steps) FROM health_connect_steps WHERE date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startDate: String, endDate: String):
      List<HealthStepsEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM health_connect_steps 
        |        WHERE date BETWEEN ? AND ? 
        |        ORDER BY date DESC
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, startDate)
        _argIndex = 2
        _stmt.bindText(_argIndex, endDate)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSteps: Int = getColumnIndexOrThrow(_stmt, "steps")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthStepsEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthStepsEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpSteps: Int
          _tmpSteps = _stmt.getLong(_columnIndexOfSteps).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthStepsEntity(_tmpId,_tmpDate,_tmpSteps,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecent(limit: Int): List<HealthStepsEntity> {
    val _sql: String = "SELECT * FROM health_connect_steps ORDER BY date DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSteps: Int = getColumnIndexOrThrow(_stmt, "steps")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthStepsEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthStepsEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpSteps: Int
          _tmpSteps = _stmt.getLong(_columnIndexOfSteps).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthStepsEntity(_tmpId,_tmpDate,_tmpSteps,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM health_connect_steps WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOldEntries(beforeTimestamp: Long) {
    val _sql: String = "DELETE FROM health_connect_steps WHERE syncedAt < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, beforeTimestamp)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM health_connect_steps"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
