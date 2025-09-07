package com.example.fitapp.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TodayWorkoutDao_Impl(
  __db: RoomDatabase,
) : TodayWorkoutDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTodayWorkoutEntity: EntityInsertAdapter<TodayWorkoutEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTodayWorkoutEntity = object : EntityInsertAdapter<TodayWorkoutEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `today_workouts` (`dateIso`,`content`,`status`,`createdAt`,`completedAt`,`planId`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TodayWorkoutEntity) {
        statement.bindText(1, entity.dateIso)
        statement.bindText(2, entity.content)
        statement.bindText(3, entity.status)
        statement.bindLong(4, entity.createdAt)
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpCompletedAt)
        }
        val _tmpPlanId: Long? = entity.planId
        if (_tmpPlanId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpPlanId)
        }
      }
    }
  }

  public override suspend fun upsert(workout: TodayWorkoutEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfTodayWorkoutEntity.insert(_connection, workout)
  }

  public override suspend fun getByDate(dateIso: String): TodayWorkoutEntity? {
    val _sql: String = "SELECT * FROM today_workouts WHERE dateIso = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, dateIso)
        val _columnIndexOfDateIso: Int = getColumnIndexOrThrow(_stmt, "dateIso")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _result: TodayWorkoutEntity?
        if (_stmt.step()) {
          val _tmpDateIso: String
          _tmpDateIso = _stmt.getText(_columnIndexOfDateIso)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpPlanId: Long?
          if (_stmt.isNull(_columnIndexOfPlanId)) {
            _tmpPlanId = null
          } else {
            _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          }
          _result =
              TodayWorkoutEntity(_tmpDateIso,_tmpContent,_tmpStatus,_tmpCreatedAt,_tmpCompletedAt,_tmpPlanId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBetween(fromIso: String, toIso: String): List<TodayWorkoutEntity> {
    val _sql: String =
        "SELECT * FROM today_workouts WHERE dateIso BETWEEN ? AND ? ORDER BY dateIso DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, fromIso)
        _argIndex = 2
        _stmt.bindText(_argIndex, toIso)
        val _columnIndexOfDateIso: Int = getColumnIndexOrThrow(_stmt, "dateIso")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _result: MutableList<TodayWorkoutEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TodayWorkoutEntity
          val _tmpDateIso: String
          _tmpDateIso = _stmt.getText(_columnIndexOfDateIso)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpPlanId: Long?
          if (_stmt.isNull(_columnIndexOfPlanId)) {
            _tmpPlanId = null
          } else {
            _tmpPlanId = _stmt.getLong(_columnIndexOfPlanId)
          }
          _item =
              TodayWorkoutEntity(_tmpDateIso,_tmpContent,_tmpStatus,_tmpCreatedAt,_tmpCompletedAt,_tmpPlanId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setStatus(
    dateIso: String,
    status: String,
    completedAt: Long?,
  ) {
    val _sql: String = "UPDATE today_workouts SET status = ?, completedAt = ? WHERE dateIso = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        if (completedAt == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, completedAt)
        }
        _argIndex = 3
        _stmt.bindText(_argIndex, dateIso)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM today_workouts"
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
