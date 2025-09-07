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
public class LeaderboardEntryDao_Impl(
  __db: RoomDatabase,
) : LeaderboardEntryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLeaderboardEntryEntity: EntityInsertAdapter<LeaderboardEntryEntity>

  private val __updateAdapterOfLeaderboardEntryEntity:
      EntityDeleteOrUpdateAdapter<LeaderboardEntryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLeaderboardEntryEntity = object :
        EntityInsertAdapter<LeaderboardEntryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `leaderboard_entries` (`id`,`challengeId`,`userId`,`userName`,`rank`,`score`,`completionTime`,`badge`,`lastUpdated`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LeaderboardEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.challengeId)
        statement.bindText(3, entity.userId)
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpUserName)
        }
        statement.bindLong(5, entity.rank.toLong())
        statement.bindDouble(6, entity.score)
        val _tmpCompletionTime: Long? = entity.completionTime
        if (_tmpCompletionTime == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCompletionTime)
        }
        val _tmpBadge: String? = entity.badge
        if (_tmpBadge == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpBadge)
        }
        statement.bindLong(9, entity.lastUpdated)
      }
    }
    this.__updateAdapterOfLeaderboardEntryEntity = object :
        EntityDeleteOrUpdateAdapter<LeaderboardEntryEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `leaderboard_entries` SET `id` = ?,`challengeId` = ?,`userId` = ?,`userName` = ?,`rank` = ?,`score` = ?,`completionTime` = ?,`badge` = ?,`lastUpdated` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: LeaderboardEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.challengeId)
        statement.bindText(3, entity.userId)
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpUserName)
        }
        statement.bindLong(5, entity.rank.toLong())
        statement.bindDouble(6, entity.score)
        val _tmpCompletionTime: Long? = entity.completionTime
        if (_tmpCompletionTime == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCompletionTime)
        }
        val _tmpBadge: String? = entity.badge
        if (_tmpBadge == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpBadge)
        }
        statement.bindLong(9, entity.lastUpdated)
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insert(entry: LeaderboardEntryEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfLeaderboardEntryEntity.insertAndReturnId(_connection,
        entry)
    _result
  }

  public override suspend fun update(entry: LeaderboardEntryEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfLeaderboardEntryEntity.handle(_connection, entry)
  }

  public override fun leaderboardByChallengeFlow(challengeId: Long):
      Flow<List<LeaderboardEntryEntity>> {
    val _sql: String = "SELECT * FROM leaderboard_entries WHERE challengeId = ? ORDER BY rank ASC"
    return createFlow(__db, false, arrayOf("leaderboard_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, challengeId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "score")
        val _columnIndexOfCompletionTime: Int = getColumnIndexOrThrow(_stmt, "completionTime")
        val _columnIndexOfBadge: Int = getColumnIndexOrThrow(_stmt, "badge")
        val _columnIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: MutableList<LeaderboardEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LeaderboardEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpChallengeId: Long
          _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpUserName: String?
          if (_stmt.isNull(_columnIndexOfUserName)) {
            _tmpUserName = null
          } else {
            _tmpUserName = _stmt.getText(_columnIndexOfUserName)
          }
          val _tmpRank: Int
          _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          val _tmpScore: Double
          _tmpScore = _stmt.getDouble(_columnIndexOfScore)
          val _tmpCompletionTime: Long?
          if (_stmt.isNull(_columnIndexOfCompletionTime)) {
            _tmpCompletionTime = null
          } else {
            _tmpCompletionTime = _stmt.getLong(_columnIndexOfCompletionTime)
          }
          val _tmpBadge: String?
          if (_stmt.isNull(_columnIndexOfBadge)) {
            _tmpBadge = null
          } else {
            _tmpBadge = _stmt.getText(_columnIndexOfBadge)
          }
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _item =
              LeaderboardEntryEntity(_tmpId,_tmpChallengeId,_tmpUserId,_tmpUserName,_tmpRank,_tmpScore,_tmpCompletionTime,_tmpBadge,_tmpLastUpdated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTopEntries(challengeId: Long, limit: Int):
      List<LeaderboardEntryEntity> {
    val _sql: String =
        "SELECT * FROM leaderboard_entries WHERE challengeId = ? ORDER BY score DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, challengeId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "score")
        val _columnIndexOfCompletionTime: Int = getColumnIndexOrThrow(_stmt, "completionTime")
        val _columnIndexOfBadge: Int = getColumnIndexOrThrow(_stmt, "badge")
        val _columnIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: MutableList<LeaderboardEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LeaderboardEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpChallengeId: Long
          _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpUserName: String?
          if (_stmt.isNull(_columnIndexOfUserName)) {
            _tmpUserName = null
          } else {
            _tmpUserName = _stmt.getText(_columnIndexOfUserName)
          }
          val _tmpRank: Int
          _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          val _tmpScore: Double
          _tmpScore = _stmt.getDouble(_columnIndexOfScore)
          val _tmpCompletionTime: Long?
          if (_stmt.isNull(_columnIndexOfCompletionTime)) {
            _tmpCompletionTime = null
          } else {
            _tmpCompletionTime = _stmt.getLong(_columnIndexOfCompletionTime)
          }
          val _tmpBadge: String?
          if (_stmt.isNull(_columnIndexOfBadge)) {
            _tmpBadge = null
          } else {
            _tmpBadge = _stmt.getText(_columnIndexOfBadge)
          }
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _item =
              LeaderboardEntryEntity(_tmpId,_tmpChallengeId,_tmpUserId,_tmpUserName,_tmpRank,_tmpScore,_tmpCompletionTime,_tmpBadge,_tmpLastUpdated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUserEntry(challengeId: Long, userId: String):
      LeaderboardEntryEntity? {
    val _sql: String = "SELECT * FROM leaderboard_entries WHERE challengeId = ? AND userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, challengeId)
        _argIndex = 2
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "score")
        val _columnIndexOfCompletionTime: Int = getColumnIndexOrThrow(_stmt, "completionTime")
        val _columnIndexOfBadge: Int = getColumnIndexOrThrow(_stmt, "badge")
        val _columnIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: LeaderboardEntryEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpChallengeId: Long
          _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpUserName: String?
          if (_stmt.isNull(_columnIndexOfUserName)) {
            _tmpUserName = null
          } else {
            _tmpUserName = _stmt.getText(_columnIndexOfUserName)
          }
          val _tmpRank: Int
          _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          val _tmpScore: Double
          _tmpScore = _stmt.getDouble(_columnIndexOfScore)
          val _tmpCompletionTime: Long?
          if (_stmt.isNull(_columnIndexOfCompletionTime)) {
            _tmpCompletionTime = null
          } else {
            _tmpCompletionTime = _stmt.getLong(_columnIndexOfCompletionTime)
          }
          val _tmpBadge: String?
          if (_stmt.isNull(_columnIndexOfBadge)) {
            _tmpBadge = null
          } else {
            _tmpBadge = _stmt.getText(_columnIndexOfBadge)
          }
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _result =
              LeaderboardEntryEntity(_tmpId,_tmpChallengeId,_tmpUserId,_tmpUserName,_tmpRank,_tmpScore,_tmpCompletionTime,_tmpBadge,_tmpLastUpdated)
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
    val _sql: String = "DELETE FROM leaderboard_entries WHERE id = ?"
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

  public override suspend fun updateEntry(
    challengeId: Long,
    userId: String,
    rank: Int,
    score: Double,
    updated: Long,
  ) {
    val _sql: String =
        "UPDATE leaderboard_entries SET rank = ?, score = ?, lastUpdated = ? WHERE challengeId = ? AND userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, rank.toLong())
        _argIndex = 2
        _stmt.bindDouble(_argIndex, score)
        _argIndex = 3
        _stmt.bindLong(_argIndex, updated)
        _argIndex = 4
        _stmt.bindLong(_argIndex, challengeId)
        _argIndex = 5
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearLeaderboard(challengeId: Long) {
    val _sql: String = "DELETE FROM leaderboard_entries WHERE challengeId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, challengeId)
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
