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
public class PersonalStreakDao_Impl(
  __db: RoomDatabase,
) : PersonalStreakDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPersonalStreakEntity: EntityInsertAdapter<PersonalStreakEntity>

  private val __updateAdapterOfPersonalStreakEntity:
      EntityDeleteOrUpdateAdapter<PersonalStreakEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPersonalStreakEntity = object :
        EntityInsertAdapter<PersonalStreakEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `personal_streaks` (`id`,`name`,`description`,`category`,`currentStreak`,`longestStreak`,`lastActivityTimestamp`,`isActive`,`targetDays`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PersonalStreakEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindLong(5, entity.currentStreak.toLong())
        statement.bindLong(6, entity.longestStreak.toLong())
        val _tmpLastActivityTimestamp: Long? = entity.lastActivityTimestamp
        if (_tmpLastActivityTimestamp == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpLastActivityTimestamp)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        val _tmpTargetDays: Int? = entity.targetDays
        if (_tmpTargetDays == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpTargetDays.toLong())
        }
        statement.bindLong(10, entity.createdAt)
      }
    }
    this.__updateAdapterOfPersonalStreakEntity = object :
        EntityDeleteOrUpdateAdapter<PersonalStreakEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `personal_streaks` SET `id` = ?,`name` = ?,`description` = ?,`category` = ?,`currentStreak` = ?,`longestStreak` = ?,`lastActivityTimestamp` = ?,`isActive` = ?,`targetDays` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PersonalStreakEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindLong(5, entity.currentStreak.toLong())
        statement.bindLong(6, entity.longestStreak.toLong())
        val _tmpLastActivityTimestamp: Long? = entity.lastActivityTimestamp
        if (_tmpLastActivityTimestamp == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpLastActivityTimestamp)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        val _tmpTargetDays: Int? = entity.targetDays
        if (_tmpTargetDays == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpTargetDays.toLong())
        }
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.id)
      }
    }
  }

  public override suspend fun insert(streak: PersonalStreakEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfPersonalStreakEntity.insertAndReturnId(_connection, streak)
    _result
  }

  public override suspend fun update(streak: PersonalStreakEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfPersonalStreakEntity.handle(_connection, streak)
  }

  public override fun activeStreaksFlow(): Flow<List<PersonalStreakEntity>> {
    val _sql: String =
        "SELECT `personal_streaks`.`id` AS `id`, `personal_streaks`.`name` AS `name`, `personal_streaks`.`description` AS `description`, `personal_streaks`.`category` AS `category`, `personal_streaks`.`currentStreak` AS `currentStreak`, `personal_streaks`.`longestStreak` AS `longestStreak`, `personal_streaks`.`lastActivityTimestamp` AS `lastActivityTimestamp`, `personal_streaks`.`isActive` AS `isActive`, `personal_streaks`.`targetDays` AS `targetDays`, `personal_streaks`.`createdAt` AS `createdAt` FROM personal_streaks WHERE isActive = 1 ORDER BY currentStreak DESC"
    return createFlow(__db, false, arrayOf("personal_streaks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfCurrentStreak: Int = 4
        val _columnIndexOfLongestStreak: Int = 5
        val _columnIndexOfLastActivityTimestamp: Int = 6
        val _columnIndexOfIsActive: Int = 7
        val _columnIndexOfTargetDays: Int = 8
        val _columnIndexOfCreatedAt: Int = 9
        val _result: MutableList<PersonalStreakEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalStreakEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrentStreak: Int
          _tmpCurrentStreak = _stmt.getLong(_columnIndexOfCurrentStreak).toInt()
          val _tmpLongestStreak: Int
          _tmpLongestStreak = _stmt.getLong(_columnIndexOfLongestStreak).toInt()
          val _tmpLastActivityTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfLastActivityTimestamp)) {
            _tmpLastActivityTimestamp = null
          } else {
            _tmpLastActivityTimestamp = _stmt.getLong(_columnIndexOfLastActivityTimestamp)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpTargetDays: Int?
          if (_stmt.isNull(_columnIndexOfTargetDays)) {
            _tmpTargetDays = null
          } else {
            _tmpTargetDays = _stmt.getLong(_columnIndexOfTargetDays).toInt()
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              PersonalStreakEntity(_tmpId,_tmpName,_tmpDescription,_tmpCategory,_tmpCurrentStreak,_tmpLongestStreak,_tmpLastActivityTimestamp,_tmpIsActive,_tmpTargetDays,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun allStreaksFlow(): Flow<List<PersonalStreakEntity>> {
    val _sql: String =
        "SELECT `personal_streaks`.`id` AS `id`, `personal_streaks`.`name` AS `name`, `personal_streaks`.`description` AS `description`, `personal_streaks`.`category` AS `category`, `personal_streaks`.`currentStreak` AS `currentStreak`, `personal_streaks`.`longestStreak` AS `longestStreak`, `personal_streaks`.`lastActivityTimestamp` AS `lastActivityTimestamp`, `personal_streaks`.`isActive` AS `isActive`, `personal_streaks`.`targetDays` AS `targetDays`, `personal_streaks`.`createdAt` AS `createdAt` FROM personal_streaks ORDER BY longestStreak DESC"
    return createFlow(__db, false, arrayOf("personal_streaks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfCurrentStreak: Int = 4
        val _columnIndexOfLongestStreak: Int = 5
        val _columnIndexOfLastActivityTimestamp: Int = 6
        val _columnIndexOfIsActive: Int = 7
        val _columnIndexOfTargetDays: Int = 8
        val _columnIndexOfCreatedAt: Int = 9
        val _result: MutableList<PersonalStreakEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalStreakEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrentStreak: Int
          _tmpCurrentStreak = _stmt.getLong(_columnIndexOfCurrentStreak).toInt()
          val _tmpLongestStreak: Int
          _tmpLongestStreak = _stmt.getLong(_columnIndexOfLongestStreak).toInt()
          val _tmpLastActivityTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfLastActivityTimestamp)) {
            _tmpLastActivityTimestamp = null
          } else {
            _tmpLastActivityTimestamp = _stmt.getLong(_columnIndexOfLastActivityTimestamp)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpTargetDays: Int?
          if (_stmt.isNull(_columnIndexOfTargetDays)) {
            _tmpTargetDays = null
          } else {
            _tmpTargetDays = _stmt.getLong(_columnIndexOfTargetDays).toInt()
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              PersonalStreakEntity(_tmpId,_tmpName,_tmpDescription,_tmpCategory,_tmpCurrentStreak,_tmpLongestStreak,_tmpLastActivityTimestamp,_tmpIsActive,_tmpTargetDays,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun streaksByCategoryFlow(category: String): Flow<List<PersonalStreakEntity>> {
    val _sql: String =
        "SELECT * FROM personal_streaks WHERE category = ? ORDER BY currentStreak DESC"
    return createFlow(__db, false, arrayOf("personal_streaks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCurrentStreak: Int = getColumnIndexOrThrow(_stmt, "currentStreak")
        val _columnIndexOfLongestStreak: Int = getColumnIndexOrThrow(_stmt, "longestStreak")
        val _columnIndexOfLastActivityTimestamp: Int = getColumnIndexOrThrow(_stmt,
            "lastActivityTimestamp")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfTargetDays: Int = getColumnIndexOrThrow(_stmt, "targetDays")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<PersonalStreakEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalStreakEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrentStreak: Int
          _tmpCurrentStreak = _stmt.getLong(_columnIndexOfCurrentStreak).toInt()
          val _tmpLongestStreak: Int
          _tmpLongestStreak = _stmt.getLong(_columnIndexOfLongestStreak).toInt()
          val _tmpLastActivityTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfLastActivityTimestamp)) {
            _tmpLastActivityTimestamp = null
          } else {
            _tmpLastActivityTimestamp = _stmt.getLong(_columnIndexOfLastActivityTimestamp)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpTargetDays: Int?
          if (_stmt.isNull(_columnIndexOfTargetDays)) {
            _tmpTargetDays = null
          } else {
            _tmpTargetDays = _stmt.getLong(_columnIndexOfTargetDays).toInt()
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              PersonalStreakEntity(_tmpId,_tmpName,_tmpDescription,_tmpCategory,_tmpCurrentStreak,_tmpLongestStreak,_tmpLastActivityTimestamp,_tmpIsActive,_tmpTargetDays,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getStreak(id: Long): PersonalStreakEntity? {
    val _sql: String = "SELECT * FROM personal_streaks WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCurrentStreak: Int = getColumnIndexOrThrow(_stmt, "currentStreak")
        val _columnIndexOfLongestStreak: Int = getColumnIndexOrThrow(_stmt, "longestStreak")
        val _columnIndexOfLastActivityTimestamp: Int = getColumnIndexOrThrow(_stmt,
            "lastActivityTimestamp")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfTargetDays: Int = getColumnIndexOrThrow(_stmt, "targetDays")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: PersonalStreakEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrentStreak: Int
          _tmpCurrentStreak = _stmt.getLong(_columnIndexOfCurrentStreak).toInt()
          val _tmpLongestStreak: Int
          _tmpLongestStreak = _stmt.getLong(_columnIndexOfLongestStreak).toInt()
          val _tmpLastActivityTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfLastActivityTimestamp)) {
            _tmpLastActivityTimestamp = null
          } else {
            _tmpLastActivityTimestamp = _stmt.getLong(_columnIndexOfLastActivityTimestamp)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpTargetDays: Int?
          if (_stmt.isNull(_columnIndexOfTargetDays)) {
            _tmpTargetDays = null
          } else {
            _tmpTargetDays = _stmt.getLong(_columnIndexOfTargetDays).toInt()
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              PersonalStreakEntity(_tmpId,_tmpName,_tmpDescription,_tmpCategory,_tmpCurrentStreak,_tmpLongestStreak,_tmpLastActivityTimestamp,_tmpIsActive,_tmpTargetDays,_tmpCreatedAt)
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
    val _sql: String = "DELETE FROM personal_streaks WHERE id = ?"
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

  public override suspend fun updateStreak(
    id: Long,
    currentStreak: Int,
    longestStreak: Int,
    lastActivityTimestamp: Long?,
  ) {
    val _sql: String =
        "UPDATE personal_streaks SET currentStreak = ?, longestStreak = ?, lastActivityTimestamp = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, currentStreak.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, longestStreak.toLong())
        _argIndex = 3
        if (lastActivityTimestamp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, lastActivityTimestamp)
        }
        _argIndex = 4
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setActive(id: Long, active: Boolean) {
    val _sql: String = "UPDATE personal_streaks SET isActive = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (active) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetAllStreaks() {
    val _sql: String = "UPDATE personal_streaks SET currentStreak = 0, lastActivityTimestamp = NULL"
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
    val _sql: String = "DELETE FROM personal_streaks"
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
