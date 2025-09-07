package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WaterEntryDao_Impl(
  __db: RoomDatabase,
) : WaterEntryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWaterEntryEntity: EntityInsertAdapter<WaterEntryEntity>

  private val __updateAdapterOfWaterEntryEntity: EntityDeleteOrUpdateAdapter<WaterEntryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWaterEntryEntity = object : EntityInsertAdapter<WaterEntryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `water_entries` (`id`,`date`,`amountMl`,`timestamp`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WaterEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindLong(3, entity.amountMl.toLong())
        statement.bindLong(4, entity.timestamp)
      }
    }
    this.__updateAdapterOfWaterEntryEntity = object :
        EntityDeleteOrUpdateAdapter<WaterEntryEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `water_entries` SET `id` = ?,`date` = ?,`amountMl` = ?,`timestamp` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WaterEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindLong(3, entity.amountMl.toLong())
        statement.bindLong(4, entity.timestamp)
        statement.bindLong(5, entity.id)
      }
    }
  }

  public override suspend fun insert(waterEntry: WaterEntryEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfWaterEntryEntity.insertAndReturnId(_connection, waterEntry)
    _result
  }

  public override suspend fun update(waterEntry: WaterEntryEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfWaterEntryEntity.handle(_connection, waterEntry)
  }

  public override suspend fun getByDate(date: String): List<WaterEntryEntity> {
    val _sql: String = "SELECT * FROM water_entries WHERE date = ? ORDER BY timestamp"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfAmountMl: Int = getColumnIndexOrThrow(_stmt, "amountMl")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<WaterEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WaterEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpAmountMl: Int
          _tmpAmountMl = _stmt.getLong(_columnIndexOfAmountMl).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = WaterEntryEntity(_tmpId,_tmpDate,_tmpAmountMl,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getByDateFlow(date: String): Flow<List<WaterEntryEntity>> {
    val _sql: String = "SELECT * FROM water_entries WHERE date = ? ORDER BY timestamp"
    return createFlow(__db, false, arrayOf("water_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfAmountMl: Int = getColumnIndexOrThrow(_stmt, "amountMl")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<WaterEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WaterEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpAmountMl: Int
          _tmpAmountMl = _stmt.getLong(_columnIndexOfAmountMl).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = WaterEntryEntity(_tmpId,_tmpDate,_tmpAmountMl,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalWaterForDate(date: String): Int {
    val _sql: String = "SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalWaterForDateFlow(date: String): Flow<Int> {
    val _sql: String = "SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE date = ?"
    return createFlow(__db, false, arrayOf("water_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM water_entries WHERE id = ?"
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

  public override suspend fun clearForDate(date: String) {
    val _sql: String = "DELETE FROM water_entries WHERE date = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM water_entries"
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
