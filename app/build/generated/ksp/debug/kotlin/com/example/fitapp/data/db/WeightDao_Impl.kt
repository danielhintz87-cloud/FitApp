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
public class WeightDao_Impl(
  __db: RoomDatabase,
) : WeightDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWeightEntity: EntityInsertAdapter<WeightEntity>

  private val __updateAdapterOfWeightEntity: EntityDeleteOrUpdateAdapter<WeightEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWeightEntity = object : EntityInsertAdapter<WeightEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `weight_entries` (`id`,`weight`,`dateIso`,`notes`,`recordedAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WeightEntity) {
        statement.bindLong(1, entity.id)
        statement.bindDouble(2, entity.weight)
        statement.bindText(3, entity.dateIso)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpNotes)
        }
        statement.bindLong(5, entity.recordedAt)
      }
    }
    this.__updateAdapterOfWeightEntity = object : EntityDeleteOrUpdateAdapter<WeightEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `weight_entries` SET `id` = ?,`weight` = ?,`dateIso` = ?,`notes` = ?,`recordedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WeightEntity) {
        statement.bindLong(1, entity.id)
        statement.bindDouble(2, entity.weight)
        statement.bindText(3, entity.dateIso)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpNotes)
        }
        statement.bindLong(5, entity.recordedAt)
        statement.bindLong(6, entity.id)
      }
    }
  }

  public override suspend fun insert(weight: WeightEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfWeightEntity.insertAndReturnId(_connection, weight)
    _result
  }

  public override suspend fun update(weight: WeightEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfWeightEntity.handle(_connection, weight)
  }

  public override fun allWeightsFlow(): Flow<List<WeightEntity>> {
    val _sql: String =
        "SELECT `weight_entries`.`id` AS `id`, `weight_entries`.`weight` AS `weight`, `weight_entries`.`dateIso` AS `dateIso`, `weight_entries`.`notes` AS `notes`, `weight_entries`.`recordedAt` AS `recordedAt` FROM weight_entries ORDER BY dateIso DESC"
    return createFlow(__db, false, arrayOf("weight_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfWeight: Int = 1
        val _columnIndexOfDateIso: Int = 2
        val _columnIndexOfNotes: Int = 3
        val _columnIndexOfRecordedAt: Int = 4
        val _result: MutableList<WeightEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WeightEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpDateIso: String
          _tmpDateIso = _stmt.getText(_columnIndexOfDateIso)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _item = WeightEntity(_tmpId,_tmpWeight,_tmpDateIso,_tmpNotes,_tmpRecordedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDate(dateIso: String): WeightEntity? {
    val _sql: String = "SELECT * FROM weight_entries WHERE dateIso = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, dateIso)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfDateIso: Int = getColumnIndexOrThrow(_stmt, "dateIso")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _result: WeightEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpDateIso: String
          _tmpDateIso = _stmt.getText(_columnIndexOfDateIso)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _result = WeightEntity(_tmpId,_tmpWeight,_tmpDateIso,_tmpNotes,_tmpRecordedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLatest(): WeightEntity? {
    val _sql: String =
        "SELECT `weight_entries`.`id` AS `id`, `weight_entries`.`weight` AS `weight`, `weight_entries`.`dateIso` AS `dateIso`, `weight_entries`.`notes` AS `notes`, `weight_entries`.`recordedAt` AS `recordedAt` FROM weight_entries ORDER BY dateIso DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfWeight: Int = 1
        val _columnIndexOfDateIso: Int = 2
        val _columnIndexOfNotes: Int = 3
        val _columnIndexOfRecordedAt: Int = 4
        val _result: WeightEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpDateIso: String
          _tmpDateIso = _stmt.getText(_columnIndexOfDateIso)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _result = WeightEntity(_tmpId,_tmpWeight,_tmpDateIso,_tmpNotes,_tmpRecordedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBetween(fromIso: String, toIso: String): List<WeightEntity> {
    val _sql: String =
        "SELECT * FROM weight_entries WHERE dateIso BETWEEN ? AND ? ORDER BY dateIso DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, fromIso)
        _argIndex = 2
        _stmt.bindText(_argIndex, toIso)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfDateIso: Int = getColumnIndexOrThrow(_stmt, "dateIso")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _result: MutableList<WeightEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WeightEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpDateIso: String
          _tmpDateIso = _stmt.getText(_columnIndexOfDateIso)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpRecordedAt: Long
          _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          _item = WeightEntity(_tmpId,_tmpWeight,_tmpDateIso,_tmpNotes,_tmpRecordedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun hasEntryForDate(dateIso: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM weight_entries WHERE dateIso = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, dateIso)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM weight_entries WHERE id = ?"
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
    val _sql: String = "DELETE FROM weight_entries"
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
