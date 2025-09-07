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
public class WorkoutPerformanceDao_Impl(
  __db: RoomDatabase,
) : WorkoutPerformanceDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWorkoutPerformanceEntity:
      EntityInsertAdapter<WorkoutPerformanceEntity>

  private val __updateAdapterOfWorkoutPerformanceEntity:
      EntityDeleteOrUpdateAdapter<WorkoutPerformanceEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWorkoutPerformanceEntity = object :
        EntityInsertAdapter<WorkoutPerformanceEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `workout_performance` (`id`,`exerciseId`,`sessionId`,`planId`,`exerciseIndex`,`heartRateAvg`,`heartRateMax`,`heartRateZone`,`reps`,`weight`,`volume`,`restTime`,`actualRestTime`,`formQuality`,`perceivedExertion`,`movementSpeed`,`rangeOfMotion`,`timestamp`,`duration`,`isPersonalRecord`,`notes`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutPerformanceEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.exerciseId)
        statement.bindText(3, entity.sessionId)
        statement.bindLong(4, entity.planId)
        statement.bindLong(5, entity.exerciseIndex.toLong())
        val _tmpHeartRateAvg: Int? = entity.heartRateAvg
        if (_tmpHeartRateAvg == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpHeartRateAvg.toLong())
        }
        val _tmpHeartRateMax: Int? = entity.heartRateMax
        if (_tmpHeartRateMax == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpHeartRateMax.toLong())
        }
        val _tmpHeartRateZone: String? = entity.heartRateZone
        if (_tmpHeartRateZone == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpHeartRateZone)
        }
        statement.bindLong(9, entity.reps.toLong())
        statement.bindDouble(10, entity.weight.toDouble())
        statement.bindDouble(11, entity.volume.toDouble())
        statement.bindLong(12, entity.restTime)
        statement.bindLong(13, entity.actualRestTime)
        statement.bindDouble(14, entity.formQuality.toDouble())
        val _tmpPerceivedExertion: Int? = entity.perceivedExertion
        if (_tmpPerceivedExertion == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpPerceivedExertion.toLong())
        }
        val _tmpMovementSpeed: Float? = entity.movementSpeed
        if (_tmpMovementSpeed == null) {
          statement.bindNull(16)
        } else {
          statement.bindDouble(16, _tmpMovementSpeed.toDouble())
        }
        val _tmpRangeOfMotion: Float? = entity.rangeOfMotion
        if (_tmpRangeOfMotion == null) {
          statement.bindNull(17)
        } else {
          statement.bindDouble(17, _tmpRangeOfMotion.toDouble())
        }
        statement.bindLong(18, entity.timestamp)
        statement.bindLong(19, entity.duration)
        val _tmp: Int = if (entity.isPersonalRecord) 1 else 0
        statement.bindLong(20, _tmp.toLong())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpNotes)
        }
      }
    }
    this.__updateAdapterOfWorkoutPerformanceEntity = object :
        EntityDeleteOrUpdateAdapter<WorkoutPerformanceEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `workout_performance` SET `id` = ?,`exerciseId` = ?,`sessionId` = ?,`planId` = ?,`exerciseIndex` = ?,`heartRateAvg` = ?,`heartRateMax` = ?,`heartRateZone` = ?,`reps` = ?,`weight` = ?,`volume` = ?,`restTime` = ?,`actualRestTime` = ?,`formQuality` = ?,`perceivedExertion` = ?,`movementSpeed` = ?,`rangeOfMotion` = ?,`timestamp` = ?,`duration` = ?,`isPersonalRecord` = ?,`notes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutPerformanceEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.exerciseId)
        statement.bindText(3, entity.sessionId)
        statement.bindLong(4, entity.planId)
        statement.bindLong(5, entity.exerciseIndex.toLong())
        val _tmpHeartRateAvg: Int? = entity.heartRateAvg
        if (_tmpHeartRateAvg == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpHeartRateAvg.toLong())
        }
        val _tmpHeartRateMax: Int? = entity.heartRateMax
        if (_tmpHeartRateMax == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpHeartRateMax.toLong())
        }
        val _tmpHeartRateZone: String? = entity.heartRateZone
        if (_tmpHeartRateZone == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpHeartRateZone)
        }
        statement.bindLong(9, entity.reps.toLong())
        statement.bindDouble(10, entity.weight.toDouble())
        statement.bindDouble(11, entity.volume.toDouble())
        statement.bindLong(12, entity.restTime)
        statement.bindLong(13, entity.actualRestTime)
        statement.bindDouble(14, entity.formQuality.toDouble())
        val _tmpPerceivedExertion: Int? = entity.perceivedExertion
        if (_tmpPerceivedExertion == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpPerceivedExertion.toLong())
        }
        val _tmpMovementSpeed: Float? = entity.movementSpeed
        if (_tmpMovementSpeed == null) {
          statement.bindNull(16)
        } else {
          statement.bindDouble(16, _tmpMovementSpeed.toDouble())
        }
        val _tmpRangeOfMotion: Float? = entity.rangeOfMotion
        if (_tmpRangeOfMotion == null) {
          statement.bindNull(17)
        } else {
          statement.bindDouble(17, _tmpRangeOfMotion.toDouble())
        }
        statement.bindLong(18, entity.timestamp)
        statement.bindLong(19, entity.duration)
        val _tmp: Int = if (entity.isPersonalRecord) 1 else 0
        statement.bindLong(20, _tmp.toLong())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpNotes)
        }
        statement.bindText(22, entity.id)
      }
    }
  }

  public override suspend fun insert(performance: WorkoutPerformanceEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfWorkoutPerformanceEntity.insert(_connection, performance)
  }

  public override suspend fun update(performance: WorkoutPerformanceEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfWorkoutPerformanceEntity.handle(_connection, performance)
  }

  public override suspend fun getById(id: String): WorkoutPerformanceEntity? {
    val _sql: String = "SELECT * FROM workout_performance WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfExerciseIndex: Int = getColumnIndexOrThrow(_stmt, "exerciseIndex")
        val _columnIndexOfHeartRateAvg: Int = getColumnIndexOrThrow(_stmt, "heartRateAvg")
        val _columnIndexOfHeartRateMax: Int = getColumnIndexOrThrow(_stmt, "heartRateMax")
        val _columnIndexOfHeartRateZone: Int = getColumnIndexOrThrow(_stmt, "heartRateZone")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfVolume: Int = getColumnIndexOrThrow(_stmt, "volume")
        val _columnIndexOfRestTime: Int = getColumnIndexOrThrow(_stmt, "restTime")
        val _columnIndexOfActualRestTime: Int = getColumnIndexOrThrow(_stmt, "actualRestTime")
        val _columnIndexOfFormQuality: Int = getColumnIndexOrThrow(_stmt, "formQuality")
        val _columnIndexOfPerceivedExertion: Int = getColumnIndexOrThrow(_stmt, "perceivedExertion")
        val _columnIndexOfMovementSpeed: Int = getColumnIndexOrThrow(_stmt, "movementSpeed")
        val _columnIndexOfRangeOfMotion: Int = getColumnIndexOrThrow(_stmt, "rangeOfMotion")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfIsPersonalRecord: Int = getColumnIndexOrThrow(_stmt, "isPersonalRecord")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: WorkoutPerformanceEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _result =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBySessionId(sessionId: String): List<WorkoutPerformanceEntity> {
    val _sql: String =
        "SELECT * FROM workout_performance WHERE sessionId = ? ORDER BY exerciseIndex"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfExerciseIndex: Int = getColumnIndexOrThrow(_stmt, "exerciseIndex")
        val _columnIndexOfHeartRateAvg: Int = getColumnIndexOrThrow(_stmt, "heartRateAvg")
        val _columnIndexOfHeartRateMax: Int = getColumnIndexOrThrow(_stmt, "heartRateMax")
        val _columnIndexOfHeartRateZone: Int = getColumnIndexOrThrow(_stmt, "heartRateZone")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfVolume: Int = getColumnIndexOrThrow(_stmt, "volume")
        val _columnIndexOfRestTime: Int = getColumnIndexOrThrow(_stmt, "restTime")
        val _columnIndexOfActualRestTime: Int = getColumnIndexOrThrow(_stmt, "actualRestTime")
        val _columnIndexOfFormQuality: Int = getColumnIndexOrThrow(_stmt, "formQuality")
        val _columnIndexOfPerceivedExertion: Int = getColumnIndexOrThrow(_stmt, "perceivedExertion")
        val _columnIndexOfMovementSpeed: Int = getColumnIndexOrThrow(_stmt, "movementSpeed")
        val _columnIndexOfRangeOfMotion: Int = getColumnIndexOrThrow(_stmt, "rangeOfMotion")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfIsPersonalRecord: Int = getColumnIndexOrThrow(_stmt, "isPersonalRecord")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<WorkoutPerformanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutPerformanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getBySessionIdFlow(sessionId: String): Flow<List<WorkoutPerformanceEntity>> {
    val _sql: String =
        "SELECT * FROM workout_performance WHERE sessionId = ? ORDER BY exerciseIndex"
    return createFlow(__db, false, arrayOf("workout_performance")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfExerciseIndex: Int = getColumnIndexOrThrow(_stmt, "exerciseIndex")
        val _columnIndexOfHeartRateAvg: Int = getColumnIndexOrThrow(_stmt, "heartRateAvg")
        val _columnIndexOfHeartRateMax: Int = getColumnIndexOrThrow(_stmt, "heartRateMax")
        val _columnIndexOfHeartRateZone: Int = getColumnIndexOrThrow(_stmt, "heartRateZone")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfVolume: Int = getColumnIndexOrThrow(_stmt, "volume")
        val _columnIndexOfRestTime: Int = getColumnIndexOrThrow(_stmt, "restTime")
        val _columnIndexOfActualRestTime: Int = getColumnIndexOrThrow(_stmt, "actualRestTime")
        val _columnIndexOfFormQuality: Int = getColumnIndexOrThrow(_stmt, "formQuality")
        val _columnIndexOfPerceivedExertion: Int = getColumnIndexOrThrow(_stmt, "perceivedExertion")
        val _columnIndexOfMovementSpeed: Int = getColumnIndexOrThrow(_stmt, "movementSpeed")
        val _columnIndexOfRangeOfMotion: Int = getColumnIndexOrThrow(_stmt, "rangeOfMotion")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfIsPersonalRecord: Int = getColumnIndexOrThrow(_stmt, "isPersonalRecord")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<WorkoutPerformanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutPerformanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByExerciseId(exerciseId: String): List<WorkoutPerformanceEntity> {
    val _sql: String =
        "SELECT * FROM workout_performance WHERE exerciseId = ? ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfExerciseIndex: Int = getColumnIndexOrThrow(_stmt, "exerciseIndex")
        val _columnIndexOfHeartRateAvg: Int = getColumnIndexOrThrow(_stmt, "heartRateAvg")
        val _columnIndexOfHeartRateMax: Int = getColumnIndexOrThrow(_stmt, "heartRateMax")
        val _columnIndexOfHeartRateZone: Int = getColumnIndexOrThrow(_stmt, "heartRateZone")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfVolume: Int = getColumnIndexOrThrow(_stmt, "volume")
        val _columnIndexOfRestTime: Int = getColumnIndexOrThrow(_stmt, "restTime")
        val _columnIndexOfActualRestTime: Int = getColumnIndexOrThrow(_stmt, "actualRestTime")
        val _columnIndexOfFormQuality: Int = getColumnIndexOrThrow(_stmt, "formQuality")
        val _columnIndexOfPerceivedExertion: Int = getColumnIndexOrThrow(_stmt, "perceivedExertion")
        val _columnIndexOfMovementSpeed: Int = getColumnIndexOrThrow(_stmt, "movementSpeed")
        val _columnIndexOfRangeOfMotion: Int = getColumnIndexOrThrow(_stmt, "rangeOfMotion")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfIsPersonalRecord: Int = getColumnIndexOrThrow(_stmt, "isPersonalRecord")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<WorkoutPerformanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutPerformanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecentByExerciseId(exerciseId: String, limit: Int):
      List<WorkoutPerformanceEntity> {
    val _sql: String =
        "SELECT * FROM workout_performance WHERE exerciseId = ? ORDER BY timestamp DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfExerciseIndex: Int = getColumnIndexOrThrow(_stmt, "exerciseIndex")
        val _columnIndexOfHeartRateAvg: Int = getColumnIndexOrThrow(_stmt, "heartRateAvg")
        val _columnIndexOfHeartRateMax: Int = getColumnIndexOrThrow(_stmt, "heartRateMax")
        val _columnIndexOfHeartRateZone: Int = getColumnIndexOrThrow(_stmt, "heartRateZone")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfVolume: Int = getColumnIndexOrThrow(_stmt, "volume")
        val _columnIndexOfRestTime: Int = getColumnIndexOrThrow(_stmt, "restTime")
        val _columnIndexOfActualRestTime: Int = getColumnIndexOrThrow(_stmt, "actualRestTime")
        val _columnIndexOfFormQuality: Int = getColumnIndexOrThrow(_stmt, "formQuality")
        val _columnIndexOfPerceivedExertion: Int = getColumnIndexOrThrow(_stmt, "perceivedExertion")
        val _columnIndexOfMovementSpeed: Int = getColumnIndexOrThrow(_stmt, "movementSpeed")
        val _columnIndexOfRangeOfMotion: Int = getColumnIndexOrThrow(_stmt, "rangeOfMotion")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfIsPersonalRecord: Int = getColumnIndexOrThrow(_stmt, "isPersonalRecord")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<WorkoutPerformanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutPerformanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByPlanId(planId: Long): List<WorkoutPerformanceEntity> {
    val _sql: String = "SELECT * FROM workout_performance WHERE planId = ? ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, planId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfExerciseIndex: Int = getColumnIndexOrThrow(_stmt, "exerciseIndex")
        val _columnIndexOfHeartRateAvg: Int = getColumnIndexOrThrow(_stmt, "heartRateAvg")
        val _columnIndexOfHeartRateMax: Int = getColumnIndexOrThrow(_stmt, "heartRateMax")
        val _columnIndexOfHeartRateZone: Int = getColumnIndexOrThrow(_stmt, "heartRateZone")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfVolume: Int = getColumnIndexOrThrow(_stmt, "volume")
        val _columnIndexOfRestTime: Int = getColumnIndexOrThrow(_stmt, "restTime")
        val _columnIndexOfActualRestTime: Int = getColumnIndexOrThrow(_stmt, "actualRestTime")
        val _columnIndexOfFormQuality: Int = getColumnIndexOrThrow(_stmt, "formQuality")
        val _columnIndexOfPerceivedExertion: Int = getColumnIndexOrThrow(_stmt, "perceivedExertion")
        val _columnIndexOfMovementSpeed: Int = getColumnIndexOrThrow(_stmt, "movementSpeed")
        val _columnIndexOfRangeOfMotion: Int = getColumnIndexOrThrow(_stmt, "rangeOfMotion")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfIsPersonalRecord: Int = getColumnIndexOrThrow(_stmt, "isPersonalRecord")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<WorkoutPerformanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutPerformanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPersonalRecords(): List<WorkoutPerformanceEntity> {
    val _sql: String =
        "SELECT `workout_performance`.`id` AS `id`, `workout_performance`.`exerciseId` AS `exerciseId`, `workout_performance`.`sessionId` AS `sessionId`, `workout_performance`.`planId` AS `planId`, `workout_performance`.`exerciseIndex` AS `exerciseIndex`, `workout_performance`.`heartRateAvg` AS `heartRateAvg`, `workout_performance`.`heartRateMax` AS `heartRateMax`, `workout_performance`.`heartRateZone` AS `heartRateZone`, `workout_performance`.`reps` AS `reps`, `workout_performance`.`weight` AS `weight`, `workout_performance`.`volume` AS `volume`, `workout_performance`.`restTime` AS `restTime`, `workout_performance`.`actualRestTime` AS `actualRestTime`, `workout_performance`.`formQuality` AS `formQuality`, `workout_performance`.`perceivedExertion` AS `perceivedExertion`, `workout_performance`.`movementSpeed` AS `movementSpeed`, `workout_performance`.`rangeOfMotion` AS `rangeOfMotion`, `workout_performance`.`timestamp` AS `timestamp`, `workout_performance`.`duration` AS `duration`, `workout_performance`.`isPersonalRecord` AS `isPersonalRecord`, `workout_performance`.`notes` AS `notes` FROM workout_performance WHERE isPersonalRecord = 1 ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfExerciseId: Int = 1
        val _columnIndexOfSessionId: Int = 2
        val _columnIndexOfPlanId: Int = 3
        val _columnIndexOfExerciseIndex: Int = 4
        val _columnIndexOfHeartRateAvg: Int = 5
        val _columnIndexOfHeartRateMax: Int = 6
        val _columnIndexOfHeartRateZone: Int = 7
        val _columnIndexOfReps: Int = 8
        val _columnIndexOfWeight: Int = 9
        val _columnIndexOfVolume: Int = 10
        val _columnIndexOfRestTime: Int = 11
        val _columnIndexOfActualRestTime: Int = 12
        val _columnIndexOfFormQuality: Int = 13
        val _columnIndexOfPerceivedExertion: Int = 14
        val _columnIndexOfMovementSpeed: Int = 15
        val _columnIndexOfRangeOfMotion: Int = 16
        val _columnIndexOfTimestamp: Int = 17
        val _columnIndexOfDuration: Int = 18
        val _columnIndexOfIsPersonalRecord: Int = 19
        val _columnIndexOfNotes: Int = 20
        val _result: MutableList<WorkoutPerformanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutPerformanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long):
      List<WorkoutPerformanceEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM workout_performance 
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
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfExerciseIndex: Int = getColumnIndexOrThrow(_stmt, "exerciseIndex")
        val _columnIndexOfHeartRateAvg: Int = getColumnIndexOrThrow(_stmt, "heartRateAvg")
        val _columnIndexOfHeartRateMax: Int = getColumnIndexOrThrow(_stmt, "heartRateMax")
        val _columnIndexOfHeartRateZone: Int = getColumnIndexOrThrow(_stmt, "heartRateZone")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfVolume: Int = getColumnIndexOrThrow(_stmt, "volume")
        val _columnIndexOfRestTime: Int = getColumnIndexOrThrow(_stmt, "restTime")
        val _columnIndexOfActualRestTime: Int = getColumnIndexOrThrow(_stmt, "actualRestTime")
        val _columnIndexOfFormQuality: Int = getColumnIndexOrThrow(_stmt, "formQuality")
        val _columnIndexOfPerceivedExertion: Int = getColumnIndexOrThrow(_stmt, "perceivedExertion")
        val _columnIndexOfMovementSpeed: Int = getColumnIndexOrThrow(_stmt, "movementSpeed")
        val _columnIndexOfRangeOfMotion: Int = getColumnIndexOrThrow(_stmt, "rangeOfMotion")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfIsPersonalRecord: Int = getColumnIndexOrThrow(_stmt, "isPersonalRecord")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<WorkoutPerformanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutPerformanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPlanId: Long
          _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          val _tmpExerciseIndex: Int
          _tmpExerciseIndex = _stmt.getLong(_columnIndexOfExerciseIndex).toInt()
          val _tmpHeartRateAvg: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateAvg)) {
            _tmpHeartRateAvg = null
          } else {
            _tmpHeartRateAvg = _stmt.getLong(_columnIndexOfHeartRateAvg).toInt()
          }
          val _tmpHeartRateMax: Int?
          if (_stmt.isNull(_columnIndexOfHeartRateMax)) {
            _tmpHeartRateMax = null
          } else {
            _tmpHeartRateMax = _stmt.getLong(_columnIndexOfHeartRateMax).toInt()
          }
          val _tmpHeartRateZone: String?
          if (_stmt.isNull(_columnIndexOfHeartRateZone)) {
            _tmpHeartRateZone = null
          } else {
            _tmpHeartRateZone = _stmt.getText(_columnIndexOfHeartRateZone)
          }
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpVolume: Float
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume).toFloat()
          val _tmpRestTime: Long
          _tmpRestTime = _stmt.getLong(_columnIndexOfRestTime)
          val _tmpActualRestTime: Long
          _tmpActualRestTime = _stmt.getLong(_columnIndexOfActualRestTime)
          val _tmpFormQuality: Float
          _tmpFormQuality = _stmt.getDouble(_columnIndexOfFormQuality).toFloat()
          val _tmpPerceivedExertion: Int?
          if (_stmt.isNull(_columnIndexOfPerceivedExertion)) {
            _tmpPerceivedExertion = null
          } else {
            _tmpPerceivedExertion = _stmt.getLong(_columnIndexOfPerceivedExertion).toInt()
          }
          val _tmpMovementSpeed: Float?
          if (_stmt.isNull(_columnIndexOfMovementSpeed)) {
            _tmpMovementSpeed = null
          } else {
            _tmpMovementSpeed = _stmt.getDouble(_columnIndexOfMovementSpeed).toFloat()
          }
          val _tmpRangeOfMotion: Float?
          if (_stmt.isNull(_columnIndexOfRangeOfMotion)) {
            _tmpRangeOfMotion = null
          } else {
            _tmpRangeOfMotion = _stmt.getDouble(_columnIndexOfRangeOfMotion).toFloat()
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpIsPersonalRecord: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPersonalRecord).toInt()
          _tmpIsPersonalRecord = _tmp != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              WorkoutPerformanceEntity(_tmpId,_tmpExerciseId,_tmpSessionId,_tmpPlanId,_tmpExerciseIndex,_tmpHeartRateAvg,_tmpHeartRateMax,_tmpHeartRateZone,_tmpReps,_tmpWeight,_tmpVolume,_tmpRestTime,_tmpActualRestTime,_tmpFormQuality,_tmpPerceivedExertion,_tmpMovementSpeed,_tmpRangeOfMotion,_tmpTimestamp,_tmpDuration,_tmpIsPersonalRecord,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAverageVolumeForExercise(exerciseId: String, sinceTimestamp: Long):
      Float? {
    val _sql: String = """
        |
        |        SELECT AVG(volume) FROM workout_performance 
        |        WHERE exerciseId = ? AND timestamp >= ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
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

  public override suspend fun getMaxVolumeForExercise(exerciseId: String): Float? {
    val _sql: String = """
        |
        |        SELECT MAX(volume) FROM workout_performance 
        |        WHERE exerciseId = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
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
    val _sql: String = "DELETE FROM workout_performance WHERE id = ?"
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
    val _sql: String = "DELETE FROM workout_performance"
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
