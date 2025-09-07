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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ProgressMilestoneDao_Impl(
  __db: RoomDatabase,
) : ProgressMilestoneDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfProgressMilestoneEntity: EntityInsertAdapter<ProgressMilestoneEntity>

  private val __updateAdapterOfProgressMilestoneEntity:
      EntityDeleteOrUpdateAdapter<ProgressMilestoneEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfProgressMilestoneEntity = object :
        EntityInsertAdapter<ProgressMilestoneEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `progress_milestones` (`id`,`title`,`description`,`category`,`targetValue`,`currentValue`,`unit`,`targetDate`,`isCompleted`,`completedAt`,`progress`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProgressMilestoneEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindDouble(5, entity.targetValue)
        statement.bindDouble(6, entity.currentValue)
        statement.bindText(7, entity.unit)
        val _tmpTargetDate: String? = entity.targetDate
        if (_tmpTargetDate == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpTargetDate)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAt)
        }
        statement.bindDouble(11, entity.progress)
        statement.bindLong(12, entity.createdAt)
      }
    }
    this.__updateAdapterOfProgressMilestoneEntity = object :
        EntityDeleteOrUpdateAdapter<ProgressMilestoneEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `progress_milestones` SET `id` = ?,`title` = ?,`description` = ?,`category` = ?,`targetValue` = ?,`currentValue` = ?,`unit` = ?,`targetDate` = ?,`isCompleted` = ?,`completedAt` = ?,`progress` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ProgressMilestoneEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindDouble(5, entity.targetValue)
        statement.bindDouble(6, entity.currentValue)
        statement.bindText(7, entity.unit)
        val _tmpTargetDate: String? = entity.targetDate
        if (_tmpTargetDate == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpTargetDate)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAt)
        }
        statement.bindDouble(11, entity.progress)
        statement.bindLong(12, entity.createdAt)
        statement.bindLong(13, entity.id)
      }
    }
  }

  public override suspend fun insert(milestone: ProgressMilestoneEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfProgressMilestoneEntity.insertAndReturnId(_connection,
        milestone)
    _result
  }

  public override suspend fun update(milestone: ProgressMilestoneEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfProgressMilestoneEntity.handle(_connection, milestone)
  }

  public override fun allMilestonesFlow(): Flow<List<ProgressMilestoneEntity>> {
    val _sql: String =
        "SELECT `progress_milestones`.`id` AS `id`, `progress_milestones`.`title` AS `title`, `progress_milestones`.`description` AS `description`, `progress_milestones`.`category` AS `category`, `progress_milestones`.`targetValue` AS `targetValue`, `progress_milestones`.`currentValue` AS `currentValue`, `progress_milestones`.`unit` AS `unit`, `progress_milestones`.`targetDate` AS `targetDate`, `progress_milestones`.`isCompleted` AS `isCompleted`, `progress_milestones`.`completedAt` AS `completedAt`, `progress_milestones`.`progress` AS `progress`, `progress_milestones`.`createdAt` AS `createdAt` FROM progress_milestones ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("progress_milestones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfTargetValue: Int = 4
        val _columnIndexOfCurrentValue: Int = 5
        val _columnIndexOfUnit: Int = 6
        val _columnIndexOfTargetDate: Int = 7
        val _columnIndexOfIsCompleted: Int = 8
        val _columnIndexOfCompletedAt: Int = 9
        val _columnIndexOfProgress: Int = 10
        val _columnIndexOfCreatedAt: Int = 11
        val _result: MutableList<ProgressMilestoneEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressMilestoneEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpTargetDate: String?
          if (_stmt.isNull(_columnIndexOfTargetDate)) {
            _tmpTargetDate = null
          } else {
            _tmpTargetDate = _stmt.getText(_columnIndexOfTargetDate)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ProgressMilestoneEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpTargetDate,_tmpIsCompleted,_tmpCompletedAt,_tmpProgress,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun milestonesByCategoryFlow(category: String):
      Flow<List<ProgressMilestoneEntity>> {
    val _sql: String =
        "SELECT * FROM progress_milestones WHERE category = ? ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("progress_milestones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfTargetDate: Int = getColumnIndexOrThrow(_stmt, "targetDate")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfProgress: Int = getColumnIndexOrThrow(_stmt, "progress")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ProgressMilestoneEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressMilestoneEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpTargetDate: String?
          if (_stmt.isNull(_columnIndexOfTargetDate)) {
            _tmpTargetDate = null
          } else {
            _tmpTargetDate = _stmt.getText(_columnIndexOfTargetDate)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ProgressMilestoneEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpTargetDate,_tmpIsCompleted,_tmpCompletedAt,_tmpProgress,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun milestonesByCompletionFlow(completed: Boolean):
      Flow<List<ProgressMilestoneEntity>> {
    val _sql: String =
        "SELECT * FROM progress_milestones WHERE isCompleted = ? ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("progress_milestones")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (completed) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfTargetDate: Int = getColumnIndexOrThrow(_stmt, "targetDate")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfProgress: Int = getColumnIndexOrThrow(_stmt, "progress")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ProgressMilestoneEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressMilestoneEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpTargetDate: String?
          if (_stmt.isNull(_columnIndexOfTargetDate)) {
            _tmpTargetDate = null
          } else {
            _tmpTargetDate = _stmt.getText(_columnIndexOfTargetDate)
          }
          val _tmpIsCompleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_1 != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ProgressMilestoneEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpTargetDate,_tmpIsCompleted,_tmpCompletedAt,_tmpProgress,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMilestone(id: Long): ProgressMilestoneEntity? {
    val _sql: String = "SELECT * FROM progress_milestones WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfTargetDate: Int = getColumnIndexOrThrow(_stmt, "targetDate")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfProgress: Int = getColumnIndexOrThrow(_stmt, "progress")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ProgressMilestoneEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpTargetDate: String?
          if (_stmt.isNull(_columnIndexOfTargetDate)) {
            _tmpTargetDate = null
          } else {
            _tmpTargetDate = _stmt.getText(_columnIndexOfTargetDate)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              ProgressMilestoneEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpTargetDate,_tmpIsCompleted,_tmpCompletedAt,_tmpProgress,_tmpCreatedAt)
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
    val _sql: String = "DELETE FROM progress_milestones WHERE id = ?"
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

  public override suspend fun updateProgress(
    id: Long,
    `value`: Double,
    progress: Double,
  ) {
    val _sql: String = "UPDATE progress_milestones SET currentValue = ?, progress = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, value)
        _argIndex = 2
        _stmt.bindDouble(_argIndex, progress)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsCompleted(
    id: Long,
    completed: Boolean,
    completedAt: Long?,
  ) {
    val _sql: String =
        "UPDATE progress_milestones SET isCompleted = ?, completedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (completed) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        if (completedAt == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, completedAt)
        }
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM progress_milestones"
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
