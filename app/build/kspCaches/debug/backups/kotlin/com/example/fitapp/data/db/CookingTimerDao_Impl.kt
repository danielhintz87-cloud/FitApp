package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class CookingTimerDao_Impl(
  __db: RoomDatabase,
) : CookingTimerDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCookingTimerEntity: EntityInsertAdapter<CookingTimerEntity>

  private val __updateAdapterOfCookingTimerEntity: EntityDeleteOrUpdateAdapter<CookingTimerEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCookingTimerEntity = object : EntityInsertAdapter<CookingTimerEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `cooking_timers` (`id`,`sessionId`,`stepIndex`,`name`,`durationSeconds`,`remainingSeconds`,`isActive`,`isPaused`,`startTime`,`completedAt`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CookingTimerEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        statement.bindLong(3, entity.stepIndex.toLong())
        statement.bindText(4, entity.name)
        statement.bindLong(5, entity.durationSeconds)
        statement.bindLong(6, entity.remainingSeconds)
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmp_1: Int = if (entity.isPaused) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        val _tmpStartTime: Long? = entity.startTime
        if (_tmpStartTime == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpStartTime)
        }
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAt)
        }
        statement.bindLong(11, entity.createdAt)
      }
    }
    this.__updateAdapterOfCookingTimerEntity = object :
        EntityDeleteOrUpdateAdapter<CookingTimerEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `cooking_timers` SET `id` = ?,`sessionId` = ?,`stepIndex` = ?,`name` = ?,`durationSeconds` = ?,`remainingSeconds` = ?,`isActive` = ?,`isPaused` = ?,`startTime` = ?,`completedAt` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CookingTimerEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        statement.bindLong(3, entity.stepIndex.toLong())
        statement.bindText(4, entity.name)
        statement.bindLong(5, entity.durationSeconds)
        statement.bindLong(6, entity.remainingSeconds)
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmp_1: Int = if (entity.isPaused) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        val _tmpStartTime: Long? = entity.startTime
        if (_tmpStartTime == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpStartTime)
        }
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAt)
        }
        statement.bindLong(11, entity.createdAt)
        statement.bindText(12, entity.id)
      }
    }
  }

  public override suspend fun insert(timer: CookingTimerEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfCookingTimerEntity.insert(_connection, timer)
  }

  public override suspend fun update(timer: CookingTimerEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfCookingTimerEntity.handle(_connection, timer)
  }

  public override suspend fun getTimerById(id: String): CookingTimerEntity? {
    val _sql: String = "SELECT * FROM cooking_timers WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfStepIndex: Int = getColumnIndexOrThrow(_stmt, "stepIndex")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "durationSeconds")
        val _columnIndexOfRemainingSeconds: Int = getColumnIndexOrThrow(_stmt, "remainingSeconds")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfIsPaused: Int = getColumnIndexOrThrow(_stmt, "isPaused")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: CookingTimerEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpStepIndex: Int
          _tmpStepIndex = _stmt.getLong(_columnIndexOfStepIndex).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationSeconds: Long
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          val _tmpRemainingSeconds: Long
          _tmpRemainingSeconds = _stmt.getLong(_columnIndexOfRemainingSeconds)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpIsPaused: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPaused).toInt()
          _tmpIsPaused = _tmp_1 != 0
          val _tmpStartTime: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              CookingTimerEntity(_tmpId,_tmpSessionId,_tmpStepIndex,_tmpName,_tmpDurationSeconds,_tmpRemainingSeconds,_tmpIsActive,_tmpIsPaused,_tmpStartTime,_tmpCompletedAt,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBySessionId(sessionId: String): List<CookingTimerEntity> {
    val _sql: String = "SELECT * FROM cooking_timers WHERE sessionId = ? ORDER BY stepIndex"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfStepIndex: Int = getColumnIndexOrThrow(_stmt, "stepIndex")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "durationSeconds")
        val _columnIndexOfRemainingSeconds: Int = getColumnIndexOrThrow(_stmt, "remainingSeconds")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfIsPaused: Int = getColumnIndexOrThrow(_stmt, "isPaused")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CookingTimerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingTimerEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpStepIndex: Int
          _tmpStepIndex = _stmt.getLong(_columnIndexOfStepIndex).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationSeconds: Long
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          val _tmpRemainingSeconds: Long
          _tmpRemainingSeconds = _stmt.getLong(_columnIndexOfRemainingSeconds)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpIsPaused: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPaused).toInt()
          _tmpIsPaused = _tmp_1 != 0
          val _tmpStartTime: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingTimerEntity(_tmpId,_tmpSessionId,_tmpStepIndex,_tmpName,_tmpDurationSeconds,_tmpRemainingSeconds,_tmpIsActive,_tmpIsPaused,_tmpStartTime,_tmpCompletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getBySessionIdFlow(sessionId: String): Flow<List<CookingTimerEntity>> {
    val _sql: String = "SELECT * FROM cooking_timers WHERE sessionId = ? ORDER BY stepIndex"
    return createFlow(__db, false, arrayOf("cooking_timers")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfStepIndex: Int = getColumnIndexOrThrow(_stmt, "stepIndex")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "durationSeconds")
        val _columnIndexOfRemainingSeconds: Int = getColumnIndexOrThrow(_stmt, "remainingSeconds")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfIsPaused: Int = getColumnIndexOrThrow(_stmt, "isPaused")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CookingTimerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingTimerEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpStepIndex: Int
          _tmpStepIndex = _stmt.getLong(_columnIndexOfStepIndex).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationSeconds: Long
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          val _tmpRemainingSeconds: Long
          _tmpRemainingSeconds = _stmt.getLong(_columnIndexOfRemainingSeconds)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpIsPaused: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPaused).toInt()
          _tmpIsPaused = _tmp_1 != 0
          val _tmpStartTime: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingTimerEntity(_tmpId,_tmpSessionId,_tmpStepIndex,_tmpName,_tmpDurationSeconds,_tmpRemainingSeconds,_tmpIsActive,_tmpIsPaused,_tmpStartTime,_tmpCompletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByStepIndex(sessionId: String, stepIndex: Int):
      List<CookingTimerEntity> {
    val _sql: String = "SELECT * FROM cooking_timers WHERE stepIndex = ? AND sessionId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, stepIndex.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfStepIndex: Int = getColumnIndexOrThrow(_stmt, "stepIndex")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "durationSeconds")
        val _columnIndexOfRemainingSeconds: Int = getColumnIndexOrThrow(_stmt, "remainingSeconds")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfIsPaused: Int = getColumnIndexOrThrow(_stmt, "isPaused")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CookingTimerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingTimerEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpStepIndex: Int
          _tmpStepIndex = _stmt.getLong(_columnIndexOfStepIndex).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationSeconds: Long
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          val _tmpRemainingSeconds: Long
          _tmpRemainingSeconds = _stmt.getLong(_columnIndexOfRemainingSeconds)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpIsPaused: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPaused).toInt()
          _tmpIsPaused = _tmp_1 != 0
          val _tmpStartTime: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingTimerEntity(_tmpId,_tmpSessionId,_tmpStepIndex,_tmpName,_tmpDurationSeconds,_tmpRemainingSeconds,_tmpIsActive,_tmpIsPaused,_tmpStartTime,_tmpCompletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveTimers(): List<CookingTimerEntity> {
    val _sql: String =
        "SELECT `cooking_timers`.`id` AS `id`, `cooking_timers`.`sessionId` AS `sessionId`, `cooking_timers`.`stepIndex` AS `stepIndex`, `cooking_timers`.`name` AS `name`, `cooking_timers`.`durationSeconds` AS `durationSeconds`, `cooking_timers`.`remainingSeconds` AS `remainingSeconds`, `cooking_timers`.`isActive` AS `isActive`, `cooking_timers`.`isPaused` AS `isPaused`, `cooking_timers`.`startTime` AS `startTime`, `cooking_timers`.`completedAt` AS `completedAt`, `cooking_timers`.`createdAt` AS `createdAt` FROM cooking_timers WHERE isActive = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfSessionId: Int = 1
        val _columnIndexOfStepIndex: Int = 2
        val _columnIndexOfName: Int = 3
        val _columnIndexOfDurationSeconds: Int = 4
        val _columnIndexOfRemainingSeconds: Int = 5
        val _columnIndexOfIsActive: Int = 6
        val _columnIndexOfIsPaused: Int = 7
        val _columnIndexOfStartTime: Int = 8
        val _columnIndexOfCompletedAt: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: MutableList<CookingTimerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingTimerEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpStepIndex: Int
          _tmpStepIndex = _stmt.getLong(_columnIndexOfStepIndex).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationSeconds: Long
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          val _tmpRemainingSeconds: Long
          _tmpRemainingSeconds = _stmt.getLong(_columnIndexOfRemainingSeconds)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpIsPaused: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPaused).toInt()
          _tmpIsPaused = _tmp_1 != 0
          val _tmpStartTime: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingTimerEntity(_tmpId,_tmpSessionId,_tmpStepIndex,_tmpName,_tmpDurationSeconds,_tmpRemainingSeconds,_tmpIsActive,_tmpIsPaused,_tmpStartTime,_tmpCompletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getActiveTimersFlow(): Flow<List<CookingTimerEntity>> {
    val _sql: String =
        "SELECT `cooking_timers`.`id` AS `id`, `cooking_timers`.`sessionId` AS `sessionId`, `cooking_timers`.`stepIndex` AS `stepIndex`, `cooking_timers`.`name` AS `name`, `cooking_timers`.`durationSeconds` AS `durationSeconds`, `cooking_timers`.`remainingSeconds` AS `remainingSeconds`, `cooking_timers`.`isActive` AS `isActive`, `cooking_timers`.`isPaused` AS `isPaused`, `cooking_timers`.`startTime` AS `startTime`, `cooking_timers`.`completedAt` AS `completedAt`, `cooking_timers`.`createdAt` AS `createdAt` FROM cooking_timers WHERE isActive = 1"
    return createFlow(__db, false, arrayOf("cooking_timers")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfSessionId: Int = 1
        val _columnIndexOfStepIndex: Int = 2
        val _columnIndexOfName: Int = 3
        val _columnIndexOfDurationSeconds: Int = 4
        val _columnIndexOfRemainingSeconds: Int = 5
        val _columnIndexOfIsActive: Int = 6
        val _columnIndexOfIsPaused: Int = 7
        val _columnIndexOfStartTime: Int = 8
        val _columnIndexOfCompletedAt: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: MutableList<CookingTimerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingTimerEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpStepIndex: Int
          _tmpStepIndex = _stmt.getLong(_columnIndexOfStepIndex).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationSeconds: Long
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          val _tmpRemainingSeconds: Long
          _tmpRemainingSeconds = _stmt.getLong(_columnIndexOfRemainingSeconds)
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpIsPaused: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPaused).toInt()
          _tmpIsPaused = _tmp_1 != 0
          val _tmpStartTime: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingTimerEntity(_tmpId,_tmpSessionId,_tmpStepIndex,_tmpName,_tmpDurationSeconds,_tmpRemainingSeconds,_tmpIsActive,_tmpIsPaused,_tmpStartTime,_tmpCompletedAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM cooking_timers WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePauseState(stepIndex: Int, isPaused: Boolean) {
    val _sql: String = "UPDATE cooking_timers SET isPaused = ? WHERE stepIndex = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (isPaused) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, stepIndex.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateRemainingTime(timerId: String, remainingSeconds: Long) {
    val _sql: String = "UPDATE cooking_timers SET remainingSeconds = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, remainingSeconds)
        _argIndex = 2
        _stmt.bindText(_argIndex, timerId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun completeTimer(timerId: String, completedAt: Long) {
    val _sql: String = """
        |
        |        UPDATE cooking_timers 
        |        SET isActive = 0, completedAt = ? 
        |        WHERE id = ?
        |    
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, completedAt)
        _argIndex = 2
        _stmt.bindText(_argIndex, timerId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBySessionId(sessionId: String) {
    val _sql: String = "DELETE FROM cooking_timers WHERE sessionId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM cooking_timers"
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
