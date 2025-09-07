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
public class HealthExerciseSessionDao_Impl(
  __db: RoomDatabase,
) : HealthExerciseSessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHealthExerciseSessionEntity:
      EntityInsertAdapter<HealthExerciseSessionEntity>

  private val __updateAdapterOfHealthExerciseSessionEntity:
      EntityDeleteOrUpdateAdapter<HealthExerciseSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfHealthExerciseSessionEntity = object :
        EntityInsertAdapter<HealthExerciseSessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `health_connect_exercise_sessions` (`id`,`sessionId`,`date`,`startTime`,`endTime`,`durationMinutes`,`exerciseType`,`title`,`calories`,`avgHeartRate`,`maxHeartRate`,`distance`,`source`,`syncedAt`,`lastModified`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HealthExerciseSessionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.sessionId)
        statement.bindText(3, entity.date)
        statement.bindLong(4, entity.startTime)
        statement.bindLong(5, entity.endTime)
        statement.bindLong(6, entity.durationMinutes.toLong())
        statement.bindText(7, entity.exerciseType)
        statement.bindText(8, entity.title)
        val _tmpCalories: Double? = entity.calories
        if (_tmpCalories == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpCalories)
        }
        val _tmpAvgHeartRate: Int? = entity.avgHeartRate
        if (_tmpAvgHeartRate == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpAvgHeartRate.toLong())
        }
        val _tmpMaxHeartRate: Int? = entity.maxHeartRate
        if (_tmpMaxHeartRate == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpMaxHeartRate.toLong())
        }
        val _tmpDistance: Double? = entity.distance
        if (_tmpDistance == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpDistance)
        }
        statement.bindText(13, entity.source)
        statement.bindLong(14, entity.syncedAt)
        statement.bindLong(15, entity.lastModified)
      }
    }
    this.__updateAdapterOfHealthExerciseSessionEntity = object :
        EntityDeleteOrUpdateAdapter<HealthExerciseSessionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `health_connect_exercise_sessions` SET `id` = ?,`sessionId` = ?,`date` = ?,`startTime` = ?,`endTime` = ?,`durationMinutes` = ?,`exerciseType` = ?,`title` = ?,`calories` = ?,`avgHeartRate` = ?,`maxHeartRate` = ?,`distance` = ?,`source` = ?,`syncedAt` = ?,`lastModified` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HealthExerciseSessionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.sessionId)
        statement.bindText(3, entity.date)
        statement.bindLong(4, entity.startTime)
        statement.bindLong(5, entity.endTime)
        statement.bindLong(6, entity.durationMinutes.toLong())
        statement.bindText(7, entity.exerciseType)
        statement.bindText(8, entity.title)
        val _tmpCalories: Double? = entity.calories
        if (_tmpCalories == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpCalories)
        }
        val _tmpAvgHeartRate: Int? = entity.avgHeartRate
        if (_tmpAvgHeartRate == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpAvgHeartRate.toLong())
        }
        val _tmpMaxHeartRate: Int? = entity.maxHeartRate
        if (_tmpMaxHeartRate == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpMaxHeartRate.toLong())
        }
        val _tmpDistance: Double? = entity.distance
        if (_tmpDistance == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpDistance)
        }
        statement.bindText(13, entity.source)
        statement.bindLong(14, entity.syncedAt)
        statement.bindLong(15, entity.lastModified)
        statement.bindLong(16, entity.id)
      }
    }
  }

  public override suspend fun insert(session: HealthExerciseSessionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfHealthExerciseSessionEntity.insert(_connection, session)
  }

  public override suspend fun update(session: HealthExerciseSessionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfHealthExerciseSessionEntity.handle(_connection, session)
  }

  public override suspend fun getBySessionId(sessionId: String): HealthExerciseSessionEntity? {
    val _sql: String = "SELECT * FROM health_connect_exercise_sessions WHERE sessionId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfExerciseType: Int = getColumnIndexOrThrow(_stmt, "exerciseType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfAvgHeartRate: Int = getColumnIndexOrThrow(_stmt, "avgHeartRate")
        val _columnIndexOfMaxHeartRate: Int = getColumnIndexOrThrow(_stmt, "maxHeartRate")
        val _columnIndexOfDistance: Int = getColumnIndexOrThrow(_stmt, "distance")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: HealthExerciseSessionEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpExerciseType: String
          _tmpExerciseType = _stmt.getText(_columnIndexOfExerciseType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCalories: Double?
          if (_stmt.isNull(_columnIndexOfCalories)) {
            _tmpCalories = null
          } else {
            _tmpCalories = _stmt.getDouble(_columnIndexOfCalories)
          }
          val _tmpAvgHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAvgHeartRate)) {
            _tmpAvgHeartRate = null
          } else {
            _tmpAvgHeartRate = _stmt.getLong(_columnIndexOfAvgHeartRate).toInt()
          }
          val _tmpMaxHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfMaxHeartRate)) {
            _tmpMaxHeartRate = null
          } else {
            _tmpMaxHeartRate = _stmt.getLong(_columnIndexOfMaxHeartRate).toInt()
          }
          val _tmpDistance: Double?
          if (_stmt.isNull(_columnIndexOfDistance)) {
            _tmpDistance = null
          } else {
            _tmpDistance = _stmt.getDouble(_columnIndexOfDistance)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _result =
              HealthExerciseSessionEntity(_tmpId,_tmpSessionId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpDurationMinutes,_tmpExerciseType,_tmpTitle,_tmpCalories,_tmpAvgHeartRate,_tmpMaxHeartRate,_tmpDistance,_tmpSource,_tmpSyncedAt,_tmpLastModified)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDate(date: String): List<HealthExerciseSessionEntity> {
    val _sql: String =
        "SELECT * FROM health_connect_exercise_sessions WHERE date = ? ORDER BY startTime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfExerciseType: Int = getColumnIndexOrThrow(_stmt, "exerciseType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfAvgHeartRate: Int = getColumnIndexOrThrow(_stmt, "avgHeartRate")
        val _columnIndexOfMaxHeartRate: Int = getColumnIndexOrThrow(_stmt, "maxHeartRate")
        val _columnIndexOfDistance: Int = getColumnIndexOrThrow(_stmt, "distance")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthExerciseSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthExerciseSessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpExerciseType: String
          _tmpExerciseType = _stmt.getText(_columnIndexOfExerciseType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCalories: Double?
          if (_stmt.isNull(_columnIndexOfCalories)) {
            _tmpCalories = null
          } else {
            _tmpCalories = _stmt.getDouble(_columnIndexOfCalories)
          }
          val _tmpAvgHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAvgHeartRate)) {
            _tmpAvgHeartRate = null
          } else {
            _tmpAvgHeartRate = _stmt.getLong(_columnIndexOfAvgHeartRate).toInt()
          }
          val _tmpMaxHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfMaxHeartRate)) {
            _tmpMaxHeartRate = null
          } else {
            _tmpMaxHeartRate = _stmt.getLong(_columnIndexOfMaxHeartRate).toInt()
          }
          val _tmpDistance: Double?
          if (_stmt.isNull(_columnIndexOfDistance)) {
            _tmpDistance = null
          } else {
            _tmpDistance = _stmt.getDouble(_columnIndexOfDistance)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthExerciseSessionEntity(_tmpId,_tmpSessionId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpDurationMinutes,_tmpExerciseType,_tmpTitle,_tmpCalories,_tmpAvgHeartRate,_tmpMaxHeartRate,_tmpDistance,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startDate: String, endDate: String):
      List<HealthExerciseSessionEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM health_connect_exercise_sessions 
        |        WHERE date BETWEEN ? AND ? 
        |        ORDER BY startTime DESC
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
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfExerciseType: Int = getColumnIndexOrThrow(_stmt, "exerciseType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfAvgHeartRate: Int = getColumnIndexOrThrow(_stmt, "avgHeartRate")
        val _columnIndexOfMaxHeartRate: Int = getColumnIndexOrThrow(_stmt, "maxHeartRate")
        val _columnIndexOfDistance: Int = getColumnIndexOrThrow(_stmt, "distance")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthExerciseSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthExerciseSessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpExerciseType: String
          _tmpExerciseType = _stmt.getText(_columnIndexOfExerciseType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCalories: Double?
          if (_stmt.isNull(_columnIndexOfCalories)) {
            _tmpCalories = null
          } else {
            _tmpCalories = _stmt.getDouble(_columnIndexOfCalories)
          }
          val _tmpAvgHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAvgHeartRate)) {
            _tmpAvgHeartRate = null
          } else {
            _tmpAvgHeartRate = _stmt.getLong(_columnIndexOfAvgHeartRate).toInt()
          }
          val _tmpMaxHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfMaxHeartRate)) {
            _tmpMaxHeartRate = null
          } else {
            _tmpMaxHeartRate = _stmt.getLong(_columnIndexOfMaxHeartRate).toInt()
          }
          val _tmpDistance: Double?
          if (_stmt.isNull(_columnIndexOfDistance)) {
            _tmpDistance = null
          } else {
            _tmpDistance = _stmt.getDouble(_columnIndexOfDistance)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthExerciseSessionEntity(_tmpId,_tmpSessionId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpDurationMinutes,_tmpExerciseType,_tmpTitle,_tmpCalories,_tmpAvgHeartRate,_tmpMaxHeartRate,_tmpDistance,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByExerciseType(type: String): List<HealthExerciseSessionEntity> {
    val _sql: String =
        "SELECT * FROM health_connect_exercise_sessions WHERE exerciseType = ? ORDER BY startTime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfExerciseType: Int = getColumnIndexOrThrow(_stmt, "exerciseType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfAvgHeartRate: Int = getColumnIndexOrThrow(_stmt, "avgHeartRate")
        val _columnIndexOfMaxHeartRate: Int = getColumnIndexOrThrow(_stmt, "maxHeartRate")
        val _columnIndexOfDistance: Int = getColumnIndexOrThrow(_stmt, "distance")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSyncedAt: Int = getColumnIndexOrThrow(_stmt, "syncedAt")
        val _columnIndexOfLastModified: Int = getColumnIndexOrThrow(_stmt, "lastModified")
        val _result: MutableList<HealthExerciseSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthExerciseSessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpExerciseType: String
          _tmpExerciseType = _stmt.getText(_columnIndexOfExerciseType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCalories: Double?
          if (_stmt.isNull(_columnIndexOfCalories)) {
            _tmpCalories = null
          } else {
            _tmpCalories = _stmt.getDouble(_columnIndexOfCalories)
          }
          val _tmpAvgHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAvgHeartRate)) {
            _tmpAvgHeartRate = null
          } else {
            _tmpAvgHeartRate = _stmt.getLong(_columnIndexOfAvgHeartRate).toInt()
          }
          val _tmpMaxHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfMaxHeartRate)) {
            _tmpMaxHeartRate = null
          } else {
            _tmpMaxHeartRate = _stmt.getLong(_columnIndexOfMaxHeartRate).toInt()
          }
          val _tmpDistance: Double?
          if (_stmt.isNull(_columnIndexOfDistance)) {
            _tmpDistance = null
          } else {
            _tmpDistance = _stmt.getDouble(_columnIndexOfDistance)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSyncedAt: Long
          _tmpSyncedAt = _stmt.getLong(_columnIndexOfSyncedAt)
          val _tmpLastModified: Long
          _tmpLastModified = _stmt.getLong(_columnIndexOfLastModified)
          _item =
              HealthExerciseSessionEntity(_tmpId,_tmpSessionId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpDurationMinutes,_tmpExerciseType,_tmpTitle,_tmpCalories,_tmpAvgHeartRate,_tmpMaxHeartRate,_tmpDistance,_tmpSource,_tmpSyncedAt,_tmpLastModified)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalExerciseTimeForDate(date: String): Int? {
    val _sql: String =
        "SELECT SUM(durationMinutes) FROM health_connect_exercise_sessions WHERE date = ?"
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

  public override suspend fun getTotalCaloriesBurnedForDate(date: String): Double? {
    val _sql: String = "SELECT SUM(calories) FROM health_connect_exercise_sessions WHERE date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
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

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM health_connect_exercise_sessions WHERE id = ?"
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
    val _sql: String = "DELETE FROM health_connect_exercise_sessions WHERE syncedAt < ?"
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
    val _sql: String = "DELETE FROM health_connect_exercise_sessions"
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
