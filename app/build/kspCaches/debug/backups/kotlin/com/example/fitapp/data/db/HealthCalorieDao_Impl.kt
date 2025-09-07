package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
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
public class HealthCalorieDao_Impl(
  __db: RoomDatabase,
) : HealthCalorieDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHealthCalorieEntity: EntityInsertAdapter<HealthCalorieEntity>

  private val __updateAdapterOfHealthCalorieEntity: EntityDeleteOrUpdateAdapter<HealthCalorieEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfHealthCalorieEntity = object : EntityInsertAdapter<HealthCalorieEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `health_connect_calories` (`id`,`date`,`calories`,`calorieType`,`source`,`syncedAt`,`lastModified`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HealthCalorieEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindDouble(3, entity.calories)
        statement.bindText(4, entity.calorieType)
        statement.bindText(5, entity.source)
        statement.bindLong(6, entity.syncedAt)
        statement.bindLong(7, entity.lastModified)
      }
    }
    this.__updateAdapterOfHealthCalorieEntity = object :
        EntityDeleteOrUpdateAdapter<HealthCalorieEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `health_connect_calories` SET `id` = ?,`date` = ?,`calories` = ?,`calorieType` = ?,`source` = ?,`syncedAt` = ?,`lastModified` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HealthCalorieEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindDouble(3, entity.calories)
        statement.bindText(4, entity.calorieType)
        statement.bindText(5, entity.source)
        statement.bindLong(6, entity.syncedAt)
        statement.bindLong(7, entity.lastModified)
        statement.bindLong(8, entity.id)
      }
    }
  }

  public override suspend fun insert(calories: HealthCalorieEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfHealthCalorieEntity.insert(_connection, calories)
  }

  public override suspend fun update(calories: HealthCalorieEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfHealthCalorieEntity.handle(_connection, calories)
  }

  public override suspend fun getByDate(date: String): List<HealthCalorieEntity> {
    val _sql: String = "SELECT * FROM health_connect_calories WHERE date = ? ORDER BY syncedAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfCalorieType: Int = getColumnIndexOrThrow(_stmt, "calorieType")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthCalorieEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthCalorieEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpCalories: Double
          _tmpCalories = _stmt.getDouble(_columnIndexOfCalories)
          val _tmpCalorieType: String
          _tmpCalorieType = _stmt.getText(_columnIndexOfCalorieType)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthCalorieEntity(_tmpId,_tmpDate,_tmpCalories,_tmpCalorieType,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateTypeAndSource(
    date: String,
    type: String,
    source: String,
  ): HealthCalorieEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM health_connect_calories 
        |        WHERE date = ? AND calorieType = ? AND source = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        _argIndex = 3
        _stmt.bindText(_argIndex, source)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfCalorieType: Int = getColumnIndexOrThrow(_stmt, "calorieType")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: HealthCalorieEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpCalories: Double
          _tmpCalories = _stmt.getDouble(_columnIndexOfCalories)
          val _tmpCalorieType: String
          _tmpCalorieType = _stmt.getText(_columnIndexOfCalorieType)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _result =
              HealthCalorieEntity(_tmpId,_tmpDate,_tmpCalories,_tmpCalorieType,_tmpSource,_tmpSyncedAt,_tmpLastModified)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalCaloriesForDateAndType(date: String, type: String): Double? {
    val _sql: String =
        "SELECT SUM(calories) FROM health_connect_calories WHERE date = ? AND calorieType = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
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
      List<HealthCalorieEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM health_connect_calories 
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
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfCalorieType: Int = getColumnIndexOrThrow(_stmt, "calorieType")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthCalorieEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthCalorieEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpCalories: Double
          _tmpCalories = _stmt.getDouble(_columnIndexOfCalories)
          val _tmpCalorieType: String
          _tmpCalorieType = _stmt.getText(_columnIndexOfCalorieType)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthCalorieEntity(_tmpId,_tmpDate,_tmpCalories,_tmpCalorieType,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM health_connect_calories WHERE id = ?"
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
    val _sql: String = "DELETE FROM health_connect_calories WHERE syncedAt < ?"
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
    val _sql: String = "DELETE FROM health_connect_calories"
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
