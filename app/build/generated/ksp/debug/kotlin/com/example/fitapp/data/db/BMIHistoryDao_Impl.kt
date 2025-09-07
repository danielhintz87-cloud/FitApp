package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class BMIHistoryDao_Impl(
  __db: RoomDatabase,
) : BMIHistoryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBMIHistoryEntity: EntityInsertAdapter<BMIHistoryEntity>

  private val __updateAdapterOfBMIHistoryEntity: EntityDeleteOrUpdateAdapter<BMIHistoryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfBMIHistoryEntity = object : EntityInsertAdapter<BMIHistoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `bmi_history` (`id`,`date`,`height`,`weight`,`bmi`,`category`,`notes`,`recordedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BMIHistoryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindDouble(3, entity.height.toDouble())
        statement.bindDouble(4, entity.weight.toDouble())
        statement.bindDouble(5, entity.bmi.toDouble())
        statement.bindText(6, entity.category)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNotes)
        }
        statement.bindLong(8, entity.recordedAt)
      }
    }
    this.__updateAdapterOfBMIHistoryEntity = object :
        EntityDeleteOrUpdateAdapter<BMIHistoryEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `bmi_history` SET `id` = ?,`date` = ?,`height` = ?,`weight` = ?,`bmi` = ?,`category` = ?,`notes` = ?,`recordedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: BMIHistoryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.date)
        statement.bindDouble(3, entity.height.toDouble())
        statement.bindDouble(4, entity.weight.toDouble())
        statement.bindDouble(5, entity.bmi.toDouble())
        statement.bindText(6, entity.category)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNotes)
        }
        statement.bindLong(8, entity.recordedAt)
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insert(bmiHistory: BMIHistoryEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfBMIHistoryEntity.insertAndReturnId(_connection, bmiHistory)
    _result
  }

  public override suspend fun update(bmiHistory: BMIHistoryEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfBMIHistoryEntity.handle(_connection, bmiHistory)
  }

  public override suspend fun getByDate(date: String): BMIHistoryEntity? {
    val _sql: String = "SELECT * FROM bmi_history WHERE date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfBmi: Int = getColumnIndexOrThrow(_stmt, "bmi")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _result: BMIHistoryEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpHeight: Float
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight).toFloat()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _result =
              BMIHistoryEntity(_tmpId,_tmpDate,_tmpHeight,_tmpWeight,_tmpBmi,_tmpCategory,_tmpNotes,_tmpRecordedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<BMIHistoryEntity> {
    val _sql: String =
        "SELECT `bmi_history`.`id` AS `id`, `bmi_history`.`date` AS `date`, `bmi_history`.`height` AS `height`, `bmi_history`.`weight` AS `weight`, `bmi_history`.`bmi` AS `bmi`, `bmi_history`.`category` AS `category`, `bmi_history`.`notes` AS `notes`, `bmi_history`.`recordedAt` AS `recordedAt` FROM bmi_history ORDER BY date DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfDate: Int = 1
        val _columnIndexOfHeight: Int = 2
        val _columnIndexOfWeight: Int = 3
        val _columnIndexOfBmi: Int = 4
        val _columnIndexOfCategory: Int = 5
        val _columnIndexOfNotes: Int = 6
        val _columnIndexOfRecordedAt: Int = 7
        val _result: MutableList<BMIHistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BMIHistoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpHeight: Float
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight).toFloat()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _item =
              BMIHistoryEntity(_tmpId,_tmpDate,_tmpHeight,_tmpWeight,_tmpBmi,_tmpCategory,_tmpNotes,_tmpRecordedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllFlow(): Flow<List<BMIHistoryEntity>> {
    val _sql: String =
        "SELECT `bmi_history`.`id` AS `id`, `bmi_history`.`date` AS `date`, `bmi_history`.`height` AS `height`, `bmi_history`.`weight` AS `weight`, `bmi_history`.`bmi` AS `bmi`, `bmi_history`.`category` AS `category`, `bmi_history`.`notes` AS `notes`, `bmi_history`.`recordedAt` AS `recordedAt` FROM bmi_history ORDER BY date DESC"
    return createFlow(__db, false, arrayOf("bmi_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfDate: Int = 1
        val _columnIndexOfHeight: Int = 2
        val _columnIndexOfWeight: Int = 3
        val _columnIndexOfBmi: Int = 4
        val _columnIndexOfCategory: Int = 5
        val _columnIndexOfNotes: Int = 6
        val _columnIndexOfRecordedAt: Int = 7
        val _result: MutableList<BMIHistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BMIHistoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpHeight: Float
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight).toFloat()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _item =
              BMIHistoryEntity(_tmpId,_tmpDate,_tmpHeight,_tmpWeight,_tmpBmi,_tmpCategory,_tmpNotes,_tmpRecordedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecent(limit: Int): List<BMIHistoryEntity> {
    val _sql: String = "SELECT * FROM bmi_history ORDER BY date DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfBmi: Int = getColumnIndexOrThrow(_stmt, "bmi")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _result: MutableList<BMIHistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BMIHistoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpHeight: Float
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight).toFloat()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _item =
              BMIHistoryEntity(_tmpId,_tmpDate,_tmpHeight,_tmpWeight,_tmpBmi,_tmpCategory,_tmpNotes,_tmpRecordedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startDate: String, endDate: String):
      List<BMIHistoryEntity> {
    val _sql: String = "SELECT * FROM bmi_history WHERE date BETWEEN ? AND ? ORDER BY date"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, startDate)
        _argIndex = 2
        _stmt.bindText(_argIndex, endDate)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfBmi: Int = getColumnIndexOrThrow(_stmt, "bmi")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _result: MutableList<BMIHistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BMIHistoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpHeight: Float
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight).toFloat()
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _item =
              BMIHistoryEntity(_tmpId,_tmpDate,_tmpHeight,_tmpWeight,_tmpBmi,_tmpCategory,_tmpNotes,_tmpRecordedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM bmi_history WHERE id = ?"
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
    val _sql: String = "DELETE FROM bmi_history"
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
