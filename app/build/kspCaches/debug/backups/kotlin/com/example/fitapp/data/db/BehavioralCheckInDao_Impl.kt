package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
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
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BehavioralCheckInDao_Impl(
  __db: RoomDatabase,
) : BehavioralCheckInDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBehavioralCheckInEntity: EntityInsertAdapter<BehavioralCheckInEntity>

  private val __updateAdapterOfBehavioralCheckInEntity:
      EntityDeleteOrUpdateAdapter<BehavioralCheckInEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfBehavioralCheckInEntity = object :
        EntityInsertAdapter<BehavioralCheckInEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `behavioral_check_ins` (`id`,`timestamp`,`moodScore`,`hungerLevel`,`stressLevel`,`sleepQuality`,`triggers`,`copingStrategy`,`mealContext`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BehavioralCheckInEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindLong(3, entity.moodScore.toLong())
        statement.bindLong(4, entity.hungerLevel.toLong())
        statement.bindLong(5, entity.stressLevel.toLong())
        val _tmpSleepQuality: Int? = entity.sleepQuality
        if (_tmpSleepQuality == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpSleepQuality.toLong())
        }
        statement.bindText(7, entity.triggers)
        val _tmpCopingStrategy: String? = entity.copingStrategy
        if (_tmpCopingStrategy == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpCopingStrategy)
        }
        val _tmpMealContext: String? = entity.mealContext
        if (_tmpMealContext == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpMealContext)
        }
      }
    }
    this.__updateAdapterOfBehavioralCheckInEntity = object :
        EntityDeleteOrUpdateAdapter<BehavioralCheckInEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `behavioral_check_ins` SET `id` = ?,`timestamp` = ?,`moodScore` = ?,`hungerLevel` = ?,`stressLevel` = ?,`sleepQuality` = ?,`triggers` = ?,`copingStrategy` = ?,`mealContext` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: BehavioralCheckInEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindLong(3, entity.moodScore.toLong())
        statement.bindLong(4, entity.hungerLevel.toLong())
        statement.bindLong(5, entity.stressLevel.toLong())
        val _tmpSleepQuality: Int? = entity.sleepQuality
        if (_tmpSleepQuality == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpSleepQuality.toLong())
        }
        statement.bindText(7, entity.triggers)
        val _tmpCopingStrategy: String? = entity.copingStrategy
        if (_tmpCopingStrategy == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpCopingStrategy)
        }
        val _tmpMealContext: String? = entity.mealContext
        if (_tmpMealContext == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpMealContext)
        }
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insert(checkIn: BehavioralCheckInEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfBehavioralCheckInEntity.insertAndReturnId(_connection,
        checkIn)
    _result
  }

  public override suspend fun update(checkIn: BehavioralCheckInEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfBehavioralCheckInEntity.handle(_connection, checkIn)
  }

  public override suspend fun getById(id: Long): BehavioralCheckInEntity? {
    val _sql: String = "SELECT * FROM behavioral_check_ins WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfMoodScore: Int = getColumnIndexOrThrow(_stmt, "moodScore")
        val _columnIndexOfHungerLevel: Int = getColumnIndexOrThrow(_stmt, "hungerLevel")
        val _columnIndexOfStressLevel: Int = getColumnIndexOrThrow(_stmt, "stressLevel")
        val _columnIndexOfSleepQuality: Int = getColumnIndexOrThrow(_stmt, "sleepQuality")
        val _columnIndexOfTriggers: Int = getColumnIndexOrThrow(_stmt, "triggers")
        val _columnIndexOfCopingStrategy: Int = getColumnIndexOrThrow(_stmt, "copingStrategy")
        val _columnIndexOfMealContext: Int = getColumnIndexOrThrow(_stmt, "mealContext")
        val _result: BehavioralCheckInEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpMoodScore: Int
          _tmpMoodScore = _stmt.getLong(_columnIndexOfMoodScore).toInt()
          val _tmpHungerLevel: Int
          _tmpHungerLevel = _stmt.getLong(_columnIndexOfHungerLevel).toInt()
          val _tmpStressLevel: Int
          _tmpStressLevel = _stmt.getLong(_columnIndexOfStressLevel).toInt()
          val _tmpSleepQuality: Int?
          if (_stmt.isNull(_columnIndexOfSleepQuality)) {
            _tmpSleepQuality = null
          } else {
            _tmpSleepQuality = _stmt.getLong(_columnIndexOfSleepQuality).toInt()
          }
          val _tmpTriggers: String
          _tmpTriggers = _stmt.getText(_columnIndexOfTriggers)
          val _tmpCopingStrategy: String?
          if (_stmt.isNull(_columnIndexOfCopingStrategy)) {
            _tmpCopingStrategy = null
          } else {
            _tmpCopingStrategy = _stmt.getText(_columnIndexOfCopingStrategy)
          }
          val _tmpMealContext: String?
          if (_stmt.isNull(_columnIndexOfMealContext)) {
            _tmpMealContext = null
          } else {
            _tmpMealContext = _stmt.getText(_columnIndexOfMealContext)
          }
          _result =
              BehavioralCheckInEntity(_tmpId,_tmpTimestamp,_tmpMoodScore,_tmpHungerLevel,_tmpStressLevel,_tmpSleepQuality,_tmpTriggers,_tmpCopingStrategy,_tmpMealContext)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<BehavioralCheckInEntity> {
    val _sql: String =
        "SELECT `behavioral_check_ins`.`id` AS `id`, `behavioral_check_ins`.`timestamp` AS `timestamp`, `behavioral_check_ins`.`moodScore` AS `moodScore`, `behavioral_check_ins`.`hungerLevel` AS `hungerLevel`, `behavioral_check_ins`.`stressLevel` AS `stressLevel`, `behavioral_check_ins`.`sleepQuality` AS `sleepQuality`, `behavioral_check_ins`.`triggers` AS `triggers`, `behavioral_check_ins`.`copingStrategy` AS `copingStrategy`, `behavioral_check_ins`.`mealContext` AS `mealContext` FROM behavioral_check_ins ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTimestamp: Int = 1
        val _columnIndexOfMoodScore: Int = 2
        val _columnIndexOfHungerLevel: Int = 3
        val _columnIndexOfStressLevel: Int = 4
        val _columnIndexOfSleepQuality: Int = 5
        val _columnIndexOfTriggers: Int = 6
        val _columnIndexOfCopingStrategy: Int = 7
        val _columnIndexOfMealContext: Int = 8
        val _result: MutableList<BehavioralCheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BehavioralCheckInEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpMoodScore: Int
          _tmpMoodScore = _stmt.getLong(_columnIndexOfMoodScore).toInt()
          val _tmpHungerLevel: Int
          _tmpHungerLevel = _stmt.getLong(_columnIndexOfHungerLevel).toInt()
          val _tmpStressLevel: Int
          _tmpStressLevel = _stmt.getLong(_columnIndexOfStressLevel).toInt()
          val _tmpSleepQuality: Int?
          if (_stmt.isNull(_columnIndexOfSleepQuality)) {
            _tmpSleepQuality = null
          } else {
            _tmpSleepQuality = _stmt.getLong(_columnIndexOfSleepQuality).toInt()
          }
          val _tmpTriggers: String
          _tmpTriggers = _stmt.getText(_columnIndexOfTriggers)
          val _tmpCopingStrategy: String?
          if (_stmt.isNull(_columnIndexOfCopingStrategy)) {
            _tmpCopingStrategy = null
          } else {
            _tmpCopingStrategy = _stmt.getText(_columnIndexOfCopingStrategy)
          }
          val _tmpMealContext: String?
          if (_stmt.isNull(_columnIndexOfMealContext)) {
            _tmpMealContext = null
          } else {
            _tmpMealContext = _stmt.getText(_columnIndexOfMealContext)
          }
          _item =
              BehavioralCheckInEntity(_tmpId,_tmpTimestamp,_tmpMoodScore,_tmpHungerLevel,_tmpStressLevel,_tmpSleepQuality,_tmpTriggers,_tmpCopingStrategy,_tmpMealContext)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllFlow(): Flow<List<BehavioralCheckInEntity>> {
    val _sql: String =
        "SELECT `behavioral_check_ins`.`id` AS `id`, `behavioral_check_ins`.`timestamp` AS `timestamp`, `behavioral_check_ins`.`moodScore` AS `moodScore`, `behavioral_check_ins`.`hungerLevel` AS `hungerLevel`, `behavioral_check_ins`.`stressLevel` AS `stressLevel`, `behavioral_check_ins`.`sleepQuality` AS `sleepQuality`, `behavioral_check_ins`.`triggers` AS `triggers`, `behavioral_check_ins`.`copingStrategy` AS `copingStrategy`, `behavioral_check_ins`.`mealContext` AS `mealContext` FROM behavioral_check_ins ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("behavioral_check_ins")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTimestamp: Int = 1
        val _columnIndexOfMoodScore: Int = 2
        val _columnIndexOfHungerLevel: Int = 3
        val _columnIndexOfStressLevel: Int = 4
        val _columnIndexOfSleepQuality: Int = 5
        val _columnIndexOfTriggers: Int = 6
        val _columnIndexOfCopingStrategy: Int = 7
        val _columnIndexOfMealContext: Int = 8
        val _result: MutableList<BehavioralCheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BehavioralCheckInEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpMoodScore: Int
          _tmpMoodScore = _stmt.getLong(_columnIndexOfMoodScore).toInt()
          val _tmpHungerLevel: Int
          _tmpHungerLevel = _stmt.getLong(_columnIndexOfHungerLevel).toInt()
          val _tmpStressLevel: Int
          _tmpStressLevel = _stmt.getLong(_columnIndexOfStressLevel).toInt()
          val _tmpSleepQuality: Int?
          if (_stmt.isNull(_columnIndexOfSleepQuality)) {
            _tmpSleepQuality = null
          } else {
            _tmpSleepQuality = _stmt.getLong(_columnIndexOfSleepQuality).toInt()
          }
          val _tmpTriggers: String
          _tmpTriggers = _stmt.getText(_columnIndexOfTriggers)
          val _tmpCopingStrategy: String?
          if (_stmt.isNull(_columnIndexOfCopingStrategy)) {
            _tmpCopingStrategy = null
          } else {
            _tmpCopingStrategy = _stmt.getText(_columnIndexOfCopingStrategy)
          }
          val _tmpMealContext: String?
          if (_stmt.isNull(_columnIndexOfMealContext)) {
            _tmpMealContext = null
          } else {
            _tmpMealContext = _stmt.getText(_columnIndexOfMealContext)
          }
          _item =
              BehavioralCheckInEntity(_tmpId,_tmpTimestamp,_tmpMoodScore,_tmpHungerLevel,_tmpStressLevel,_tmpSleepQuality,_tmpTriggers,_tmpCopingStrategy,_tmpMealContext)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecent(limit: Int): List<BehavioralCheckInEntity> {
    val _sql: String = "SELECT * FROM behavioral_check_ins ORDER BY timestamp DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfMoodScore: Int = getColumnIndexOrThrow(_stmt, "moodScore")
        val _columnIndexOfHungerLevel: Int = getColumnIndexOrThrow(_stmt, "hungerLevel")
        val _columnIndexOfStressLevel: Int = getColumnIndexOrThrow(_stmt, "stressLevel")
        val _columnIndexOfSleepQuality: Int = getColumnIndexOrThrow(_stmt, "sleepQuality")
        val _columnIndexOfTriggers: Int = getColumnIndexOrThrow(_stmt, "triggers")
        val _columnIndexOfCopingStrategy: Int = getColumnIndexOrThrow(_stmt, "copingStrategy")
        val _columnIndexOfMealContext: Int = getColumnIndexOrThrow(_stmt, "mealContext")
        val _result: MutableList<BehavioralCheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BehavioralCheckInEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpMoodScore: Int
          _tmpMoodScore = _stmt.getLong(_columnIndexOfMoodScore).toInt()
          val _tmpHungerLevel: Int
          _tmpHungerLevel = _stmt.getLong(_columnIndexOfHungerLevel).toInt()
          val _tmpStressLevel: Int
          _tmpStressLevel = _stmt.getLong(_columnIndexOfStressLevel).toInt()
          val _tmpSleepQuality: Int?
          if (_stmt.isNull(_columnIndexOfSleepQuality)) {
            _tmpSleepQuality = null
          } else {
            _tmpSleepQuality = _stmt.getLong(_columnIndexOfSleepQuality).toInt()
          }
          val _tmpTriggers: String
          _tmpTriggers = _stmt.getText(_columnIndexOfTriggers)
          val _tmpCopingStrategy: String?
          if (_stmt.isNull(_columnIndexOfCopingStrategy)) {
            _tmpCopingStrategy = null
          } else {
            _tmpCopingStrategy = _stmt.getText(_columnIndexOfCopingStrategy)
          }
          val _tmpMealContext: String?
          if (_stmt.isNull(_columnIndexOfMealContext)) {
            _tmpMealContext = null
          } else {
            _tmpMealContext = _stmt.getText(_columnIndexOfMealContext)
          }
          _item =
              BehavioralCheckInEntity(_tmpId,_tmpTimestamp,_tmpMoodScore,_tmpHungerLevel,_tmpStressLevel,_tmpSleepQuality,_tmpTriggers,_tmpCopingStrategy,_tmpMealContext)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long):
      List<BehavioralCheckInEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM behavioral_check_ins 
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
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfMoodScore: Int = getColumnIndexOrThrow(_stmt, "moodScore")
        val _columnIndexOfHungerLevel: Int = getColumnIndexOrThrow(_stmt, "hungerLevel")
        val _columnIndexOfStressLevel: Int = getColumnIndexOrThrow(_stmt, "stressLevel")
        val _columnIndexOfSleepQuality: Int = getColumnIndexOrThrow(_stmt, "sleepQuality")
        val _columnIndexOfTriggers: Int = getColumnIndexOrThrow(_stmt, "triggers")
        val _columnIndexOfCopingStrategy: Int = getColumnIndexOrThrow(_stmt, "copingStrategy")
        val _columnIndexOfMealContext: Int = getColumnIndexOrThrow(_stmt, "mealContext")
        val _result: MutableList<BehavioralCheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BehavioralCheckInEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpMoodScore: Int
          _tmpMoodScore = _stmt.getLong(_columnIndexOfMoodScore).toInt()
          val _tmpHungerLevel: Int
          _tmpHungerLevel = _stmt.getLong(_columnIndexOfHungerLevel).toInt()
          val _tmpStressLevel: Int
          _tmpStressLevel = _stmt.getLong(_columnIndexOfStressLevel).toInt()
          val _tmpSleepQuality: Int?
          if (_stmt.isNull(_columnIndexOfSleepQuality)) {
            _tmpSleepQuality = null
          } else {
            _tmpSleepQuality = _stmt.getLong(_columnIndexOfSleepQuality).toInt()
          }
          val _tmpTriggers: String
          _tmpTriggers = _stmt.getText(_columnIndexOfTriggers)
          val _tmpCopingStrategy: String?
          if (_stmt.isNull(_columnIndexOfCopingStrategy)) {
            _tmpCopingStrategy = null
          } else {
            _tmpCopingStrategy = _stmt.getText(_columnIndexOfCopingStrategy)
          }
          val _tmpMealContext: String?
          if (_stmt.isNull(_columnIndexOfMealContext)) {
            _tmpMealContext = null
          } else {
            _tmpMealContext = _stmt.getText(_columnIndexOfMealContext)
          }
          _item =
              BehavioralCheckInEntity(_tmpId,_tmpTimestamp,_tmpMoodScore,_tmpHungerLevel,_tmpStressLevel,_tmpSleepQuality,_tmpTriggers,_tmpCopingStrategy,_tmpMealContext)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM behavioral_check_ins WHERE id = ?"
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
    val _sql: String = "DELETE FROM behavioral_check_ins"
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
