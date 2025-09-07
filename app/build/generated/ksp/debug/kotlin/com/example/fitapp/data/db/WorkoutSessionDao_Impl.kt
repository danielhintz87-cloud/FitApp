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
public class WorkoutSessionDao_Impl(
  __db: RoomDatabase,
) : WorkoutSessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWorkoutSessionEntity: EntityInsertAdapter<WorkoutSessionEntity>

  private val __updateAdapterOfWorkoutSessionEntity:
      EntityDeleteOrUpdateAdapter<WorkoutSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWorkoutSessionEntity = object :
        EntityInsertAdapter<WorkoutSessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `workout_sessions` (`id`,`planId`,`userId`,`startTime`,`endTime`,`totalVolume`,`averageHeartRate`,`caloriesBurned`,`workoutEfficiencyScore`,`fatigueLevel`,`personalRecordsAchieved`,`completionPercentage`,`sessionRating`,`sessionNotes`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutSessionEntity) {
        statement.bindText(1, entity.id)
        statement.bindLong(2, entity.planId)
        statement.bindText(3, entity.userId)
        statement.bindLong(4, entity.startTime)
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpEndTime)
        }
        statement.bindDouble(6, entity.totalVolume.toDouble())
        val _tmpAverageHeartRate: Int? = entity.averageHeartRate
        if (_tmpAverageHeartRate == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpAverageHeartRate.toLong())
        }
        val _tmpCaloriesBurned: Int? = entity.caloriesBurned
        if (_tmpCaloriesBurned == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpCaloriesBurned.toLong())
        }
        statement.bindDouble(9, entity.workoutEfficiencyScore.toDouble())
        statement.bindText(10, entity.fatigueLevel)
        statement.bindLong(11, entity.personalRecordsAchieved.toLong())
        statement.bindDouble(12, entity.completionPercentage.toDouble())
        val _tmpSessionRating: Int? = entity.sessionRating
        if (_tmpSessionRating == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpSessionRating.toLong())
        }
        val _tmpSessionNotes: String? = entity.sessionNotes
        if (_tmpSessionNotes == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpSessionNotes)
        }
      }
    }
    this.__updateAdapterOfWorkoutSessionEntity = object :
        EntityDeleteOrUpdateAdapter<WorkoutSessionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `workout_sessions` SET `id` = ?,`planId` = ?,`userId` = ?,`startTime` = ?,`endTime` = ?,`totalVolume` = ?,`averageHeartRate` = ?,`caloriesBurned` = ?,`workoutEfficiencyScore` = ?,`fatigueLevel` = ?,`personalRecordsAchieved` = ?,`completionPercentage` = ?,`sessionRating` = ?,`sessionNotes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutSessionEntity) {
        statement.bindText(1, entity.id)
        statement.bindLong(2, entity.planId)
        statement.bindText(3, entity.userId)
        statement.bindLong(4, entity.startTime)
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpEndTime)
        }
        statement.bindDouble(6, entity.totalVolume.toDouble())
        val _tmpAverageHeartRate: Int? = entity.averageHeartRate
        if (_tmpAverageHeartRate == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpAverageHeartRate.toLong())
        }
        val _tmpCaloriesBurned: Int? = entity.caloriesBurned
        if (_tmpCaloriesBurned == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpCaloriesBurned.toLong())
        }
        statement.bindDouble(9, entity.workoutEfficiencyScore.toDouble())
        statement.bindText(10, entity.fatigueLevel)
        statement.bindLong(11, entity.personalRecordsAchieved.toLong())
        statement.bindDouble(12, entity.completionPercentage.toDouble())
        val _tmpSessionRating: Int? = entity.sessionRating
        if (_tmpSessionRating == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpSessionRating.toLong())
        }
        val _tmpSessionNotes: String? = entity.sessionNotes
        if (_tmpSessionNotes == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpSessionNotes)
        }
        statement.bindText(15, entity.id)
      }
    }
  }

  public override suspend fun insert(session: WorkoutSessionEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfWorkoutSessionEntity.insert(_connection, session)
  }

  public override suspend fun update(session: WorkoutSessionEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfWorkoutSessionEntity.handle(_connection, session)
  }

  public override suspend fun getById(id: String): WorkoutSessionEntity? {
    val _sql: String = "SELECT * FROM workout_sessions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfTotalVolume: Int = getColumnIndexOrThrow(_stmt, "totalVolume")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfCaloriesBurned: Int = getColumnIndexOrThrow(_stmt, "caloriesBurned")
        val _columnIndexOfWorkoutEfficiencyScore: Int = getColumnIndexOrThrow(_stmt,
            "workoutEfficiencyScore")
        val _columnIndexOfFatigueLevel: Int = getColumnIndexOrThrow(_stmt, "fatigueLevel")
        val _columnIndexOfPersonalRecordsAchieved: Int = getColumnIndexOrThrow(_stmt,
            "personalRecordsAchieved")
        val _columnIndexOfCompletionPercentage: Int = getColumnIndexOrThrow(_stmt,
            "completionPercentage")
        val _columnIndexOfSessionRating: Int = getColumnIndexOrThrow(_stmt, "sessionRating")
        val _columnIndexOfSessionNotes: Int = getColumnIndexOrThrow(_stmt, "sessionNotes")
        val _result: WorkoutSessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _result =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<WorkoutSessionEntity> {
    val _sql: String =
        "SELECT `workout_sessions`.`id` AS `id`, `workout_sessions`.`planId` AS `planId`, `workout_sessions`.`userId` AS `userId`, `workout_sessions`.`startTime` AS `startTime`, `workout_sessions`.`endTime` AS `endTime`, `workout_sessions`.`totalVolume` AS `totalVolume`, `workout_sessions`.`averageHeartRate` AS `averageHeartRate`, `workout_sessions`.`caloriesBurned` AS `caloriesBurned`, `workout_sessions`.`workoutEfficiencyScore` AS `workoutEfficiencyScore`, `workout_sessions`.`fatigueLevel` AS `fatigueLevel`, `workout_sessions`.`personalRecordsAchieved` AS `personalRecordsAchieved`, `workout_sessions`.`completionPercentage` AS `completionPercentage`, `workout_sessions`.`sessionRating` AS `sessionRating`, `workout_sessions`.`sessionNotes` AS `sessionNotes` FROM workout_sessions ORDER BY startTime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfPlanId: Int = 1
        val _columnIndexOfUserId: Int = 2
        val _columnIndexOfStartTime: Int = 3
        val _columnIndexOfEndTime: Int = 4
        val _columnIndexOfTotalVolume: Int = 5
        val _columnIndexOfAverageHeartRate: Int = 6
        val _columnIndexOfCaloriesBurned: Int = 7
        val _columnIndexOfWorkoutEfficiencyScore: Int = 8
        val _columnIndexOfFatigueLevel: Int = 9
        val _columnIndexOfPersonalRecordsAchieved: Int = 10
        val _columnIndexOfCompletionPercentage: Int = 11
        val _columnIndexOfSessionRating: Int = 12
        val _columnIndexOfSessionNotes: Int = 13
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _item =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllFlow(): Flow<List<WorkoutSessionEntity>> {
    val _sql: String =
        "SELECT `workout_sessions`.`id` AS `id`, `workout_sessions`.`planId` AS `planId`, `workout_sessions`.`userId` AS `userId`, `workout_sessions`.`startTime` AS `startTime`, `workout_sessions`.`endTime` AS `endTime`, `workout_sessions`.`totalVolume` AS `totalVolume`, `workout_sessions`.`averageHeartRate` AS `averageHeartRate`, `workout_sessions`.`caloriesBurned` AS `caloriesBurned`, `workout_sessions`.`workoutEfficiencyScore` AS `workoutEfficiencyScore`, `workout_sessions`.`fatigueLevel` AS `fatigueLevel`, `workout_sessions`.`personalRecordsAchieved` AS `personalRecordsAchieved`, `workout_sessions`.`completionPercentage` AS `completionPercentage`, `workout_sessions`.`sessionRating` AS `sessionRating`, `workout_sessions`.`sessionNotes` AS `sessionNotes` FROM workout_sessions ORDER BY startTime DESC"
    return createFlow(__db, false, arrayOf("workout_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfPlanId: Int = 1
        val _columnIndexOfUserId: Int = 2
        val _columnIndexOfStartTime: Int = 3
        val _columnIndexOfEndTime: Int = 4
        val _columnIndexOfTotalVolume: Int = 5
        val _columnIndexOfAverageHeartRate: Int = 6
        val _columnIndexOfCaloriesBurned: Int = 7
        val _columnIndexOfWorkoutEfficiencyScore: Int = 8
        val _columnIndexOfFatigueLevel: Int = 9
        val _columnIndexOfPersonalRecordsAchieved: Int = 10
        val _columnIndexOfCompletionPercentage: Int = 11
        val _columnIndexOfSessionRating: Int = 12
        val _columnIndexOfSessionNotes: Int = 13
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _item =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByPlanId(planId: Long): List<WorkoutSessionEntity> {
    val _sql: String = "SELECT * FROM workout_sessions WHERE planId = ? ORDER BY startTime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, planId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfTotalVolume: Int = getColumnIndexOrThrow(_stmt, "totalVolume")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfCaloriesBurned: Int = getColumnIndexOrThrow(_stmt, "caloriesBurned")
        val _columnIndexOfWorkoutEfficiencyScore: Int = getColumnIndexOrThrow(_stmt,
            "workoutEfficiencyScore")
        val _columnIndexOfFatigueLevel: Int = getColumnIndexOrThrow(_stmt, "fatigueLevel")
        val _columnIndexOfPersonalRecordsAchieved: Int = getColumnIndexOrThrow(_stmt,
            "personalRecordsAchieved")
        val _columnIndexOfCompletionPercentage: Int = getColumnIndexOrThrow(_stmt,
            "completionPercentage")
        val _columnIndexOfSessionRating: Int = getColumnIndexOrThrow(_stmt, "sessionRating")
        val _columnIndexOfSessionNotes: Int = getColumnIndexOrThrow(_stmt, "sessionNotes")
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _item =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByUserId(userId: String): List<WorkoutSessionEntity> {
    val _sql: String = "SELECT * FROM workout_sessions WHERE userId = ? ORDER BY startTime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfTotalVolume: Int = getColumnIndexOrThrow(_stmt, "totalVolume")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfCaloriesBurned: Int = getColumnIndexOrThrow(_stmt, "caloriesBurned")
        val _columnIndexOfWorkoutEfficiencyScore: Int = getColumnIndexOrThrow(_stmt,
            "workoutEfficiencyScore")
        val _columnIndexOfFatigueLevel: Int = getColumnIndexOrThrow(_stmt, "fatigueLevel")
        val _columnIndexOfPersonalRecordsAchieved: Int = getColumnIndexOrThrow(_stmt,
            "personalRecordsAchieved")
        val _columnIndexOfCompletionPercentage: Int = getColumnIndexOrThrow(_stmt,
            "completionPercentage")
        val _columnIndexOfSessionRating: Int = getColumnIndexOrThrow(_stmt, "sessionRating")
        val _columnIndexOfSessionNotes: Int = getColumnIndexOrThrow(_stmt, "sessionNotes")
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _item =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveSession(): WorkoutSessionEntity? {
    val _sql: String =
        "SELECT `workout_sessions`.`id` AS `id`, `workout_sessions`.`planId` AS `planId`, `workout_sessions`.`userId` AS `userId`, `workout_sessions`.`startTime` AS `startTime`, `workout_sessions`.`endTime` AS `endTime`, `workout_sessions`.`totalVolume` AS `totalVolume`, `workout_sessions`.`averageHeartRate` AS `averageHeartRate`, `workout_sessions`.`caloriesBurned` AS `caloriesBurned`, `workout_sessions`.`workoutEfficiencyScore` AS `workoutEfficiencyScore`, `workout_sessions`.`fatigueLevel` AS `fatigueLevel`, `workout_sessions`.`personalRecordsAchieved` AS `personalRecordsAchieved`, `workout_sessions`.`completionPercentage` AS `completionPercentage`, `workout_sessions`.`sessionRating` AS `sessionRating`, `workout_sessions`.`sessionNotes` AS `sessionNotes` FROM workout_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfPlanId: Int = 1
        val _columnIndexOfUserId: Int = 2
        val _columnIndexOfStartTime: Int = 3
        val _columnIndexOfEndTime: Int = 4
        val _columnIndexOfTotalVolume: Int = 5
        val _columnIndexOfAverageHeartRate: Int = 6
        val _columnIndexOfCaloriesBurned: Int = 7
        val _columnIndexOfWorkoutEfficiencyScore: Int = 8
        val _columnIndexOfFatigueLevel: Int = 9
        val _columnIndexOfPersonalRecordsAchieved: Int = 10
        val _columnIndexOfCompletionPercentage: Int = 11
        val _columnIndexOfSessionRating: Int = 12
        val _columnIndexOfSessionNotes: Int = 13
        val _result: WorkoutSessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _result =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecent(limit: Int): List<WorkoutSessionEntity> {
    val _sql: String = "SELECT * FROM workout_sessions ORDER BY startTime DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfTotalVolume: Int = getColumnIndexOrThrow(_stmt, "totalVolume")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfCaloriesBurned: Int = getColumnIndexOrThrow(_stmt, "caloriesBurned")
        val _columnIndexOfWorkoutEfficiencyScore: Int = getColumnIndexOrThrow(_stmt,
            "workoutEfficiencyScore")
        val _columnIndexOfFatigueLevel: Int = getColumnIndexOrThrow(_stmt, "fatigueLevel")
        val _columnIndexOfPersonalRecordsAchieved: Int = getColumnIndexOrThrow(_stmt,
            "personalRecordsAchieved")
        val _columnIndexOfCompletionPercentage: Int = getColumnIndexOrThrow(_stmt,
            "completionPercentage")
        val _columnIndexOfSessionRating: Int = getColumnIndexOrThrow(_stmt, "sessionRating")
        val _columnIndexOfSessionNotes: Int = getColumnIndexOrThrow(_stmt, "sessionNotes")
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _item =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long):
      List<WorkoutSessionEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM workout_sessions 
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
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfTotalVolume: Int = getColumnIndexOrThrow(_stmt, "totalVolume")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfCaloriesBurned: Int = getColumnIndexOrThrow(_stmt, "caloriesBurned")
        val _columnIndexOfWorkoutEfficiencyScore: Int = getColumnIndexOrThrow(_stmt,
            "workoutEfficiencyScore")
        val _columnIndexOfFatigueLevel: Int = getColumnIndexOrThrow(_stmt, "fatigueLevel")
        val _columnIndexOfPersonalRecordsAchieved: Int = getColumnIndexOrThrow(_stmt,
            "personalRecordsAchieved")
        val _columnIndexOfCompletionPercentage: Int = getColumnIndexOrThrow(_stmt,
            "completionPercentage")
        val _columnIndexOfSessionRating: Int = getColumnIndexOrThrow(_stmt, "sessionRating")
        val _columnIndexOfSessionNotes: Int = getColumnIndexOrThrow(_stmt, "sessionNotes")
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpStartTime: Long
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime)
          val _tmpEndTime: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime)
          }
          val _tmpTotalVolume: Float
          _tmpTotalVolume = _stmt.getDouble(_columnIndexOfTotalVolume).toFloat()
          val _tmpAverageHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getLong(_columnIndexOfAverageHeartRate).toInt()
          }
          val _tmpCaloriesBurned: Int?
          if (_stmt.isNull(_columnIndexOfCaloriesBurned)) {
            _tmpCaloriesBurned = null
          } else {
            _tmpCaloriesBurned = _stmt.getLong(_columnIndexOfCaloriesBurned).toInt()
          }
          val _tmpWorkoutEfficiencyScore: Float
          _tmpWorkoutEfficiencyScore =
              _stmt.getDouble(_columnIndexOfWorkoutEfficiencyScore).toFloat()
          val _tmpFatigueLevel: String
          _tmpFatigueLevel = _stmt.getText(_columnIndexOfFatigueLevel)
          val _tmpPersonalRecordsAchieved: Int
          _tmpPersonalRecordsAchieved = _stmt.getLong(_columnIndexOfPersonalRecordsAchieved).toInt()
          val _tmpCompletionPercentage: Float
          _tmpCompletionPercentage = _stmt.getDouble(_columnIndexOfCompletionPercentage).toFloat()
          val _tmpSessionRating: Int?
          if (_stmt.isNull(_columnIndexOfSessionRating)) {
            _tmpSessionRating = null
          } else {
            _tmpSessionRating = _stmt.getLong(_columnIndexOfSessionRating).toInt()
          }
          val _tmpSessionNotes: String?
          if (_stmt.isNull(_columnIndexOfSessionNotes)) {
            _tmpSessionNotes = null
          } else {
            _tmpSessionNotes = _stmt.getText(_columnIndexOfSessionNotes)
          }
          _item =
              WorkoutSessionEntity(_tmpId,_tmpPlanId,_tmpUserId,_tmpStartTime,_tmpEndTime,_tmpTotalVolume,_tmpAverageHeartRate,_tmpCaloriesBurned,_tmpWorkoutEfficiencyScore,_tmpFatigueLevel,_tmpPersonalRecordsAchieved,_tmpCompletionPercentage,_tmpSessionRating,_tmpSessionNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAverageEfficiencyScore(userId: String, sinceTimestamp: Long):
      Float? {
    val _sql: String = """
        |
        |        SELECT AVG(workoutEfficiencyScore) FROM workout_sessions 
        |        WHERE userId = ? AND startTime >= ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, sinceTimestamp)
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

  public override suspend fun getTotalPersonalRecords(userId: String, sinceTimestamp: Long): Int? {
    val _sql: String = """
        |
        |        SELECT SUM(personalRecordsAchieved) FROM workout_sessions 
        |        WHERE userId = ? AND startTime >= ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, sinceTimestamp)
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

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM workout_sessions WHERE id = ?"
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

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM workout_sessions"
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
