package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class ChallengeProgressLogDao_Impl(
  __db: RoomDatabase,
) : ChallengeProgressLogDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfChallengeProgressLogEntity:
      EntityInsertAdapter<ChallengeProgressLogEntity>

  private val __updateAdapterOfChallengeProgressLogEntity:
      EntityDeleteOrUpdateAdapter<ChallengeProgressLogEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfChallengeProgressLogEntity = object :
        EntityInsertAdapter<ChallengeProgressLogEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `challenge_progress_logs` (`id`,`participationId`,`logDate`,`value`,`description`,`source`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChallengeProgressLogEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.participationId)
        statement.bindText(3, entity.logDate)
        statement.bindDouble(4, entity.value)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpDescription)
        }
        statement.bindText(6, entity.source)
        statement.bindLong(7, entity.timestamp)
      }
    }
    this.__updateAdapterOfChallengeProgressLogEntity = object :
        EntityDeleteOrUpdateAdapter<ChallengeProgressLogEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `challenge_progress_logs` SET `id` = ?,`participationId` = ?,`logDate` = ?,`value` = ?,`description` = ?,`source` = ?,`timestamp` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ChallengeProgressLogEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.participationId)
        statement.bindText(3, entity.logDate)
        statement.bindDouble(4, entity.value)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpDescription)
        }
        statement.bindText(6, entity.source)
        statement.bindLong(7, entity.timestamp)
        statement.bindLong(8, entity.id)
      }
    }
  }

  public override suspend fun insert(log: ChallengeProgressLogEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfChallengeProgressLogEntity.insertAndReturnId(_connection,
        log)
    _result
  }

  public override suspend fun update(log: ChallengeProgressLogEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfChallengeProgressLogEntity.handle(_connection, log)
  }

  public override fun logsByParticipationFlow(participationId: Long):
      Flow<List<ChallengeProgressLogEntity>> {
    val _sql: String =
        "SELECT * FROM challenge_progress_logs WHERE participationId = ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("challenge_progress_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, participationId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfParticipationId: Int = getColumnIndexOrThrow(_stmt, "participationId")
        val _columnIndexOfLogDate: Int = getColumnIndexOrThrow(_stmt, "logDate")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<ChallengeProgressLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChallengeProgressLogEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpParticipationId: Long
          _tmpParticipationId = _stmt.getLong(_columnIndexOfParticipationId)
          val _tmpLogDate: String
          _tmpLogDate = _stmt.getText(_columnIndexOfLogDate)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item =
              ChallengeProgressLogEntity(_tmpId,_tmpParticipationId,_tmpLogDate,_tmpValue,_tmpDescription,_tmpSource,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLogForDate(participationId: Long, date: String):
      ChallengeProgressLogEntity? {
    val _sql: String =
        "SELECT * FROM challenge_progress_logs WHERE participationId = ? AND logDate = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, participationId)
        _argIndex = 2
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfParticipationId: Int = getColumnIndexOrThrow(_stmt, "participationId")
        val _columnIndexOfLogDate: Int = getColumnIndexOrThrow(_stmt, "logDate")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: ChallengeProgressLogEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpParticipationId: Long
          _tmpParticipationId = _stmt.getLong(_columnIndexOfParticipationId)
          val _tmpLogDate: String
          _tmpLogDate = _stmt.getText(_columnIndexOfLogDate)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _result =
              ChallengeProgressLogEntity(_tmpId,_tmpParticipationId,_tmpLogDate,_tmpValue,_tmpDescription,_tmpSource,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalProgress(participationId: Long): Double? {
    val _sql: String = "SELECT SUM(value) FROM challenge_progress_logs WHERE participationId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, participationId)
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
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

  public override suspend fun getProgressBetweenDates(
    participationId: Long,
    startDate: String,
    endDate: String,
  ): Double? {
    val _sql: String =
        "SELECT SUM(value) FROM challenge_progress_logs WHERE participationId = ? AND logDate BETWEEN ? AND ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, participationId)
        _argIndex = 2
        _stmt.bindText(_argIndex, startDate)
        _argIndex = 3
        _stmt.bindText(_argIndex, endDate)
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
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

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM challenge_progress_logs WHERE id = ?"
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
