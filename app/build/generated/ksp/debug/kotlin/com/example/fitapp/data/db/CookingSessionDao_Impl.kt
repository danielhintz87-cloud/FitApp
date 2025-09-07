package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CookingSessionDao_Impl(
  __db: RoomDatabase,
) : CookingSessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCookingSessionEntity: EntityInsertAdapter<CookingSessionEntity>

  private val __updateAdapterOfCookingSessionEntity:
      EntityDeleteOrUpdateAdapter<CookingSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCookingSessionEntity = object :
        EntityInsertAdapter<CookingSessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `cooking_sessions` (`id`,`recipeId`,`startTime`,`endTime`,`status`,`currentStep`,`totalSteps`,`estimatedDuration`,`actualDuration`,`notes`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CookingSessionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.recipeId)
        statement.bindLong(3, entity.startTime)
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTime)
        }
        statement.bindText(5, entity.status)
        statement.bindLong(6, entity.currentStep.toLong())
        statement.bindLong(7, entity.totalSteps.toLong())
        val _tmpEstimatedDuration: Long? = entity.estimatedDuration
        if (_tmpEstimatedDuration == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEstimatedDuration)
        }
        val _tmpActualDuration: Long? = entity.actualDuration
        if (_tmpActualDuration == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpActualDuration)
        }
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpNotes)
        }
        statement.bindLong(11, entity.createdAt)
      }
    }
    this.__updateAdapterOfCookingSessionEntity = object :
        EntityDeleteOrUpdateAdapter<CookingSessionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `cooking_sessions` SET `id` = ?,`recipeId` = ?,`startTime` = ?,`endTime` = ?,`status` = ?,`currentStep` = ?,`totalSteps` = ?,`estimatedDuration` = ?,`actualDuration` = ?,`notes` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CookingSessionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.recipeId)
        statement.bindLong(3, entity.startTime)
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTime)
        }
        statement.bindText(5, entity.status)
        statement.bindLong(6, entity.currentStep.toLong())
        statement.bindLong(7, entity.totalSteps.toLong())
        val _tmpEstimatedDuration: Long? = entity.estimatedDuration
        if (_tmpEstimatedDuration == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEstimatedDuration)
        }
        val _tmpActualDuration: Long? = entity.actualDuration
        if (_tmpActualDuration == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpActualDuration)
        }
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpNotes)
        }
        statement.bindLong(11, entity.createdAt)
        statement.bindText(12, entity.id)
      }
    }
  }

  public override suspend fun insert(session: CookingSessionEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfCookingSessionEntity.insert(_connection, session)
  }

  public override suspend fun update(session: CookingSessionEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfCookingSessionEntity.handle(_connection, session)
  }

  public override suspend fun getById(id: String): CookingSessionEntity? {
    val _sql: String = "SELECT * FROM cooking_sessions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRecipeId: Int = getColumnIndexOrThrow(_stmt, "recipeId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCurrentStep: Int = getColumnIndexOrThrow(_stmt, "currentStep")
        val _columnIndexOfTotalSteps: Int = getColumnIndexOrThrow(_stmt, "totalSteps")
        val _columnIndexOfEstimatedDuration: Int = getColumnIndexOrThrow(_stmt, "estimatedDuration")
        val _columnIndexOfActualDuration: Int = getColumnIndexOrThrow(_stmt, "actualDuration")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: CookingSessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRecipeId: String
          _tmpRecipeId = _stmt.getText(_columnIndexOfRecipeId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentStep: Int
          _tmpCurrentStep = _stmt.getLong(_columnIndexOfCurrentStep).toInt()
          val _tmpTotalSteps: Int
          _tmpTotalSteps = _stmt.getLong(_columnIndexOfTotalSteps).toInt()
          val _tmpEstimatedDuration: Long?
          if (_stmt.isNull(_columnIndexOfEstimatedDuration)) {
            _tmpEstimatedDuration = null
          } else {
            _tmpEstimatedDuration = _stmt.getLong(_columnIndexOfEstimatedDuration)
          }
          val _tmpActualDuration: Long?
          if (_stmt.isNull(_columnIndexOfActualDuration)) {
            _tmpActualDuration = null
          } else {
            _tmpActualDuration = _stmt.getLong(_columnIndexOfActualDuration)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              CookingSessionEntity(_tmpId,_tmpRecipeId,_tmpStartTime,_tmpEndTime,_tmpStatus,_tmpCurrentStep,_tmpTotalSteps,_tmpEstimatedDuration,_tmpActualDuration,_tmpNotes,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByRecipeId(recipeId: String): List<CookingSessionEntity> {
    val _sql: String = "SELECT * FROM cooking_sessions WHERE recipeId = ? ORDER BY startTime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, recipeId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRecipeId: Int = getColumnIndexOrThrow(_stmt, "recipeId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCurrentStep: Int = getColumnIndexOrThrow(_stmt, "currentStep")
        val _columnIndexOfTotalSteps: Int = getColumnIndexOrThrow(_stmt, "totalSteps")
        val _columnIndexOfEstimatedDuration: Int = getColumnIndexOrThrow(_stmt, "estimatedDuration")
        val _columnIndexOfActualDuration: Int = getColumnIndexOrThrow(_stmt, "actualDuration")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CookingSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRecipeId: String
          _tmpRecipeId = _stmt.getText(_columnIndexOfRecipeId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentStep: Int
          _tmpCurrentStep = _stmt.getLong(_columnIndexOfCurrentStep).toInt()
          val _tmpTotalSteps: Int
          _tmpTotalSteps = _stmt.getLong(_columnIndexOfTotalSteps).toInt()
          val _tmpEstimatedDuration: Long?
          if (_stmt.isNull(_columnIndexOfEstimatedDuration)) {
            _tmpEstimatedDuration = null
          } else {
            _tmpEstimatedDuration = _stmt.getLong(_columnIndexOfEstimatedDuration)
          }
          val _tmpActualDuration: Long?
          if (_stmt.isNull(_columnIndexOfActualDuration)) {
            _tmpActualDuration = null
          } else {
            _tmpActualDuration = _stmt.getLong(_columnIndexOfActualDuration)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingSessionEntity(_tmpId,_tmpRecipeId,_tmpStartTime,_tmpEndTime,_tmpStatus,_tmpCurrentStep,_tmpTotalSteps,_tmpEstimatedDuration,_tmpActualDuration,_tmpNotes,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveSession(): CookingSessionEntity? {
    val _sql: String =
        "SELECT `cooking_sessions`.`id` AS `id`, `cooking_sessions`.`recipeId` AS `recipeId`, `cooking_sessions`.`startTime` AS `startTime`, `cooking_sessions`.`endTime` AS `endTime`, `cooking_sessions`.`status` AS `status`, `cooking_sessions`.`currentStep` AS `currentStep`, `cooking_sessions`.`totalSteps` AS `totalSteps`, `cooking_sessions`.`estimatedDuration` AS `estimatedDuration`, `cooking_sessions`.`actualDuration` AS `actualDuration`, `cooking_sessions`.`notes` AS `notes`, `cooking_sessions`.`createdAt` AS `createdAt` FROM cooking_sessions WHERE status = 'active' LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfRecipeId: Int = 1
        val _columnIndexOfStartTime: Int = 2
        val _columnIndexOfEndTime: Int = 3
        val _columnIndexOfStatus: Int = 4
        val _columnIndexOfCurrentStep: Int = 5
        val _columnIndexOfTotalSteps: Int = 6
        val _columnIndexOfEstimatedDuration: Int = 7
        val _columnIndexOfActualDuration: Int = 8
        val _columnIndexOfNotes: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: CookingSessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRecipeId: String
          _tmpRecipeId = _stmt.getText(_columnIndexOfRecipeId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentStep: Int
          _tmpCurrentStep = _stmt.getLong(_columnIndexOfCurrentStep).toInt()
          val _tmpTotalSteps: Int
          _tmpTotalSteps = _stmt.getLong(_columnIndexOfTotalSteps).toInt()
          val _tmpEstimatedDuration: Long?
          if (_stmt.isNull(_columnIndexOfEstimatedDuration)) {
            _tmpEstimatedDuration = null
          } else {
            _tmpEstimatedDuration = _stmt.getLong(_columnIndexOfEstimatedDuration)
          }
          val _tmpActualDuration: Long?
          if (_stmt.isNull(_columnIndexOfActualDuration)) {
            _tmpActualDuration = null
          } else {
            _tmpActualDuration = _stmt.getLong(_columnIndexOfActualDuration)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              CookingSessionEntity(_tmpId,_tmpRecipeId,_tmpStartTime,_tmpEndTime,_tmpStatus,_tmpCurrentStep,_tmpTotalSteps,_tmpEstimatedDuration,_tmpActualDuration,_tmpNotes,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<CookingSessionEntity> {
    val _sql: String =
        "SELECT `cooking_sessions`.`id` AS `id`, `cooking_sessions`.`recipeId` AS `recipeId`, `cooking_sessions`.`startTime` AS `startTime`, `cooking_sessions`.`endTime` AS `endTime`, `cooking_sessions`.`status` AS `status`, `cooking_sessions`.`currentStep` AS `currentStep`, `cooking_sessions`.`totalSteps` AS `totalSteps`, `cooking_sessions`.`estimatedDuration` AS `estimatedDuration`, `cooking_sessions`.`actualDuration` AS `actualDuration`, `cooking_sessions`.`notes` AS `notes`, `cooking_sessions`.`createdAt` AS `createdAt` FROM cooking_sessions ORDER BY startTime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfRecipeId: Int = 1
        val _columnIndexOfStartTime: Int = 2
        val _columnIndexOfEndTime: Int = 3
        val _columnIndexOfStatus: Int = 4
        val _columnIndexOfCurrentStep: Int = 5
        val _columnIndexOfTotalSteps: Int = 6
        val _columnIndexOfEstimatedDuration: Int = 7
        val _columnIndexOfActualDuration: Int = 8
        val _columnIndexOfNotes: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: MutableList<CookingSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRecipeId: String
          _tmpRecipeId = _stmt.getText(_columnIndexOfRecipeId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentStep: Int
          _tmpCurrentStep = _stmt.getLong(_columnIndexOfCurrentStep).toInt()
          val _tmpTotalSteps: Int
          _tmpTotalSteps = _stmt.getLong(_columnIndexOfTotalSteps).toInt()
          val _tmpEstimatedDuration: Long?
          if (_stmt.isNull(_columnIndexOfEstimatedDuration)) {
            _tmpEstimatedDuration = null
          } else {
            _tmpEstimatedDuration = _stmt.getLong(_columnIndexOfEstimatedDuration)
          }
          val _tmpActualDuration: Long?
          if (_stmt.isNull(_columnIndexOfActualDuration)) {
            _tmpActualDuration = null
          } else {
            _tmpActualDuration = _stmt.getLong(_columnIndexOfActualDuration)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingSessionEntity(_tmpId,_tmpRecipeId,_tmpStartTime,_tmpEndTime,_tmpStatus,_tmpCurrentStep,_tmpTotalSteps,_tmpEstimatedDuration,_tmpActualDuration,_tmpNotes,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllFlow(): Flow<List<CookingSessionEntity>> {
    val _sql: String =
        "SELECT `cooking_sessions`.`id` AS `id`, `cooking_sessions`.`recipeId` AS `recipeId`, `cooking_sessions`.`startTime` AS `startTime`, `cooking_sessions`.`endTime` AS `endTime`, `cooking_sessions`.`status` AS `status`, `cooking_sessions`.`currentStep` AS `currentStep`, `cooking_sessions`.`totalSteps` AS `totalSteps`, `cooking_sessions`.`estimatedDuration` AS `estimatedDuration`, `cooking_sessions`.`actualDuration` AS `actualDuration`, `cooking_sessions`.`notes` AS `notes`, `cooking_sessions`.`createdAt` AS `createdAt` FROM cooking_sessions ORDER BY startTime DESC"
    return createFlow(__db, false, arrayOf("cooking_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfRecipeId: Int = 1
        val _columnIndexOfStartTime: Int = 2
        val _columnIndexOfEndTime: Int = 3
        val _columnIndexOfStatus: Int = 4
        val _columnIndexOfCurrentStep: Int = 5
        val _columnIndexOfTotalSteps: Int = 6
        val _columnIndexOfEstimatedDuration: Int = 7
        val _columnIndexOfActualDuration: Int = 8
        val _columnIndexOfNotes: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: MutableList<CookingSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRecipeId: String
          _tmpRecipeId = _stmt.getText(_columnIndexOfRecipeId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentStep: Int
          _tmpCurrentStep = _stmt.getLong(_columnIndexOfCurrentStep).toInt()
          val _tmpTotalSteps: Int
          _tmpTotalSteps = _stmt.getLong(_columnIndexOfTotalSteps).toInt()
          val _tmpEstimatedDuration: Long?
          if (_stmt.isNull(_columnIndexOfEstimatedDuration)) {
            _tmpEstimatedDuration = null
          } else {
            _tmpEstimatedDuration = _stmt.getLong(_columnIndexOfEstimatedDuration)
          }
          val _tmpActualDuration: Long?
          if (_stmt.isNull(_columnIndexOfActualDuration)) {
            _tmpActualDuration = null
          } else {
            _tmpActualDuration = _stmt.getLong(_columnIndexOfActualDuration)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingSessionEntity(_tmpId,_tmpRecipeId,_tmpStartTime,_tmpEndTime,_tmpStatus,_tmpCurrentStep,_tmpTotalSteps,_tmpEstimatedDuration,_tmpActualDuration,_tmpNotes,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long):
      List<CookingSessionEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM cooking_sessions 
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
        val _columnIndexOfRecipeId: Int = getColumnIndexOrThrow(_stmt, "recipeId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCurrentStep: Int = getColumnIndexOrThrow(_stmt, "currentStep")
        val _columnIndexOfTotalSteps: Int = getColumnIndexOrThrow(_stmt, "totalSteps")
        val _columnIndexOfEstimatedDuration: Int = getColumnIndexOrThrow(_stmt, "estimatedDuration")
        val _columnIndexOfActualDuration: Int = getColumnIndexOrThrow(_stmt, "actualDuration")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CookingSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CookingSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRecipeId: String
          _tmpRecipeId = _stmt.getText(_columnIndexOfRecipeId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentStep: Int
          _tmpCurrentStep = _stmt.getLong(_columnIndexOfCurrentStep).toInt()
          val _tmpTotalSteps: Int
          _tmpTotalSteps = _stmt.getLong(_columnIndexOfTotalSteps).toInt()
          val _tmpEstimatedDuration: Long?
          if (_stmt.isNull(_columnIndexOfEstimatedDuration)) {
            _tmpEstimatedDuration = null
          } else {
            _tmpEstimatedDuration = _stmt.getLong(_columnIndexOfEstimatedDuration)
          }
          val _tmpActualDuration: Long?
          if (_stmt.isNull(_columnIndexOfActualDuration)) {
            _tmpActualDuration = null
          } else {
            _tmpActualDuration = _stmt.getLong(_columnIndexOfActualDuration)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CookingSessionEntity(_tmpId,_tmpRecipeId,_tmpStartTime,_tmpEndTime,_tmpStatus,_tmpCurrentStep,_tmpTotalSteps,_tmpEstimatedDuration,_tmpActualDuration,_tmpNotes,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCompletedSessionsCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM cooking_sessions WHERE status = 'completed'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun getAverageCookingTime(): Float? {
    val _sql: String = """
        |
        |        SELECT AVG(actualDuration) FROM cooking_sessions 
        |        WHERE status = 'completed' AND actualDuration IS NOT NULL
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM cooking_sessions WHERE id = ?"
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

  public override suspend fun updateCurrentStep(sessionId: String, stepIndex: Int) {
    val _sql: String = "UPDATE cooking_sessions SET currentStep = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, stepIndex.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateStatus(sessionId: String, status: String) {
    val _sql: String = "UPDATE cooking_sessions SET status = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun completeCookingSession(
    sessionId: String,
    endTime: Long,
    actualDuration: Long,
  ) {
    val _sql: String = """
        |
        |        UPDATE cooking_sessions 
        |        SET endTime = ?, status = 'completed', actualDuration = ? 
        |        WHERE id = ?
        |    
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, endTime)
        _argIndex = 2
        _stmt.bindLong(_argIndex, actualDuration)
        _argIndex = 3
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM cooking_sessions"
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
