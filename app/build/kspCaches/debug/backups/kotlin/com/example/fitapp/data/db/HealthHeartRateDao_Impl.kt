package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class HealthHeartRateDao_Impl(
  __db: RoomDatabase,
) : HealthHeartRateDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHealthHeartRateEntity: EntityInsertAdapter<HealthHeartRateEntity>

  private val __updateAdapterOfHealthHeartRateEntity:
      EntityDeleteOrUpdateAdapter<HealthHeartRateEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfHealthHeartRateEntity = object :
        EntityInsertAdapter<HealthHeartRateEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `health_connect_heart_rate` (`id`,`timestamp`,`date`,`heartRate`,`source`,`syncedAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HealthHeartRateEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindText(3, entity.date)
        statement.bindLong(4, entity.heartRate.toLong())
        statement.bindText(5, entity.source)
        statement.bindLong(6, entity.syncedAt)
      }
    }
    this.__updateAdapterOfHealthHeartRateEntity = object :
        EntityDeleteOrUpdateAdapter<HealthHeartRateEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `health_connect_heart_rate` SET `id` = ?,`timestamp` = ?,`date` = ?,`heartRate` = ?,`source` = ?,`syncedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HealthHeartRateEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindText(3, entity.date)
        statement.bindLong(4, entity.heartRate.toLong())
        statement.bindText(5, entity.source)
        statement.bindLong(6, entity.syncedAt)
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun insert(heartRate: HealthHeartRateEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfHealthHeartRateEntity.insert(_connection, heartRate)
  }

  public override suspend fun update(heartRate: HealthHeartRateEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfHealthHeartRateEntity.handle(_connection, heartRate)
  }

  public override suspend fun getByDate(date: String): List<HealthHeartRateEntity> {
    val _sql: String =
        "SELECT * FROM health_connect_heart_rate WHERE date = ? ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfHeartRate: Int = getColumnIndexOrThrow(_stmt, "heartRate")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _result: MutableList<HealthHeartRateEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthHeartRateEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpHeartRate: Int
          _tmpHeartRate = _stmt.getLong(_columnIndexOfHeartRate).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          _item =
              HealthHeartRateEntity(_tmpId,_tmpTimestamp,_tmpDate,_tmpHeartRate,_tmpSource,_tmpSyncedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAverageHeartRateForDate(date: String): Float? {
    val _sql: String = "SELECT AVG(heartRate) FROM health_connect_heart_rate WHERE date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
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

  public override suspend fun getMaxHeartRateForDate(date: String): Int? {
    val _sql: String = "SELECT MAX(heartRate) FROM health_connect_heart_rate WHERE date = ?"
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

  public override suspend fun getByTimeRange(startTimestamp: Long, endTimestamp: Long):
      List<HealthHeartRateEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM health_connect_heart_rate 
        |        WHERE timestamp BETWEEN ? AND ? 
        |        ORDER BY timestamp
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
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfHeartRate: Int = getColumnIndexOrThrow(_stmt, "heartRate")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _result: MutableList<HealthHeartRateEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthHeartRateEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpHeartRate: Int
          _tmpHeartRate = _stmt.getLong(_columnIndexOfHeartRate).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          _item =
              HealthHeartRateEntity(_tmpId,_tmpTimestamp,_tmpDate,_tmpHeartRate,_tmpSource,_tmpSyncedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM health_connect_heart_rate WHERE id = ?"
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
    val _sql: String = "DELETE FROM health_connect_heart_rate WHERE syncedAt < ?"
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
    val _sql: String = "DELETE FROM health_connect_heart_rate"
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
