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
public class ProgressPhotoDao_Impl(
  __db: RoomDatabase,
) : ProgressPhotoDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfProgressPhotoEntity: EntityInsertAdapter<ProgressPhotoEntity>

  private val __updateAdapterOfProgressPhotoEntity: EntityDeleteOrUpdateAdapter<ProgressPhotoEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfProgressPhotoEntity = object : EntityInsertAdapter<ProgressPhotoEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `progress_photos` (`id`,`filePath`,`timestamp`,`weight`,`bmi`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProgressPhotoEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.filePath)
        statement.bindLong(3, entity.timestamp)
        statement.bindDouble(4, entity.weight.toDouble())
        statement.bindDouble(5, entity.bmi.toDouble())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNotes)
        }
      }
    }
    this.__updateAdapterOfProgressPhotoEntity = object :
        EntityDeleteOrUpdateAdapter<ProgressPhotoEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `progress_photos` SET `id` = ?,`filePath` = ?,`timestamp` = ?,`weight` = ?,`bmi` = ?,`notes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ProgressPhotoEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.filePath)
        statement.bindLong(3, entity.timestamp)
        statement.bindDouble(4, entity.weight.toDouble())
        statement.bindDouble(5, entity.bmi.toDouble())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNotes)
        }
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun insert(photo: ProgressPhotoEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfProgressPhotoEntity.insertAndReturnId(_connection, photo)
    _result
  }

  public override suspend fun update(photo: ProgressPhotoEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfProgressPhotoEntity.handle(_connection, photo)
  }

  public override suspend fun getById(id: Long): ProgressPhotoEntity? {
    val _sql: String = "SELECT * FROM progress_photos WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfBmi: Int = getColumnIndexOrThrow(_stmt, "bmi")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: ProgressPhotoEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _result =
              ProgressPhotoEntity(_tmpId,_tmpFilePath,_tmpTimestamp,_tmpWeight,_tmpBmi,_tmpNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<ProgressPhotoEntity> {
    val _sql: String =
        "SELECT `progress_photos`.`id` AS `id`, `progress_photos`.`filePath` AS `filePath`, `progress_photos`.`timestamp` AS `timestamp`, `progress_photos`.`weight` AS `weight`, `progress_photos`.`bmi` AS `bmi`, `progress_photos`.`notes` AS `notes` FROM progress_photos ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfFilePath: Int = 1
        val _columnIndexOfTimestamp: Int = 2
        val _columnIndexOfWeight: Int = 3
        val _columnIndexOfBmi: Int = 4
        val _columnIndexOfNotes: Int = 5
        val _result: MutableList<ProgressPhotoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressPhotoEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              ProgressPhotoEntity(_tmpId,_tmpFilePath,_tmpTimestamp,_tmpWeight,_tmpBmi,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllFlow(): Flow<List<ProgressPhotoEntity>> {
    val _sql: String =
        "SELECT `progress_photos`.`id` AS `id`, `progress_photos`.`filePath` AS `filePath`, `progress_photos`.`timestamp` AS `timestamp`, `progress_photos`.`weight` AS `weight`, `progress_photos`.`bmi` AS `bmi`, `progress_photos`.`notes` AS `notes` FROM progress_photos ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("progress_photos")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfFilePath: Int = 1
        val _columnIndexOfTimestamp: Int = 2
        val _columnIndexOfWeight: Int = 3
        val _columnIndexOfBmi: Int = 4
        val _columnIndexOfNotes: Int = 5
        val _result: MutableList<ProgressPhotoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressPhotoEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              ProgressPhotoEntity(_tmpId,_tmpFilePath,_tmpTimestamp,_tmpWeight,_tmpBmi,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecent(limit: Int): List<ProgressPhotoEntity> {
    val _sql: String = "SELECT * FROM progress_photos ORDER BY timestamp DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfBmi: Int = getColumnIndexOrThrow(_stmt, "bmi")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<ProgressPhotoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressPhotoEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              ProgressPhotoEntity(_tmpId,_tmpFilePath,_tmpTimestamp,_tmpWeight,_tmpBmi,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long):
      List<ProgressPhotoEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM progress_photos 
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
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfBmi: Int = getColumnIndexOrThrow(_stmt, "bmi")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<ProgressPhotoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressPhotoEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpWeight: Float
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight).toFloat()
          val _tmpBmi: Float
          _tmpBmi = _stmt.getDouble(_columnIndexOfBmi).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              ProgressPhotoEntity(_tmpId,_tmpFilePath,_tmpTimestamp,_tmpWeight,_tmpBmi,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM progress_photos WHERE id = ?"
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
