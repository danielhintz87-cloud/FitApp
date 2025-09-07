package com.example.fitapp.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Float
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GoalDao_Impl(
  __db: RoomDatabase,
) : GoalDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDailyGoalEntity: EntityInsertAdapter<DailyGoalEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDailyGoalEntity = object : EntityInsertAdapter<DailyGoalEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `daily_goals` (`dateIso`,`targetKcal`,`targetCarbs`,`targetProtein`,`targetFat`,`targetWaterMl`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DailyGoalEntity) {
        statement.bindText(1, entity.dateIso)
        statement.bindLong(2, entity.targetKcal.toLong())
        val _tmpTargetCarbs: Float? = entity.targetCarbs
        if (_tmpTargetCarbs == null) {
          statement.bindNull(3)
        } else {
          statement.bindDouble(3, _tmpTargetCarbs.toDouble())
        }
        val _tmpTargetProtein: Float? = entity.targetProtein
        if (_tmpTargetProtein == null) {
          statement.bindNull(4)
        } else {
          statement.bindDouble(4, _tmpTargetProtein.toDouble())
        }
        val _tmpTargetFat: Float? = entity.targetFat
        if (_tmpTargetFat == null) {
          statement.bindNull(5)
        } else {
          statement.bindDouble(5, _tmpTargetFat.toDouble())
        }
        val _tmpTargetWaterMl: Int? = entity.targetWaterMl
        if (_tmpTargetWaterMl == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpTargetWaterMl.toLong())
        }
      }
    }
  }

  public override suspend fun upsert(goal: DailyGoalEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfDailyGoalEntity.insert(_connection, goal)
  }

  public override fun goalFlow(dateIso: String): Flow<DailyGoalEntity?> {
    val _sql: String = "SELECT * FROM daily_goals WHERE dateIso = ?"
    return createFlow(__db, false, arrayOf("daily_goals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, dateIso)
        val _columnIndexOfDateIso: Int = getColumnIndexOrThrow(_stmt, "dateIso")
        val _columnIndexOfTargetKcal: Int = getColumnIndexOrThrow(_stmt, "targetKcal")
        val _columnIndexOfTargetCarbs: Int = getColumnIndexOrThrow(_stmt, "targetCarbs")
        val _columnIndexOfTargetProtein: Int = getColumnIndexOrThrow(_stmt, "targetProtein")
        val _columnIndexOfTargetFat: Int = getColumnIndexOrThrow(_stmt, "targetFat")
        val _columnIndexOfTargetWaterMl: Int = getColumnIndexOrThrow(_stmt, "targetWaterMl")
        val _result: DailyGoalEntity?
        if (_stmt.step()) {
          val _tmpDateIso: String
          _tmpDateIso = _stmt.getText(_columnIndexOfDateIso)
          val _tmpTargetKcal: Int
          _tmpTargetKcal = _stmt.getLong(_columnIndexOfTargetKcal).toInt()
          val _tmpTargetCarbs: Float?
          if (_stmt.isNull(_columnIndexOfTargetCarbs)) {
            _tmpTargetCarbs = null
          } else {
            _tmpTargetCarbs = _stmt.getDouble(_columnIndexOfTargetCarbs).toFloat()
          }
          val _tmpTargetProtein: Float?
          if (_stmt.isNull(_columnIndexOfTargetProtein)) {
            _tmpTargetProtein = null
          } else {
            _tmpTargetProtein = _stmt.getDouble(_columnIndexOfTargetProtein).toFloat()
          }
          val _tmpTargetFat: Float?
          if (_stmt.isNull(_columnIndexOfTargetFat)) {
            _tmpTargetFat = null
          } else {
            _tmpTargetFat = _stmt.getDouble(_columnIndexOfTargetFat).toFloat()
          }
          val _tmpTargetWaterMl: Int?
          if (_stmt.isNull(_columnIndexOfTargetWaterMl)) {
            _tmpTargetWaterMl = null
          } else {
            _tmpTargetWaterMl = _stmt.getLong(_columnIndexOfTargetWaterMl).toInt()
          }
          _result =
              DailyGoalEntity(_tmpDateIso,_tmpTargetKcal,_tmpTargetCarbs,_tmpTargetProtein,_tmpTargetFat,_tmpTargetWaterMl)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
