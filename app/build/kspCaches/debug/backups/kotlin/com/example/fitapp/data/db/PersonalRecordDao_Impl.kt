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
public class PersonalRecordDao_Impl(
  __db: RoomDatabase,
) : PersonalRecordDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPersonalRecordEntity: EntityInsertAdapter<PersonalRecordEntity>

  private val __updateAdapterOfPersonalRecordEntity:
      EntityDeleteOrUpdateAdapter<PersonalRecordEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPersonalRecordEntity = object :
        EntityInsertAdapter<PersonalRecordEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `personal_records` (`id`,`exerciseName`,`recordType`,`value`,`unit`,`notes`,`achievedAt`,`previousRecord`,`improvement`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PersonalRecordEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.exerciseName)
        statement.bindText(3, entity.recordType)
        statement.bindDouble(4, entity.value)
        statement.bindText(5, entity.unit)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNotes)
        }
        statement.bindLong(7, entity.achievedAt)
        val _tmpPreviousRecord: Double? = entity.previousRecord
        if (_tmpPreviousRecord == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpPreviousRecord)
        }
        val _tmpImprovement: Double? = entity.improvement
        if (_tmpImprovement == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpImprovement)
        }
      }
    }
    this.__updateAdapterOfPersonalRecordEntity = object :
        EntityDeleteOrUpdateAdapter<PersonalRecordEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `personal_records` SET `id` = ?,`exerciseName` = ?,`recordType` = ?,`value` = ?,`unit` = ?,`notes` = ?,`achievedAt` = ?,`previousRecord` = ?,`improvement` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PersonalRecordEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.exerciseName)
        statement.bindText(3, entity.recordType)
        statement.bindDouble(4, entity.value)
        statement.bindText(5, entity.unit)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNotes)
        }
        statement.bindLong(7, entity.achievedAt)
        val _tmpPreviousRecord: Double? = entity.previousRecord
        if (_tmpPreviousRecord == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpPreviousRecord)
        }
        val _tmpImprovement: Double? = entity.improvement
        if (_tmpImprovement == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpImprovement)
        }
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insert(record: PersonalRecordEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfPersonalRecordEntity.insertAndReturnId(_connection, record)
    _result
  }

  public override suspend fun update(record: PersonalRecordEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfPersonalRecordEntity.handle(_connection, record)
  }

  public override fun allRecordsFlow(): Flow<List<PersonalRecordEntity>> {
    val _sql: String =
        "SELECT `personal_records`.`id` AS `id`, `personal_records`.`exerciseName` AS `exerciseName`, `personal_records`.`recordType` AS `recordType`, `personal_records`.`value` AS `value`, `personal_records`.`unit` AS `unit`, `personal_records`.`notes` AS `notes`, `personal_records`.`achievedAt` AS `achievedAt`, `personal_records`.`previousRecord` AS `previousRecord`, `personal_records`.`improvement` AS `improvement` FROM personal_records ORDER BY achievedAt DESC"
    return createFlow(__db, false, arrayOf("personal_records")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfExerciseName: Int = 1
        val _columnIndexOfRecordType: Int = 2
        val _columnIndexOfValue: Int = 3
        val _columnIndexOfUnit: Int = 4
        val _columnIndexOfNotes: Int = 5
        val _columnIndexOfAchievedAt: Int = 6
        val _columnIndexOfPreviousRecord: Int = 7
        val _columnIndexOfImprovement: Int = 8
        val _result: MutableList<PersonalRecordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalRecordEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpAchievedAt: Long
          _tmpAchievedAt = _stmt.getLong(_columnIndexOfAchievedAt)
          val _tmpPreviousRecord: Double?
          if (_stmt.isNull(_columnIndexOfPreviousRecord)) {
            _tmpPreviousRecord = null
          } else {
            _tmpPreviousRecord = _stmt.getDouble(_columnIndexOfPreviousRecord)
          }
          val _tmpImprovement: Double?
          if (_stmt.isNull(_columnIndexOfImprovement)) {
            _tmpImprovement = null
          } else {
            _tmpImprovement = _stmt.getDouble(_columnIndexOfImprovement)
          }
          _item =
              PersonalRecordEntity(_tmpId,_tmpExerciseName,_tmpRecordType,_tmpValue,_tmpUnit,_tmpNotes,_tmpAchievedAt,_tmpPreviousRecord,_tmpImprovement)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun recordsByExerciseFlow(exerciseName: String):
      Flow<List<PersonalRecordEntity>> {
    val _sql: String =
        "SELECT * FROM personal_records WHERE exerciseName = ? ORDER BY achievedAt DESC"
    return createFlow(__db, false, arrayOf("personal_records")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfAchievedAt: Int = getColumnIndexOrThrow(_stmt, "achievedAt")
        val _columnIndexOfPreviousRecord: Int = getColumnIndexOrThrow(_stmt, "previousRecord")
        val _columnIndexOfImprovement: Int = getColumnIndexOrThrow(_stmt, "improvement")
        val _result: MutableList<PersonalRecordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalRecordEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpAchievedAt: Long
          _tmpAchievedAt = _stmt.getLong(_columnIndexOfAchievedAt)
          val _tmpPreviousRecord: Double?
          if (_stmt.isNull(_columnIndexOfPreviousRecord)) {
            _tmpPreviousRecord = null
          } else {
            _tmpPreviousRecord = _stmt.getDouble(_columnIndexOfPreviousRecord)
          }
          val _tmpImprovement: Double?
          if (_stmt.isNull(_columnIndexOfImprovement)) {
            _tmpImprovement = null
          } else {
            _tmpImprovement = _stmt.getDouble(_columnIndexOfImprovement)
          }
          _item =
              PersonalRecordEntity(_tmpId,_tmpExerciseName,_tmpRecordType,_tmpValue,_tmpUnit,_tmpNotes,_tmpAchievedAt,_tmpPreviousRecord,_tmpImprovement)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun recordsByTypeFlow(recordType: String): Flow<List<PersonalRecordEntity>> {
    val _sql: String =
        "SELECT * FROM personal_records WHERE recordType = ? ORDER BY achievedAt DESC"
    return createFlow(__db, false, arrayOf("personal_records")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, recordType)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfAchievedAt: Int = getColumnIndexOrThrow(_stmt, "achievedAt")
        val _columnIndexOfPreviousRecord: Int = getColumnIndexOrThrow(_stmt, "previousRecord")
        val _columnIndexOfImprovement: Int = getColumnIndexOrThrow(_stmt, "improvement")
        val _result: MutableList<PersonalRecordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalRecordEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpAchievedAt: Long
          _tmpAchievedAt = _stmt.getLong(_columnIndexOfAchievedAt)
          val _tmpPreviousRecord: Double?
          if (_stmt.isNull(_columnIndexOfPreviousRecord)) {
            _tmpPreviousRecord = null
          } else {
            _tmpPreviousRecord = _stmt.getDouble(_columnIndexOfPreviousRecord)
          }
          val _tmpImprovement: Double?
          if (_stmt.isNull(_columnIndexOfImprovement)) {
            _tmpImprovement = null
          } else {
            _tmpImprovement = _stmt.getDouble(_columnIndexOfImprovement)
          }
          _item =
              PersonalRecordEntity(_tmpId,_tmpExerciseName,_tmpRecordType,_tmpValue,_tmpUnit,_tmpNotes,_tmpAchievedAt,_tmpPreviousRecord,_tmpImprovement)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBestRecord(exerciseName: String, recordType: String):
      PersonalRecordEntity? {
    val _sql: String =
        "SELECT * FROM personal_records WHERE exerciseName = ? AND recordType = ? ORDER BY value DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseName)
        _argIndex = 2
        _stmt.bindText(_argIndex, recordType)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfAchievedAt: Int = getColumnIndexOrThrow(_stmt, "achievedAt")
        val _columnIndexOfPreviousRecord: Int = getColumnIndexOrThrow(_stmt, "previousRecord")
        val _columnIndexOfImprovement: Int = getColumnIndexOrThrow(_stmt, "improvement")
        val _result: PersonalRecordEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpAchievedAt: Long
          _tmpAchievedAt = _stmt.getLong(_columnIndexOfAchievedAt)
          val _tmpPreviousRecord: Double?
          if (_stmt.isNull(_columnIndexOfPreviousRecord)) {
            _tmpPreviousRecord = null
          } else {
            _tmpPreviousRecord = _stmt.getDouble(_columnIndexOfPreviousRecord)
          }
          val _tmpImprovement: Double?
          if (_stmt.isNull(_columnIndexOfImprovement)) {
            _tmpImprovement = null
          } else {
            _tmpImprovement = _stmt.getDouble(_columnIndexOfImprovement)
          }
          _result =
              PersonalRecordEntity(_tmpId,_tmpExerciseName,_tmpRecordType,_tmpValue,_tmpUnit,_tmpNotes,_tmpAchievedAt,_tmpPreviousRecord,_tmpImprovement)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecord(exerciseName: String, recordType: String):
      PersonalRecordEntity? {
    val _sql: String =
        "SELECT * FROM personal_records WHERE exerciseName = ? AND recordType = ? ORDER BY achievedAt DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseName)
        _argIndex = 2
        _stmt.bindText(_argIndex, recordType)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfAchievedAt: Int = getColumnIndexOrThrow(_stmt, "achievedAt")
        val _columnIndexOfPreviousRecord: Int = getColumnIndexOrThrow(_stmt, "previousRecord")
        val _columnIndexOfImprovement: Int = getColumnIndexOrThrow(_stmt, "improvement")
        val _result: PersonalRecordEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpAchievedAt: Long
          _tmpAchievedAt = _stmt.getLong(_columnIndexOfAchievedAt)
          val _tmpPreviousRecord: Double?
          if (_stmt.isNull(_columnIndexOfPreviousRecord)) {
            _tmpPreviousRecord = null
          } else {
            _tmpPreviousRecord = _stmt.getDouble(_columnIndexOfPreviousRecord)
          }
          val _tmpImprovement: Double?
          if (_stmt.isNull(_columnIndexOfImprovement)) {
            _tmpImprovement = null
          } else {
            _tmpImprovement = _stmt.getDouble(_columnIndexOfImprovement)
          }
          _result =
              PersonalRecordEntity(_tmpId,_tmpExerciseName,_tmpRecordType,_tmpValue,_tmpUnit,_tmpNotes,_tmpAchievedAt,_tmpPreviousRecord,_tmpImprovement)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecord(id: Long): PersonalRecordEntity? {
    val _sql: String = "SELECT * FROM personal_records WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfAchievedAt: Int = getColumnIndexOrThrow(_stmt, "achievedAt")
        val _columnIndexOfPreviousRecord: Int = getColumnIndexOrThrow(_stmt, "previousRecord")
        val _columnIndexOfImprovement: Int = getColumnIndexOrThrow(_stmt, "improvement")
        val _result: PersonalRecordEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpAchievedAt: Long
          _tmpAchievedAt = _stmt.getLong(_columnIndexOfAchievedAt)
          val _tmpPreviousRecord: Double?
          if (_stmt.isNull(_columnIndexOfPreviousRecord)) {
            _tmpPreviousRecord = null
          } else {
            _tmpPreviousRecord = _stmt.getDouble(_columnIndexOfPreviousRecord)
          }
          val _tmpImprovement: Double?
          if (_stmt.isNull(_columnIndexOfImprovement)) {
            _tmpImprovement = null
          } else {
            _tmpImprovement = _stmt.getDouble(_columnIndexOfImprovement)
          }
          _result =
              PersonalRecordEntity(_tmpId,_tmpExerciseName,_tmpRecordType,_tmpValue,_tmpUnit,_tmpNotes,_tmpAchievedAt,_tmpPreviousRecord,_tmpImprovement)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExerciseNames(): List<String> {
    val _sql: String = "SELECT DISTINCT exerciseName FROM personal_records ORDER BY exerciseName"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM personal_records WHERE id = ?"
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
    val _sql: String = "DELETE FROM personal_records"
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
