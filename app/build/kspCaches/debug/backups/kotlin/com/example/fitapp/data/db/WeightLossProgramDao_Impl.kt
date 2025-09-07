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
public class WeightLossProgramDao_Impl(
  __db: RoomDatabase,
) : WeightLossProgramDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWeightLossProgramEntity: EntityInsertAdapter<WeightLossProgramEntity>

  private val __updateAdapterOfWeightLossProgramEntity:
      EntityDeleteOrUpdateAdapter<WeightLossProgramEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWeightLossProgramEntity = object :
        EntityInsertAdapter<WeightLossProgramEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `weight_loss_programs` (`id`,`startDate`,`endDate`,`startWeight`,`targetWeight`,`currentWeight`,`dailyCalorieTarget`,`weeklyWeightLossGoal`,`isActive`,`programType`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WeightLossProgramEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.startDate)
        val _tmpEndDate: String? = entity.endDate
        if (_tmpEndDate == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpEndDate)
        }
        statement.bindDouble(4, entity.startWeight.toDouble())
        statement.bindDouble(5, entity.targetWeight.toDouble())
        statement.bindDouble(6, entity.currentWeight.toDouble())
        statement.bindLong(7, entity.dailyCalorieTarget.toLong())
        statement.bindDouble(8, entity.weeklyWeightLossGoal.toDouble())
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindText(10, entity.programType)
        statement.bindLong(11, entity.createdAt)
      }
    }
    this.__updateAdapterOfWeightLossProgramEntity = object :
        EntityDeleteOrUpdateAdapter<WeightLossProgramEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `weight_loss_programs` SET `id` = ?,`startDate` = ?,`endDate` = ?,`startWeight` = ?,`targetWeight` = ?,`currentWeight` = ?,`dailyCalorieTarget` = ?,`weeklyWeightLossGoal` = ?,`isActive` = ?,`programType` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WeightLossProgramEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.startDate)
        val _tmpEndDate: String? = entity.endDate
        if (_tmpEndDate == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpEndDate)
        }
        statement.bindDouble(4, entity.startWeight.toDouble())
        statement.bindDouble(5, entity.targetWeight.toDouble())
        statement.bindDouble(6, entity.currentWeight.toDouble())
        statement.bindLong(7, entity.dailyCalorieTarget.toLong())
        statement.bindDouble(8, entity.weeklyWeightLossGoal.toDouble())
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindText(10, entity.programType)
        statement.bindLong(11, entity.createdAt)
        statement.bindLong(12, entity.id)
      }
    }
  }

  public override suspend fun insert(program: WeightLossProgramEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfWeightLossProgramEntity.insertAndReturnId(_connection,
        program)
    _result
  }

  public override suspend fun update(program: WeightLossProgramEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfWeightLossProgramEntity.handle(_connection, program)
  }

  public override suspend fun getById(id: Long): WeightLossProgramEntity? {
    val _sql: String = "SELECT * FROM weight_loss_programs WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfStartWeight: Int = getColumnIndexOrThrow(_stmt, "startWeight")
        val _columnIndexOfTargetWeight: Int = getColumnIndexOrThrow(_stmt, "targetWeight")
        val _columnIndexOfCurrentWeight: Int = getColumnIndexOrThrow(_stmt, "currentWeight")
        val _columnIndexOfDailyCalorieTarget: Int = getColumnIndexOrThrow(_stmt,
            "dailyCalorieTarget")
        val _columnIndexOfWeeklyWeightLossGoal: Int = getColumnIndexOrThrow(_stmt,
            "weeklyWeightLossGoal")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfProgramType: Int = getColumnIndexOrThrow(_stmt, "programType")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: WeightLossProgramEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmpEndDate = null
          } else {
            _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          }
          val _tmpStartWeight: Float
          _tmpStartWeight = _stmt.getDouble(_columnIndexOfStartWeight).toFloat()
          val _tmpTargetWeight: Float
          _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight).toFloat()
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpDailyCalorieTarget: Int
          _tmpDailyCalorieTarget = _stmt.getLong(_columnIndexOfDailyCalorieTarget).toInt()
          val _tmpWeeklyWeightLossGoal: Float
          _tmpWeeklyWeightLossGoal = _stmt.getDouble(_columnIndexOfWeeklyWeightLossGoal).toFloat()
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpProgramType: String
          _tmpProgramType = _stmt.getText(_columnIndexOfProgramType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              WeightLossProgramEntity(_tmpId,_tmpStartDate,_tmpEndDate,_tmpStartWeight,_tmpTargetWeight,_tmpCurrentWeight,_tmpDailyCalorieTarget,_tmpWeeklyWeightLossGoal,_tmpIsActive,_tmpProgramType,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveProgram(): WeightLossProgramEntity? {
    val _sql: String =
        "SELECT `weight_loss_programs`.`id` AS `id`, `weight_loss_programs`.`startDate` AS `startDate`, `weight_loss_programs`.`endDate` AS `endDate`, `weight_loss_programs`.`startWeight` AS `startWeight`, `weight_loss_programs`.`targetWeight` AS `targetWeight`, `weight_loss_programs`.`currentWeight` AS `currentWeight`, `weight_loss_programs`.`dailyCalorieTarget` AS `dailyCalorieTarget`, `weight_loss_programs`.`weeklyWeightLossGoal` AS `weeklyWeightLossGoal`, `weight_loss_programs`.`isActive` AS `isActive`, `weight_loss_programs`.`programType` AS `programType`, `weight_loss_programs`.`createdAt` AS `createdAt` FROM weight_loss_programs WHERE isActive = 1 ORDER BY startDate DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfStartDate: Int = 1
        val _columnIndexOfEndDate: Int = 2
        val _columnIndexOfStartWeight: Int = 3
        val _columnIndexOfTargetWeight: Int = 4
        val _columnIndexOfCurrentWeight: Int = 5
        val _columnIndexOfDailyCalorieTarget: Int = 6
        val _columnIndexOfWeeklyWeightLossGoal: Int = 7
        val _columnIndexOfIsActive: Int = 8
        val _columnIndexOfProgramType: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: WeightLossProgramEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmpEndDate = null
          } else {
            _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          }
          val _tmpStartWeight: Float
          _tmpStartWeight = _stmt.getDouble(_columnIndexOfStartWeight).toFloat()
          val _tmpTargetWeight: Float
          _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight).toFloat()
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpDailyCalorieTarget: Int
          _tmpDailyCalorieTarget = _stmt.getLong(_columnIndexOfDailyCalorieTarget).toInt()
          val _tmpWeeklyWeightLossGoal: Float
          _tmpWeeklyWeightLossGoal = _stmt.getDouble(_columnIndexOfWeeklyWeightLossGoal).toFloat()
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpProgramType: String
          _tmpProgramType = _stmt.getText(_columnIndexOfProgramType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              WeightLossProgramEntity(_tmpId,_tmpStartDate,_tmpEndDate,_tmpStartWeight,_tmpTargetWeight,_tmpCurrentWeight,_tmpDailyCalorieTarget,_tmpWeeklyWeightLossGoal,_tmpIsActive,_tmpProgramType,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getActiveProgramFlow(): Flow<WeightLossProgramEntity?> {
    val _sql: String =
        "SELECT `weight_loss_programs`.`id` AS `id`, `weight_loss_programs`.`startDate` AS `startDate`, `weight_loss_programs`.`endDate` AS `endDate`, `weight_loss_programs`.`startWeight` AS `startWeight`, `weight_loss_programs`.`targetWeight` AS `targetWeight`, `weight_loss_programs`.`currentWeight` AS `currentWeight`, `weight_loss_programs`.`dailyCalorieTarget` AS `dailyCalorieTarget`, `weight_loss_programs`.`weeklyWeightLossGoal` AS `weeklyWeightLossGoal`, `weight_loss_programs`.`isActive` AS `isActive`, `weight_loss_programs`.`programType` AS `programType`, `weight_loss_programs`.`createdAt` AS `createdAt` FROM weight_loss_programs WHERE isActive = 1 ORDER BY startDate DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("weight_loss_programs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfStartDate: Int = 1
        val _columnIndexOfEndDate: Int = 2
        val _columnIndexOfStartWeight: Int = 3
        val _columnIndexOfTargetWeight: Int = 4
        val _columnIndexOfCurrentWeight: Int = 5
        val _columnIndexOfDailyCalorieTarget: Int = 6
        val _columnIndexOfWeeklyWeightLossGoal: Int = 7
        val _columnIndexOfIsActive: Int = 8
        val _columnIndexOfProgramType: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: WeightLossProgramEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmpEndDate = null
          } else {
            _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          }
          val _tmpStartWeight: Float
          _tmpStartWeight = _stmt.getDouble(_columnIndexOfStartWeight).toFloat()
          val _tmpTargetWeight: Float
          _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight).toFloat()
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpDailyCalorieTarget: Int
          _tmpDailyCalorieTarget = _stmt.getLong(_columnIndexOfDailyCalorieTarget).toInt()
          val _tmpWeeklyWeightLossGoal: Float
          _tmpWeeklyWeightLossGoal = _stmt.getDouble(_columnIndexOfWeeklyWeightLossGoal).toFloat()
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpProgramType: String
          _tmpProgramType = _stmt.getText(_columnIndexOfProgramType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              WeightLossProgramEntity(_tmpId,_tmpStartDate,_tmpEndDate,_tmpStartWeight,_tmpTargetWeight,_tmpCurrentWeight,_tmpDailyCalorieTarget,_tmpWeeklyWeightLossGoal,_tmpIsActive,_tmpProgramType,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<WeightLossProgramEntity> {
    val _sql: String =
        "SELECT `weight_loss_programs`.`id` AS `id`, `weight_loss_programs`.`startDate` AS `startDate`, `weight_loss_programs`.`endDate` AS `endDate`, `weight_loss_programs`.`startWeight` AS `startWeight`, `weight_loss_programs`.`targetWeight` AS `targetWeight`, `weight_loss_programs`.`currentWeight` AS `currentWeight`, `weight_loss_programs`.`dailyCalorieTarget` AS `dailyCalorieTarget`, `weight_loss_programs`.`weeklyWeightLossGoal` AS `weeklyWeightLossGoal`, `weight_loss_programs`.`isActive` AS `isActive`, `weight_loss_programs`.`programType` AS `programType`, `weight_loss_programs`.`createdAt` AS `createdAt` FROM weight_loss_programs ORDER BY startDate DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfStartDate: Int = 1
        val _columnIndexOfEndDate: Int = 2
        val _columnIndexOfStartWeight: Int = 3
        val _columnIndexOfTargetWeight: Int = 4
        val _columnIndexOfCurrentWeight: Int = 5
        val _columnIndexOfDailyCalorieTarget: Int = 6
        val _columnIndexOfWeeklyWeightLossGoal: Int = 7
        val _columnIndexOfIsActive: Int = 8
        val _columnIndexOfProgramType: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: MutableList<WeightLossProgramEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WeightLossProgramEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmpEndDate = null
          } else {
            _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          }
          val _tmpStartWeight: Float
          _tmpStartWeight = _stmt.getDouble(_columnIndexOfStartWeight).toFloat()
          val _tmpTargetWeight: Float
          _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight).toFloat()
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpDailyCalorieTarget: Int
          _tmpDailyCalorieTarget = _stmt.getLong(_columnIndexOfDailyCalorieTarget).toInt()
          val _tmpWeeklyWeightLossGoal: Float
          _tmpWeeklyWeightLossGoal = _stmt.getDouble(_columnIndexOfWeeklyWeightLossGoal).toFloat()
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpProgramType: String
          _tmpProgramType = _stmt.getText(_columnIndexOfProgramType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              WeightLossProgramEntity(_tmpId,_tmpStartDate,_tmpEndDate,_tmpStartWeight,_tmpTargetWeight,_tmpCurrentWeight,_tmpDailyCalorieTarget,_tmpWeeklyWeightLossGoal,_tmpIsActive,_tmpProgramType,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllFlow(): Flow<List<WeightLossProgramEntity>> {
    val _sql: String =
        "SELECT `weight_loss_programs`.`id` AS `id`, `weight_loss_programs`.`startDate` AS `startDate`, `weight_loss_programs`.`endDate` AS `endDate`, `weight_loss_programs`.`startWeight` AS `startWeight`, `weight_loss_programs`.`targetWeight` AS `targetWeight`, `weight_loss_programs`.`currentWeight` AS `currentWeight`, `weight_loss_programs`.`dailyCalorieTarget` AS `dailyCalorieTarget`, `weight_loss_programs`.`weeklyWeightLossGoal` AS `weeklyWeightLossGoal`, `weight_loss_programs`.`isActive` AS `isActive`, `weight_loss_programs`.`programType` AS `programType`, `weight_loss_programs`.`createdAt` AS `createdAt` FROM weight_loss_programs ORDER BY startDate DESC"
    return createFlow(__db, false, arrayOf("weight_loss_programs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfStartDate: Int = 1
        val _columnIndexOfEndDate: Int = 2
        val _columnIndexOfStartWeight: Int = 3
        val _columnIndexOfTargetWeight: Int = 4
        val _columnIndexOfCurrentWeight: Int = 5
        val _columnIndexOfDailyCalorieTarget: Int = 6
        val _columnIndexOfWeeklyWeightLossGoal: Int = 7
        val _columnIndexOfIsActive: Int = 8
        val _columnIndexOfProgramType: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: MutableList<WeightLossProgramEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WeightLossProgramEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmpEndDate = null
          } else {
            _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          }
          val _tmpStartWeight: Float
          _tmpStartWeight = _stmt.getDouble(_columnIndexOfStartWeight).toFloat()
          val _tmpTargetWeight: Float
          _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight).toFloat()
          val _tmpCurrentWeight: Float
          _tmpCurrentWeight = _stmt.getDouble(_columnIndexOfCurrentWeight).toFloat()
          val _tmpDailyCalorieTarget: Int
          _tmpDailyCalorieTarget = _stmt.getLong(_columnIndexOfDailyCalorieTarget).toInt()
          val _tmpWeeklyWeightLossGoal: Float
          _tmpWeeklyWeightLossGoal = _stmt.getDouble(_columnIndexOfWeeklyWeightLossGoal).toFloat()
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpProgramType: String
          _tmpProgramType = _stmt.getText(_columnIndexOfProgramType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              WeightLossProgramEntity(_tmpId,_tmpStartDate,_tmpEndDate,_tmpStartWeight,_tmpTargetWeight,_tmpCurrentWeight,_tmpDailyCalorieTarget,_tmpWeeklyWeightLossGoal,_tmpIsActive,_tmpProgramType,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM weight_loss_programs WHERE id = ?"
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

  public override suspend fun deactivateAllPrograms() {
    val _sql: String = "UPDATE weight_loss_programs SET isActive = 0 WHERE isActive = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM weight_loss_programs"
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
