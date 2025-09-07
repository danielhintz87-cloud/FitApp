package com.example.fitapp.`data`.db

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
public class AiLogDao_Impl(
  __db: RoomDatabase,
) : AiLogDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAiLog: EntityInsertAdapter<AiLog>
  init {
    this.__db = __db
    this.__insertAdapterOfAiLog = object : EntityInsertAdapter<AiLog>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `ai_logs` (`id`,`ts`,`type`,`provider`,`prompt`,`result`,`success`,`tookMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AiLog) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.ts)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.provider)
        statement.bindText(5, entity.prompt)
        statement.bindText(6, entity.result)
        val _tmp: Int = if (entity.success) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.tookMs)
      }
    }
  }

  public override suspend fun insert(log: AiLog): Unit = performSuspending(__db, false, true) {
      _connection ->
    __insertAdapterOfAiLog.insert(_connection, log)
  }

  public override fun latest(limit: Int): Flow<List<AiLog>> {
    val _sql: String = "SELECT * FROM ai_logs ORDER BY ts DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("ai_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTs: Int = getColumnIndexOrThrow(_stmt, "ts")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfProvider: Int = getColumnIndexOrThrow(_stmt, "provider")
        val _columnIndexOfPrompt: Int = getColumnIndexOrThrow(_stmt, "prompt")
        val _columnIndexOfResult: Int = getColumnIndexOrThrow(_stmt, "result")
        val _columnIndexOfSuccess: Int = getColumnIndexOrThrow(_stmt, "success")
        val _columnIndexOfTookMs: Int = getColumnIndexOrThrow(_stmt, "tookMs")
        val _result: MutableList<AiLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: AiLog
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTs: Long
          _tmpTs = _stmt.getLong(_columnIndexOfTs)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpProvider: String
          _tmpProvider = _stmt.getText(_columnIndexOfProvider)
          val _tmpPrompt: String
          _tmpPrompt = _stmt.getText(_columnIndexOfPrompt)
          val _tmpResult: String
          _tmpResult = _stmt.getText(_columnIndexOfResult)
          val _tmpSuccess: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSuccess).toInt()
          _tmpSuccess = _tmp != 0
          val _tmpTookMs: Long
          _tmpTookMs = _stmt.getLong(_columnIndexOfTookMs)
          _item =
              AiLog(_tmpId,_tmpTs,_tmpType,_tmpProvider,_tmpPrompt,_tmpResult,_tmpSuccess,_tmpTookMs)
          _result.add(_item)
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
