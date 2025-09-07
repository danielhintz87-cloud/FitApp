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
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class IntakeDao_Impl(
  __db: RoomDatabase,
) : IntakeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfIntakeEntryEntity: EntityInsertAdapter<IntakeEntryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfIntakeEntryEntity = object : EntityInsertAdapter<IntakeEntryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `intake_entries` (`id`,`timestamp`,`label`,`kcal`,`source`,`referenceId`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: IntakeEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindText(3, entity.label)
        statement.bindLong(4, entity.kcal.toLong())
        statement.bindText(5, entity.source)
        val _tmpReferenceId: String? = entity.referenceId
        if (_tmpReferenceId == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpReferenceId)
        }
      }
    }
  }

  public override suspend fun insert(entry: IntakeEntryEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfIntakeEntryEntity.insert(_connection, entry)
  }

  public override suspend fun totalForDay(epochSec: Long): Int {
    val _sql: String = """
        |
        |        SELECT COALESCE(SUM(kcal),0) FROM intake_entries
        |        WHERE date(datetime(timestamp,'unixepoch')) = date(?,'unixepoch','localtime')
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, epochSec)
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

  public override fun dayEntriesFlow(epochSec: Long): Flow<List<IntakeEntryEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM intake_entries
        |        WHERE date(datetime(timestamp,'unixepoch')) = date(?,'unixepoch','localtime')
        |        ORDER BY timestamp DESC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("intake_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, epochSec)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfKcal: Int = getColumnIndexOrThrow(_stmt, "kcal")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReferenceId: Int = getColumnIndexOrThrow(_stmt, "referenceId")
        val _result: MutableList<IntakeEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: IntakeEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpKcal: Int
          _tmpKcal = _stmt.getLong(_columnIndexOfKcal).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReferenceId: String?
          if (_stmt.isNull(_columnIndexOfReferenceId)) {
            _tmpReferenceId = null
          } else {
            _tmpReferenceId = _stmt.getText(_columnIndexOfReferenceId)
          }
          _item =
              IntakeEntryEntity(_tmpId,_tmpTimestamp,_tmpLabel,_tmpKcal,_tmpSource,_tmpReferenceId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM intake_entries"
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
