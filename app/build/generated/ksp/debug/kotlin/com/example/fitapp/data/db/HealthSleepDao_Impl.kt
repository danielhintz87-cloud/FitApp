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
public class HealthSleepDao_Impl(
  __db: RoomDatabase,
) : HealthSleepDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHealthSleepEntity: EntityInsertAdapter<HealthSleepEntity>

  private val __updateAdapterOfHealthSleepEntity: EntityDeleteOrUpdateAdapter<HealthSleepEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfHealthSleepEntity = object : EntityInsertAdapter<HealthSleepEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `health_connect_sleep` (`id`,`date`,`startTime`,`endTime`,`durationMinutes`,`sleepStage`,`source`,`syncedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HealthSleepEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindLong(3, entity.startTime)
        statement.bindLong(4, entity.endTime)
        statement.bindLong(5, entity.durationMinutes.toLong())
        statement.bindText(6, entity.sleepStage)
        statement.bindText(7, entity.source)
        statement.bindLong(8, entity.syncedAt)
      }
    }
    this.__updateAdapterOfHealthSleepEntity = object :
        EntityDeleteOrUpdateAdapter<HealthSleepEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `health_connect_sleep` SET `id` = ?,`date` = ?,`startTime` = ?,`endTime` = ?,`durationMinutes` = ?,`sleepStage` = ?,`source` = ?,`syncedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HealthSleepEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindLong(3, entity.startTime)
        statement.bindLong(4, entity.endTime)
        statement.bindLong(5, entity.durationMinutes.toLong())
        statement.bindText(6, entity.sleepStage)
        statement.bindText(7, entity.source)
        statement.bindLong(8, entity.syncedAt)
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insert(sleep: HealthSleepEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfHealthSleepEntity.insert(_connection, sleep)
  }

  public override suspend fun update(sleep: HealthSleepEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfHealthSleepEntity.handle(_connection, sleep)
  }

  public override suspend fun getByDate(date: String): List<HealthSleepEntity> {
    val _sql: String = "SELECT * FROM health_connect_sleep WHERE date = ? ORDER BY startTime"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfSleepStage: Int = getColumnIndexOrThrow(_stmt, "sleepStage")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _result: MutableList<HealthSleepEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthSleepEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpSleepStage: String
          _tmpSleepStage = _stmt.getText(_columnIndexOfSleepStage)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          _item =
              HealthSleepEntity(_tmpId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpDurationMinutes,_tmpSleepStage,_tmpSource,_tmpSyncedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalSleepForDate(date: String): Int? {
    val _sql: String = "SELECT SUM(durationMinutes) FROM health_connect_sleep WHERE date = ?"
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

  public override suspend fun getSleepDurationByStage(date: String, stage: String): Int? {
    val _sql: String = """
        |
        |        SELECT SUM(durationMinutes) FROM health_connect_sleep 
        |        WHERE date = ? AND sleepStage = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindText(_argIndex, stage)
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

  public override suspend fun getByTimeRange(startTimestamp: Long, endTimestamp: Long):
      List<HealthSleepEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM health_connect_sleep 
        |        WHERE startTime BETWEEN ? AND ? 
        |        ORDER BY startTime
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startTimestamp)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endTimestamp)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfSleepStage: Int = getColumnIndexOrThrow(_stmt, "sleepStage")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _result: MutableList<HealthSleepEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthSleepEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpSleepStage: String
          _tmpSleepStage = _stmt.getText(_columnIndexOfSleepStage)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          _item =
              HealthSleepEntity(_tmpId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpDurationMinutes,_tmpSleepStage,_tmpSource,_tmpSyncedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM health_connect_sleep WHERE id = ?"
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
    val _sql: String = "DELETE FROM health_connect_sleep WHERE syncedAt < ?"
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
    val _sql: String = "DELETE FROM health_connect_sleep"
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
