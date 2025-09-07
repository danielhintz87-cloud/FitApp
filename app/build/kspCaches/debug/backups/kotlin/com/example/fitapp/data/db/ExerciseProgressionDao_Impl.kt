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
public class ExerciseProgressionDao_Impl(
  __db: RoomDatabase,
) : ExerciseProgressionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExerciseProgressionEntity:
      EntityInsertAdapter<ExerciseProgressionEntity>

  private val __updateAdapterOfExerciseProgressionEntity:
      EntityDeleteOrUpdateAdapter<ExerciseProgressionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfExerciseProgressionEntity = object :
        EntityInsertAdapter<ExerciseProgressionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `exercise_progressions` (`id`,`exerciseId`,`userId`,`currentWeight`,`recommendedWeight`,`currentReps`,`recommendedReps`,`progressionReason`,`performanceTrend`,`plateauDetected`,`plateauWeeks`,`lastProgressDate`,`aiConfidence`,`nextReviewDate`,`adaptationNotes`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseProgressionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.exerciseId)
        statement.bindText(3, entity.userId)
        statement.bindDouble(4, entity.currentWeight.toDouble())
        statement.bindDouble(5, entity.recommendedWeight.toDouble())
        statement.bindLong(6, entity.currentReps.toLong())
        statement.bindLong(7, entity.recommendedReps.toLong())
        statement.bindText(8, entity.progressionReason)
        statement.bindText(9, entity.performanceTrend)
        val _tmp: Int = if (entity.plateauDetected) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        statement.bindLong(11, entity.plateauWeeks.toLong())
        statement.bindLong(12, entity.lastProgressDate)
        statement.bindDouble(13, entity.aiConfidence.toDouble())
        statement.bindLong(14, entity.nextReviewDate)
        val _tmpAdaptationNotes: String? = entity.adaptationNotes
        if (_tmpAdaptationNotes == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpAdaptationNotes)
        }
      }
    }
    this.__updateAdapterOfExerciseProgressionEntity = object :
        EntityDeleteOrUpdateAdapter<ExerciseProgressionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `exercise_progressions` SET `id` = ?,`exerciseId` = ?,`userId` = ?,`currentWeight` = ?,`recommendedWeight` = ?,`currentReps` = ?,`recommendedReps` = ?,`progressionReason` = ?,`performanceTrend` = ?,`plateauDetected` = ?,`plateauWeeks` = ?,`lastProgressDate` = ?,`aiConfidence` = ?,`nextReviewDate` = ?,`adaptationNotes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseProgressionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.exerciseId)
        statement.bindText(3, entity.userId)
        statement.bindDouble(4, entity.currentWeight.toDouble())
        statement.bindDouble(5, entity.recommendedWeight.toDouble())
        statement.bindLong(6, entity.currentReps.toLong())
        statement.bindLong(7, entity.recommendedReps.toLong())
        statement.bindText(8, entity.progressionReason)
        statement.bindText(9, entity.performanceTrend)
        val _tmp: Int = if (entity.plateauDetected) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        statement.bindLong(11, entity.plateauWeeks.toLong())
        statement.bindLong(12, entity.lastProgressDate)
        statement.bindDouble(13, entity.aiConfidence.toDouble())
        statement.bindLong(14, entity.nextReviewDate)
        val _tmpAdaptationNotes: String? = entity.adaptationNotes
        if (_tmpAdaptationNotes == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpAdaptationNotes)
        }
        statement.bindText(16, entity.id)
      }
    }
  }

  public override suspend fun insert(progression: ExerciseProgressionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfExerciseProgressionEntity.insert(_connection, progression)
  }

  public override suspend fun update(progression: ExerciseProgressionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfExerciseProgressionEntity.handle(_connection, progression)
  }

  public override suspend fun getById(id: String): ExerciseProgressionEntity? {
    val _sql: String = "SELECT * FROM exercise_progressions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfRecommendedWeight: Int = getColumnIndexOrThrow(_stmt, "recommendedWeight")
        val _columnIndexOfCurrentReps: Int = getColumnIndexOrThrow(_stmt, "currentReps")
        val _columnIndexOfRecommendedReps: Int = getColumnIndexOrThrow(_stmt, "recommendedReps")
        val _columnIndexOfProgressionReason: Int = getColumnIndexOrThrow(_stmt, "progressionReason")
        val _columnIndexOfPerformanceTrend: Int = getColumnIndexOrThrow(_stmt, "performanceTrend")
        val _columnIndexOfPlateauDetected: Int = getColumnIndexOrThrow(_stmt, "plateauDetected")
        val _columnIndexOfPlateauWeeks: Int = getColumnIndexOrThrow(_stmt, "plateauWeeks")
        val _columnIndexOfLastProgressDate: Int = getColumnIndexOrThrow(_stmt, "lastProgressDate")
        val _columnIndexOfAiConfidence: Int = getColumnIndexOrThrow(_stmt, "aiConfidence")
        val _columnIndexOfNextReviewDate: Int = getColumnIndexOrThrow(_stmt, "nextReviewDate")
        val _columnIndexOfAdaptationNotes: Int = getColumnIndexOrThrow(_stmt, "adaptationNotes")
        val _result: ExerciseProgressionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpRecommendedWeight: Float
          _tmpRecommendedWeight = _stmt.getDouble(_columnIndexOfRecommendedWeight).toFloat()
          val _tmpCurrentReps: Int
          _tmpCurrentReps = _stmt.getLong(_columnIndexOfCurrentReps).toInt()
          val _tmpRecommendedReps: Int
          _tmpRecommendedReps = _stmt.getLong(_columnIndexOfRecommendedReps).toInt()
          val _tmpProgressionReason: String
          _tmpProgressionReason = _stmt.getText(_columnIndexOfProgressionReason)
          val _tmpPerformanceTrend: String
          _tmpPerformanceTrend = _stmt.getText(_columnIndexOfPerformanceTrend)
          val _tmpPlateauDetected: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPlateauDetected).toInt()
          _tmpPlateauDetected = _tmp != 0
          val _tmpPlateauWeeks: Int
          _tmpPlateauWeeks = _stmt.getLong(_columnIndexOfPlateauWeeks).toInt()
          val _tmpLastProgressDate: Long
          _tmpLastProgressDate = _stmt.getLong(_columnIndexOfLastProgressDate)
          val _tmpAiConfidence: Float
          _tmpAiConfidence = _stmt.getDouble(_columnIndexOfAiConfidence).toFloat()
          val _tmpNextReviewDate: Long
          _tmpNextReviewDate = _stmt.getLong(_columnIndexOfNextReviewDate)
          val _tmpAdaptationNotes: String?
          if (_stmt.isNull(_columnIndexOfAdaptationNotes)) {
            _tmpAdaptationNotes = null
          } else {
            _tmpAdaptationNotes = _stmt.getText(_columnIndexOfAdaptationNotes)
          }
          _result =
              ExerciseProgressionEntity(_tmpId,_tmpExerciseId,_tmpUserId,_tmpCurrentWeight,_tmpRecommendedWeight,_tmpCurrentReps,_tmpRecommendedReps,_tmpProgressionReason,_tmpPerformanceTrend,_tmpPlateauDetected,_tmpPlateauWeeks,_tmpLastProgressDate,_tmpAiConfidence,_tmpNextReviewDate,_tmpAdaptationNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByExerciseAndUser(exerciseId: String, userId: String):
      ExerciseProgressionEntity? {
    val _sql: String = "SELECT * FROM exercise_progressions WHERE exerciseId = ? AND userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
        _argIndex = 2
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfRecommendedWeight: Int = getColumnIndexOrThrow(_stmt, "recommendedWeight")
        val _columnIndexOfCurrentReps: Int = getColumnIndexOrThrow(_stmt, "currentReps")
        val _columnIndexOfRecommendedReps: Int = getColumnIndexOrThrow(_stmt, "recommendedReps")
        val _columnIndexOfProgressionReason: Int = getColumnIndexOrThrow(_stmt, "progressionReason")
        val _columnIndexOfPerformanceTrend: Int = getColumnIndexOrThrow(_stmt, "performanceTrend")
        val _columnIndexOfPlateauDetected: Int = getColumnIndexOrThrow(_stmt, "plateauDetected")
        val _columnIndexOfPlateauWeeks: Int = getColumnIndexOrThrow(_stmt, "plateauWeeks")
        val _columnIndexOfLastProgressDate: Int = getColumnIndexOrThrow(_stmt, "lastProgressDate")
        val _columnIndexOfAiConfidence: Int = getColumnIndexOrThrow(_stmt, "aiConfidence")
        val _columnIndexOfNextReviewDate: Int = getColumnIndexOrThrow(_stmt, "nextReviewDate")
        val _columnIndexOfAdaptationNotes: Int = getColumnIndexOrThrow(_stmt, "adaptationNotes")
        val _result: ExerciseProgressionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpRecommendedWeight: Float
          _tmpRecommendedWeight = _stmt.getDouble(_columnIndexOfRecommendedWeight).toFloat()
          val _tmpCurrentReps: Int
          _tmpCurrentReps = _stmt.getLong(_columnIndexOfCurrentReps).toInt()
          val _tmpRecommendedReps: Int
          _tmpRecommendedReps = _stmt.getLong(_columnIndexOfRecommendedReps).toInt()
          val _tmpProgressionReason: String
          _tmpProgressionReason = _stmt.getText(_columnIndexOfProgressionReason)
          val _tmpPerformanceTrend: String
          _tmpPerformanceTrend = _stmt.getText(_columnIndexOfPerformanceTrend)
          val _tmpPlateauDetected: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPlateauDetected).toInt()
          _tmpPlateauDetected = _tmp != 0
          val _tmpPlateauWeeks: Int
          _tmpPlateauWeeks = _stmt.getLong(_columnIndexOfPlateauWeeks).toInt()
          val _tmpLastProgressDate: Long
          _tmpLastProgressDate = _stmt.getLong(_columnIndexOfLastProgressDate)
          val _tmpAiConfidence: Float
          _tmpAiConfidence = _stmt.getDouble(_columnIndexOfAiConfidence).toFloat()
          val _tmpNextReviewDate: Long
          _tmpNextReviewDate = _stmt.getLong(_columnIndexOfNextReviewDate)
          val _tmpAdaptationNotes: String?
          if (_stmt.isNull(_columnIndexOfAdaptationNotes)) {
            _tmpAdaptationNotes = null
          } else {
            _tmpAdaptationNotes = _stmt.getText(_columnIndexOfAdaptationNotes)
          }
          _result =
              ExerciseProgressionEntity(_tmpId,_tmpExerciseId,_tmpUserId,_tmpCurrentWeight,_tmpRecommendedWeight,_tmpCurrentReps,_tmpRecommendedReps,_tmpProgressionReason,_tmpPerformanceTrend,_tmpPlateauDetected,_tmpPlateauWeeks,_tmpLastProgressDate,_tmpAiConfidence,_tmpNextReviewDate,_tmpAdaptationNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByUserId(userId: String): List<ExerciseProgressionEntity> {
    val _sql: String =
        "SELECT * FROM exercise_progressions WHERE userId = ? ORDER BY lastProgressDate DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfRecommendedWeight: Int = getColumnIndexOrThrow(_stmt, "recommendedWeight")
        val _columnIndexOfCurrentReps: Int = getColumnIndexOrThrow(_stmt, "currentReps")
        val _columnIndexOfRecommendedReps: Int = getColumnIndexOrThrow(_stmt, "recommendedReps")
        val _columnIndexOfProgressionReason: Int = getColumnIndexOrThrow(_stmt, "progressionReason")
        val _columnIndexOfPerformanceTrend: Int = getColumnIndexOrThrow(_stmt, "performanceTrend")
        val _columnIndexOfPlateauDetected: Int = getColumnIndexOrThrow(_stmt, "plateauDetected")
        val _columnIndexOfPlateauWeeks: Int = getColumnIndexOrThrow(_stmt, "plateauWeeks")
        val _columnIndexOfLastProgressDate: Int = getColumnIndexOrThrow(_stmt, "lastProgressDate")
        val _columnIndexOfAiConfidence: Int = getColumnIndexOrThrow(_stmt, "aiConfidence")
        val _columnIndexOfNextReviewDate: Int = getColumnIndexOrThrow(_stmt, "nextReviewDate")
        val _columnIndexOfAdaptationNotes: Int = getColumnIndexOrThrow(_stmt, "adaptationNotes")
        val _result: MutableList<ExerciseProgressionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseProgressionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpRecommendedWeight: Float
          _tmpRecommendedWeight = _stmt.getDouble(_columnIndexOfRecommendedWeight).toFloat()
          val _tmpCurrentReps: Int
          _tmpCurrentReps = _stmt.getLong(_columnIndexOfCurrentReps).toInt()
          val _tmpRecommendedReps: Int
          _tmpRecommendedReps = _stmt.getLong(_columnIndexOfRecommendedReps).toInt()
          val _tmpProgressionReason: String
          _tmpProgressionReason = _stmt.getText(_columnIndexOfProgressionReason)
          val _tmpPerformanceTrend: String
          _tmpPerformanceTrend = _stmt.getText(_columnIndexOfPerformanceTrend)
          val _tmpPlateauDetected: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPlateauDetected).toInt()
          _tmpPlateauDetected = _tmp != 0
          val _tmpPlateauWeeks: Int
          _tmpPlateauWeeks = _stmt.getLong(_columnIndexOfPlateauWeeks).toInt()
          val _tmpLastProgressDate: Long
          _tmpLastProgressDate = _stmt.getLong(_columnIndexOfLastProgressDate)
          val _tmpAiConfidence: Float
          _tmpAiConfidence = _stmt.getDouble(_columnIndexOfAiConfidence).toFloat()
          val _tmpNextReviewDate: Long
          _tmpNextReviewDate = _stmt.getLong(_columnIndexOfNextReviewDate)
          val _tmpAdaptationNotes: String?
          if (_stmt.isNull(_columnIndexOfAdaptationNotes)) {
            _tmpAdaptationNotes = null
          } else {
            _tmpAdaptationNotes = _stmt.getText(_columnIndexOfAdaptationNotes)
          }
          _item =
              ExerciseProgressionEntity(_tmpId,_tmpExerciseId,_tmpUserId,_tmpCurrentWeight,_tmpRecommendedWeight,_tmpCurrentReps,_tmpRecommendedReps,_tmpProgressionReason,_tmpPerformanceTrend,_tmpPlateauDetected,_tmpPlateauWeeks,_tmpLastProgressDate,_tmpAiConfidence,_tmpNextReviewDate,_tmpAdaptationNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getByUserIdFlow(userId: String): Flow<List<ExerciseProgressionEntity>> {
    val _sql: String =
        "SELECT * FROM exercise_progressions WHERE userId = ? ORDER BY lastProgressDate DESC"
    return createFlow(__db, false, arrayOf("exercise_progressions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfRecommendedWeight: Int = getColumnIndexOrThrow(_stmt, "recommendedWeight")
        val _columnIndexOfCurrentReps: Int = getColumnIndexOrThrow(_stmt, "currentReps")
        val _columnIndexOfRecommendedReps: Int = getColumnIndexOrThrow(_stmt, "recommendedReps")
        val _columnIndexOfProgressionReason: Int = getColumnIndexOrThrow(_stmt, "progressionReason")
        val _columnIndexOfPerformanceTrend: Int = getColumnIndexOrThrow(_stmt, "performanceTrend")
        val _columnIndexOfPlateauDetected: Int = getColumnIndexOrThrow(_stmt, "plateauDetected")
        val _columnIndexOfPlateauWeeks: Int = getColumnIndexOrThrow(_stmt, "plateauWeeks")
        val _columnIndexOfLastProgressDate: Int = getColumnIndexOrThrow(_stmt, "lastProgressDate")
        val _columnIndexOfAiConfidence: Int = getColumnIndexOrThrow(_stmt, "aiConfidence")
        val _columnIndexOfNextReviewDate: Int = getColumnIndexOrThrow(_stmt, "nextReviewDate")
        val _columnIndexOfAdaptationNotes: Int = getColumnIndexOrThrow(_stmt, "adaptationNotes")
        val _result: MutableList<ExerciseProgressionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseProgressionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpRecommendedWeight: Float
          _tmpRecommendedWeight = _stmt.getDouble(_columnIndexOfRecommendedWeight).toFloat()
          val _tmpCurrentReps: Int
          _tmpCurrentReps = _stmt.getLong(_columnIndexOfCurrentReps).toInt()
          val _tmpRecommendedReps: Int
          _tmpRecommendedReps = _stmt.getLong(_columnIndexOfRecommendedReps).toInt()
          val _tmpProgressionReason: String
          _tmpProgressionReason = _stmt.getText(_columnIndexOfProgressionReason)
          val _tmpPerformanceTrend: String
          _tmpPerformanceTrend = _stmt.getText(_columnIndexOfPerformanceTrend)
          val _tmpPlateauDetected: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPlateauDetected).toInt()
          _tmpPlateauDetected = _tmp != 0
          val _tmpPlateauWeeks: Int
          _tmpPlateauWeeks = _stmt.getLong(_columnIndexOfPlateauWeeks).toInt()
          val _tmpLastProgressDate: Long
          _tmpLastProgressDate = _stmt.getLong(_columnIndexOfLastProgressDate)
          val _tmpAiConfidence: Float
          _tmpAiConfidence = _stmt.getDouble(_columnIndexOfAiConfidence).toFloat()
          val _tmpNextReviewDate: Long
          _tmpNextReviewDate = _stmt.getLong(_columnIndexOfNextReviewDate)
          val _tmpAdaptationNotes: String?
          if (_stmt.isNull(_columnIndexOfAdaptationNotes)) {
            _tmpAdaptationNotes = null
          } else {
            _tmpAdaptationNotes = _stmt.getText(_columnIndexOfAdaptationNotes)
          }
          _item =
              ExerciseProgressionEntity(_tmpId,_tmpExerciseId,_tmpUserId,_tmpCurrentWeight,_tmpRecommendedWeight,_tmpCurrentReps,_tmpRecommendedReps,_tmpProgressionReason,_tmpPerformanceTrend,_tmpPlateauDetected,_tmpPlateauWeeks,_tmpLastProgressDate,_tmpAiConfidence,_tmpNextReviewDate,_tmpAdaptationNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlateauedExercises(userId: String):
      List<ExerciseProgressionEntity> {
    val _sql: String =
        "SELECT * FROM exercise_progressions WHERE plateauDetected = 1 AND userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfRecommendedWeight: Int = getColumnIndexOrThrow(_stmt, "recommendedWeight")
        val _columnIndexOfCurrentReps: Int = getColumnIndexOrThrow(_stmt, "currentReps")
        val _columnIndexOfRecommendedReps: Int = getColumnIndexOrThrow(_stmt, "recommendedReps")
        val _columnIndexOfProgressionReason: Int = getColumnIndexOrThrow(_stmt, "progressionReason")
        val _columnIndexOfPerformanceTrend: Int = getColumnIndexOrThrow(_stmt, "performanceTrend")
        val _columnIndexOfPlateauDetected: Int = getColumnIndexOrThrow(_stmt, "plateauDetected")
        val _columnIndexOfPlateauWeeks: Int = getColumnIndexOrThrow(_stmt, "plateauWeeks")
        val _columnIndexOfLastProgressDate: Int = getColumnIndexOrThrow(_stmt, "lastProgressDate")
        val _columnIndexOfAiConfidence: Int = getColumnIndexOrThrow(_stmt, "aiConfidence")
        val _columnIndexOfNextReviewDate: Int = getColumnIndexOrThrow(_stmt, "nextReviewDate")
        val _columnIndexOfAdaptationNotes: Int = getColumnIndexOrThrow(_stmt, "adaptationNotes")
        val _result: MutableList<ExerciseProgressionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseProgressionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpRecommendedWeight: Float
          _tmpRecommendedWeight = _stmt.getDouble(_columnIndexOfRecommendedWeight).toFloat()
          val _tmpCurrentReps: Int
          _tmpCurrentReps = _stmt.getLong(_columnIndexOfCurrentReps).toInt()
          val _tmpRecommendedReps: Int
          _tmpRecommendedReps = _stmt.getLong(_columnIndexOfRecommendedReps).toInt()
          val _tmpProgressionReason: String
          _tmpProgressionReason = _stmt.getText(_columnIndexOfProgressionReason)
          val _tmpPerformanceTrend: String
          _tmpPerformanceTrend = _stmt.getText(_columnIndexOfPerformanceTrend)
          val _tmpPlateauDetected: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPlateauDetected).toInt()
          _tmpPlateauDetected = _tmp != 0
          val _tmpPlateauWeeks: Int
          _tmpPlateauWeeks = _stmt.getLong(_columnIndexOfPlateauWeeks).toInt()
          val _tmpLastProgressDate: Long
          _tmpLastProgressDate = _stmt.getLong(_columnIndexOfLastProgressDate)
          val _tmpAiConfidence: Float
          _tmpAiConfidence = _stmt.getDouble(_columnIndexOfAiConfidence).toFloat()
          val _tmpNextReviewDate: Long
          _tmpNextReviewDate = _stmt.getLong(_columnIndexOfNextReviewDate)
          val _tmpAdaptationNotes: String?
          if (_stmt.isNull(_columnIndexOfAdaptationNotes)) {
            _tmpAdaptationNotes = null
          } else {
            _tmpAdaptationNotes = _stmt.getText(_columnIndexOfAdaptationNotes)
          }
          _item =
              ExerciseProgressionEntity(_tmpId,_tmpExerciseId,_tmpUserId,_tmpCurrentWeight,_tmpRecommendedWeight,_tmpCurrentReps,_tmpRecommendedReps,_tmpProgressionReason,_tmpPerformanceTrend,_tmpPlateauDetected,_tmpPlateauWeeks,_tmpLastProgressDate,_tmpAiConfidence,_tmpNextReviewDate,_tmpAdaptationNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByPerformanceTrend(trend: String, userId: String):
      List<ExerciseProgressionEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM exercise_progressions 
        |        WHERE performanceTrend = ? AND userId = ? 
        |        ORDER BY lastProgressDate DESC
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, trend)
        _argIndex = 2
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfRecommendedWeight: Int = getColumnIndexOrThrow(_stmt, "recommendedWeight")
        val _columnIndexOfCurrentReps: Int = getColumnIndexOrThrow(_stmt, "currentReps")
        val _columnIndexOfRecommendedReps: Int = getColumnIndexOrThrow(_stmt, "recommendedReps")
        val _columnIndexOfProgressionReason: Int = getColumnIndexOrThrow(_stmt, "progressionReason")
        val _columnIndexOfPerformanceTrend: Int = getColumnIndexOrThrow(_stmt, "performanceTrend")
        val _columnIndexOfPlateauDetected: Int = getColumnIndexOrThrow(_stmt, "plateauDetected")
        val _columnIndexOfPlateauWeeks: Int = getColumnIndexOrThrow(_stmt, "plateauWeeks")
        val _columnIndexOfLastProgressDate: Int = getColumnIndexOrThrow(_stmt, "lastProgressDate")
        val _columnIndexOfAiConfidence: Int = getColumnIndexOrThrow(_stmt, "aiConfidence")
        val _columnIndexOfNextReviewDate: Int = getColumnIndexOrThrow(_stmt, "nextReviewDate")
        val _columnIndexOfAdaptationNotes: Int = getColumnIndexOrThrow(_stmt, "adaptationNotes")
        val _result: MutableList<ExerciseProgressionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseProgressionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpRecommendedWeight: Float
          _tmpRecommendedWeight = _stmt.getDouble(_columnIndexOfRecommendedWeight).toFloat()
          val _tmpCurrentReps: Int
          _tmpCurrentReps = _stmt.getLong(_columnIndexOfCurrentReps).toInt()
          val _tmpRecommendedReps: Int
          _tmpRecommendedReps = _stmt.getLong(_columnIndexOfRecommendedReps).toInt()
          val _tmpProgressionReason: String
          _tmpProgressionReason = _stmt.getText(_columnIndexOfProgressionReason)
          val _tmpPerformanceTrend: String
          _tmpPerformanceTrend = _stmt.getText(_columnIndexOfPerformanceTrend)
          val _tmpPlateauDetected: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPlateauDetected).toInt()
          _tmpPlateauDetected = _tmp != 0
          val _tmpPlateauWeeks: Int
          _tmpPlateauWeeks = _stmt.getLong(_columnIndexOfPlateauWeeks).toInt()
          val _tmpLastProgressDate: Long
          _tmpLastProgressDate = _stmt.getLong(_columnIndexOfLastProgressDate)
          val _tmpAiConfidence: Float
          _tmpAiConfidence = _stmt.getDouble(_columnIndexOfAiConfidence).toFloat()
          val _tmpNextReviewDate: Long
          _tmpNextReviewDate = _stmt.getLong(_columnIndexOfNextReviewDate)
          val _tmpAdaptationNotes: String?
          if (_stmt.isNull(_columnIndexOfAdaptationNotes)) {
            _tmpAdaptationNotes = null
          } else {
            _tmpAdaptationNotes = _stmt.getText(_columnIndexOfAdaptationNotes)
          }
          _item =
              ExerciseProgressionEntity(_tmpId,_tmpExerciseId,_tmpUserId,_tmpCurrentWeight,_tmpRecommendedWeight,_tmpCurrentReps,_tmpRecommendedReps,_tmpProgressionReason,_tmpPerformanceTrend,_tmpPlateauDetected,_tmpPlateauWeeks,_tmpLastProgressDate,_tmpAiConfidence,_tmpNextReviewDate,_tmpAdaptationNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExercisesDueForReview(currentTimestamp: Long, userId: String):
      List<ExerciseProgressionEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM exercise_progressions 
        |        WHERE nextReviewDate <= ? AND userId = ?
        |        ORDER BY nextReviewDate
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, currentTimestamp)
        _argIndex = 2
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfRecommendedWeight: Int = getColumnIndexOrThrow(_stmt, "recommendedWeight")
        val _columnIndexOfCurrentReps: Int = getColumnIndexOrThrow(_stmt, "currentReps")
        val _columnIndexOfRecommendedReps: Int = getColumnIndexOrThrow(_stmt, "recommendedReps")
        val _columnIndexOfProgressionReason: Int = getColumnIndexOrThrow(_stmt, "progressionReason")
        val _columnIndexOfPerformanceTrend: Int = getColumnIndexOrThrow(_stmt, "performanceTrend")
        val _columnIndexOfPlateauDetected: Int = getColumnIndexOrThrow(_stmt, "plateauDetected")
        val _columnIndexOfPlateauWeeks: Int = getColumnIndexOrThrow(_stmt, "plateauWeeks")
        val _columnIndexOfLastProgressDate: Int = getColumnIndexOrThrow(_stmt, "lastProgressDate")
        val _columnIndexOfAiConfidence: Int = getColumnIndexOrThrow(_stmt, "aiConfidence")
        val _columnIndexOfNextReviewDate: Int = getColumnIndexOrThrow(_stmt, "nextReviewDate")
        val _columnIndexOfAdaptationNotes: Int = getColumnIndexOrThrow(_stmt, "adaptationNotes")
        val _result: MutableList<ExerciseProgressionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseProgressionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpRecommendedWeight: Float
          _tmpRecommendedWeight = _stmt.getDouble(_columnIndexOfRecommendedWeight).toFloat()
          val _tmpCurrentReps: Int
          _tmpCurrentReps = _stmt.getLong(_columnIndexOfCurrentReps).toInt()
          val _tmpRecommendedReps: Int
          _tmpRecommendedReps = _stmt.getLong(_columnIndexOfRecommendedReps).toInt()
          val _tmpProgressionReason: String
          _tmpProgressionReason = _stmt.getText(_columnIndexOfProgressionReason)
          val _tmpPerformanceTrend: String
          _tmpPerformanceTrend = _stmt.getText(_columnIndexOfPerformanceTrend)
          val _tmpPlateauDetected: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPlateauDetected).toInt()
          _tmpPlateauDetected = _tmp != 0
          val _tmpPlateauWeeks: Int
          _tmpPlateauWeeks = _stmt.getLong(_columnIndexOfPlateauWeeks).toInt()
          val _tmpLastProgressDate: Long
          _tmpLastProgressDate = _stmt.getLong(_columnIndexOfLastProgressDate)
          val _tmpAiConfidence: Float
          _tmpAiConfidence = _stmt.getDouble(_columnIndexOfAiConfidence).toFloat()
          val _tmpNextReviewDate: Long
          _tmpNextReviewDate = _stmt.getLong(_columnIndexOfNextReviewDate)
          val _tmpAdaptationNotes: String?
          if (_stmt.isNull(_columnIndexOfAdaptationNotes)) {
            _tmpAdaptationNotes = null
          } else {
            _tmpAdaptationNotes = _stmt.getText(_columnIndexOfAdaptationNotes)
          }
          _item =
              ExerciseProgressionEntity(_tmpId,_tmpExerciseId,_tmpUserId,_tmpCurrentWeight,_tmpRecommendedWeight,_tmpCurrentReps,_tmpRecommendedReps,_tmpProgressionReason,_tmpPerformanceTrend,_tmpPlateauDetected,_tmpPlateauWeeks,_tmpLastProgressDate,_tmpAiConfidence,_tmpNextReviewDate,_tmpAdaptationNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImprovingExercisesCount(userId: String): Int {
    val _sql: String = """
        |
        |        SELECT COUNT(*) FROM exercise_progressions 
        |        WHERE performanceTrend = 'improving' AND userId = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
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

  public override suspend fun getAverageAIConfidence(userId: String, sinceTimestamp: Long): Float? {
    val _sql: String = """
        |
        |        SELECT AVG(aiConfidence) FROM exercise_progressions 
        |        WHERE userId = ? AND lastProgressDate >= ?
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

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM exercise_progressions WHERE id = ?"
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
    val _sql: String = "DELETE FROM exercise_progressions"
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
