package com.example.fitapp.`data`.db

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
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PlanDao_Impl(
  __db: RoomDatabase,
) : PlanDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPlanEntity: EntityInsertAdapter<PlanEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPlanEntity = object : EntityInsertAdapter<PlanEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `training_plans` (`id`,`title`,`content`,`goal`,`weeks`,`sessionsPerWeek`,`minutesPerSession`,`equipment`,`trainingDays`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlanEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        statement.bindText(4, entity.goal)
        statement.bindLong(5, entity.weeks.toLong())
        statement.bindLong(6, entity.sessionsPerWeek.toLong())
        statement.bindLong(7, entity.minutesPerSession.toLong())
        statement.bindText(8, entity.equipment)
        val _tmpTrainingDays: String? = entity.trainingDays
        if (_tmpTrainingDays == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpTrainingDays)
        }
        statement.bindLong(10, entity.createdAt)
      }
    }
  }

  public override suspend fun insert(plan: PlanEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfPlanEntity.insertAndReturnId(_connection, plan)
    _result
  }

  public override fun plansFlow(): Flow<List<PlanEntity>> {
    val _sql: String =
        "SELECT `training_plans`.`id` AS `id`, `training_plans`.`title` AS `title`, `training_plans`.`content` AS `content`, `training_plans`.`goal` AS `goal`, `training_plans`.`weeks` AS `weeks`, `training_plans`.`sessionsPerWeek` AS `sessionsPerWeek`, `training_plans`.`minutesPerSession` AS `minutesPerSession`, `training_plans`.`equipment` AS `equipment`, `training_plans`.`trainingDays` AS `trainingDays`, `training_plans`.`createdAt` AS `createdAt` FROM training_plans ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("training_plans")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfContent: Int = 2
        val _columnIndexOfGoal: Int = 3
        val _columnIndexOfWeeks: Int = 4
        val _columnIndexOfSessionsPerWeek: Int = 5
        val _columnIndexOfMinutesPerSession: Int = 6
        val _columnIndexOfEquipment: Int = 7
        val _columnIndexOfTrainingDays: Int = 8
        val _columnIndexOfCreatedAt: Int = 9
        val _result: MutableList<PlanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlanEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpGoal: String
          _tmpGoal = _stmt.getText(_columnIndexOfGoal)
          val _tmpWeeks: Int
          _tmpWeeks = _stmt.getLong(_columnIndexOfWeeks).toInt()
          val _tmpSessionsPerWeek: Int
          _tmpSessionsPerWeek = _stmt.getLong(_columnIndexOfSessionsPerWeek).toInt()
          val _tmpMinutesPerSession: Int
          _tmpMinutesPerSession = _stmt.getLong(_columnIndexOfMinutesPerSession).toInt()
          val _tmpEquipment: String
          _tmpEquipment = _stmt.getText(_columnIndexOfEquipment)
          val _tmpTrainingDays: String?
          if (_stmt.isNull(_columnIndexOfTrainingDays)) {
            _tmpTrainingDays = null
          } else {
            _tmpTrainingDays = _stmt.getText(_columnIndexOfTrainingDays)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              PlanEntity(_tmpId,_tmpTitle,_tmpContent,_tmpGoal,_tmpWeeks,_tmpSessionsPerWeek,_tmpMinutesPerSession,_tmpEquipment,_tmpTrainingDays,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlan(id: Long): PlanEntity? {
    val _sql: String = "SELECT * FROM training_plans WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfGoal: Int = getColumnIndexOrThrow(_stmt, "goal")
        val _columnIndexOfWeeks: Int = getColumnIndexOrThrow(_stmt, "weeks")
        val _columnIndexOfSessionsPerWeek: Int = getColumnIndexOrThrow(_stmt, "sessionsPerWeek")
        val _columnIndexOfMinutesPerSession: Int = getColumnIndexOrThrow(_stmt, "minutesPerSession")
        val _columnIndexOfEquipment: Int = getColumnIndexOrThrow(_stmt, "equipment")
        val _columnIndexOfTrainingDays: Int = getColumnIndexOrThrow(_stmt, "trainingDays")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: PlanEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpGoal: String
          _tmpGoal = _stmt.getText(_columnIndexOfGoal)
          val _tmpWeeks: Int
          _tmpWeeks = _stmt.getLong(_columnIndexOfWeeks).toInt()
          val _tmpSessionsPerWeek: Int
          _tmpSessionsPerWeek = _stmt.getLong(_columnIndexOfSessionsPerWeek).toInt()
          val _tmpMinutesPerSession: Int
          _tmpMinutesPerSession = _stmt.getLong(_columnIndexOfMinutesPerSession).toInt()
          val _tmpEquipment: String
          _tmpEquipment = _stmt.getText(_columnIndexOfEquipment)
          val _tmpTrainingDays: String?
          if (_stmt.isNull(_columnIndexOfTrainingDays)) {
            _tmpTrainingDays = null
          } else {
            _tmpTrainingDays = _stmt.getText(_columnIndexOfTrainingDays)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              PlanEntity(_tmpId,_tmpTitle,_tmpContent,_tmpGoal,_tmpWeeks,_tmpSessionsPerWeek,_tmpMinutesPerSession,_tmpEquipment,_tmpTrainingDays,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLatestPlan(): PlanEntity? {
    val _sql: String =
        "SELECT `training_plans`.`id` AS `id`, `training_plans`.`title` AS `title`, `training_plans`.`content` AS `content`, `training_plans`.`goal` AS `goal`, `training_plans`.`weeks` AS `weeks`, `training_plans`.`sessionsPerWeek` AS `sessionsPerWeek`, `training_plans`.`minutesPerSession` AS `minutesPerSession`, `training_plans`.`equipment` AS `equipment`, `training_plans`.`trainingDays` AS `trainingDays`, `training_plans`.`createdAt` AS `createdAt` FROM training_plans ORDER BY createdAt DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfContent: Int = 2
        val _columnIndexOfGoal: Int = 3
        val _columnIndexOfWeeks: Int = 4
        val _columnIndexOfSessionsPerWeek: Int = 5
        val _columnIndexOfMinutesPerSession: Int = 6
        val _columnIndexOfEquipment: Int = 7
        val _columnIndexOfTrainingDays: Int = 8
        val _columnIndexOfCreatedAt: Int = 9
        val _result: PlanEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpGoal: String
          _tmpGoal = _stmt.getText(_columnIndexOfGoal)
          val _tmpWeeks: Int
          _tmpWeeks = _stmt.getLong(_columnIndexOfWeeks).toInt()
          val _tmpSessionsPerWeek: Int
          _tmpSessionsPerWeek = _stmt.getLong(_columnIndexOfSessionsPerWeek).toInt()
          val _tmpMinutesPerSession: Int
          _tmpMinutesPerSession = _stmt.getLong(_columnIndexOfMinutesPerSession).toInt()
          val _tmpEquipment: String
          _tmpEquipment = _stmt.getText(_columnIndexOfEquipment)
          val _tmpTrainingDays: String?
          if (_stmt.isNull(_columnIndexOfTrainingDays)) {
            _tmpTrainingDays = null
          } else {
            _tmpTrainingDays = _stmt.getText(_columnIndexOfTrainingDays)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              PlanEntity(_tmpId,_tmpTitle,_tmpContent,_tmpGoal,_tmpWeeks,_tmpSessionsPerWeek,_tmpMinutesPerSession,_tmpEquipment,_tmpTrainingDays,_tmpCreatedAt)
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
    val _sql: String = "DELETE FROM training_plans WHERE id = ?"
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

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM training_plans"
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
