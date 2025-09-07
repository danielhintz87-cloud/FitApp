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
public class ChallengeParticipationDao_Impl(
  __db: RoomDatabase,
) : ChallengeParticipationDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfChallengeParticipationEntity:
      EntityInsertAdapter<ChallengeParticipationEntity>

  private val __updateAdapterOfChallengeParticipationEntity:
      EntityDeleteOrUpdateAdapter<ChallengeParticipationEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfChallengeParticipationEntity = object :
        EntityInsertAdapter<ChallengeParticipationEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `challenge_participations` (`id`,`challengeId`,`userId`,`userName`,`status`,`currentProgress`,`progressPercentage`,`lastActivityDate`,`completedAt`,`joinedAt`,`rank`,`personalBest`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement,
          entity: ChallengeParticipationEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.challengeId)
        statement.bindText(3, entity.userId)
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpUserName)
        }
        statement.bindText(5, entity.status)
        statement.bindDouble(6, entity.currentProgress)
        statement.bindDouble(7, entity.progressPercentage)
        val _tmpLastActivityDate: String? = entity.lastActivityDate
        if (_tmpLastActivityDate == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpLastActivityDate)
        }
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpCompletedAt)
        }
        statement.bindLong(10, entity.joinedAt)
        val _tmpRank: Int? = entity.rank
        if (_tmpRank == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpRank.toLong())
        }
        val _tmpPersonalBest: Double? = entity.personalBest
        if (_tmpPersonalBest == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpPersonalBest)
        }
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpNotes)
        }
      }
    }
    this.__updateAdapterOfChallengeParticipationEntity = object :
        EntityDeleteOrUpdateAdapter<ChallengeParticipationEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `challenge_participations` SET `id` = ?,`challengeId` = ?,`userId` = ?,`userName` = ?,`status` = ?,`currentProgress` = ?,`progressPercentage` = ?,`lastActivityDate` = ?,`completedAt` = ?,`joinedAt` = ?,`rank` = ?,`personalBest` = ?,`notes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement,
          entity: ChallengeParticipationEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.challengeId)
        statement.bindText(3, entity.userId)
        val _tmpUserName: String? = entity.userName
        if (_tmpUserName == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpUserName)
        }
        statement.bindText(5, entity.status)
        statement.bindDouble(6, entity.currentProgress)
        statement.bindDouble(7, entity.progressPercentage)
        val _tmpLastActivityDate: String? = entity.lastActivityDate
        if (_tmpLastActivityDate == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpLastActivityDate)
        }
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpCompletedAt)
        }
        statement.bindLong(10, entity.joinedAt)
        val _tmpRank: Int? = entity.rank
        if (_tmpRank == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpRank.toLong())
        }
        val _tmpPersonalBest: Double? = entity.personalBest
        if (_tmpPersonalBest == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpPersonalBest)
        }
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpNotes)
        }
        statement.bindLong(14, entity.id)
      }
    }
  }

  public override suspend fun insert(participation: ChallengeParticipationEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfChallengeParticipationEntity.insertAndReturnId(_connection,
        participation)
    _result
  }

  public override suspend fun update(participation: ChallengeParticipationEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfChallengeParticipationEntity.handle(_connection, participation)
  }

  public override fun participationsByChallengeFlow(challengeId: Long):
      Flow<List<ChallengeParticipationEntity>> {
    val _sql: String =
        "SELECT * FROM challenge_participations WHERE challengeId = ? ORDER BY currentProgress DESC"
    return createFlow(__db, false, arrayOf("challenge_participations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, challengeId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCurrentProgress: Int = getColumnIndexOrThrow(_stmt, "currentProgress")
        val _columnIndexOfProgressPercentage: Int = getColumnIndexOrThrow(_stmt,
            "progressPercentage")
        val _columnIndexOfLastActivityDate: Int = getColumnIndexOrThrow(_stmt, "lastActivityDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPersonalBest: Int = getColumnIndexOrThrow(_stmt, "personalBest")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<ChallengeParticipationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChallengeParticipationEntity
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
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentProgress: Double
          _tmpCurrentProgress = _stmt.getDouble(_columnIndexOfCurrentProgress)
          val _tmpProgressPercentage: Double
          _tmpProgressPercentage = _stmt.getDouble(_columnIndexOfProgressPercentage)
          val _tmpLastActivityDate: String?
          if (_stmt.isNull(_columnIndexOfLastActivityDate)) {
            _tmpLastActivityDate = null
          } else {
            _tmpLastActivityDate = _stmt.getText(_columnIndexOfLastActivityDate)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpRank: Int?
          if (_stmt.isNull(_columnIndexOfRank)) {
            _tmpRank = null
          } else {
            _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          }
          val _tmpPersonalBest: Double?
          if (_stmt.isNull(_columnIndexOfPersonalBest)) {
            _tmpPersonalBest = null
          } else {
            _tmpPersonalBest = _stmt.getDouble(_columnIndexOfPersonalBest)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              ChallengeParticipationEntity(_tmpId,_tmpChallengeId,_tmpUserId,_tmpUserName,_tmpStatus,_tmpCurrentProgress,_tmpProgressPercentage,_tmpLastActivityDate,_tmpCompletedAt,_tmpJoinedAt,_tmpRank,_tmpPersonalBest,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun participationsByUserFlow(userId: String):
      Flow<List<ChallengeParticipationEntity>> {
    val _sql: String =
        "SELECT * FROM challenge_participations WHERE userId = ? ORDER BY joinedAt DESC"
    return createFlow(__db, false, arrayOf("challenge_participations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUserName: Int = getColumnIndexOrThrow(_stmt, "userName")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCurrentProgress: Int = getColumnIndexOrThrow(_stmt, "currentProgress")
        val _columnIndexOfProgressPercentage: Int = getColumnIndexOrThrow(_stmt,
            "progressPercentage")
        val _columnIndexOfLastActivityDate: Int = getColumnIndexOrThrow(_stmt, "lastActivityDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPersonalBest: Int = getColumnIndexOrThrow(_stmt, "personalBest")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<ChallengeParticipationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChallengeParticipationEntity
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
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentProgress: Double
          _tmpCurrentProgress = _stmt.getDouble(_columnIndexOfCurrentProgress)
          val _tmpProgressPercentage: Double
          _tmpProgressPercentage = _stmt.getDouble(_columnIndexOfProgressPercentage)
          val _tmpLastActivityDate: String?
          if (_stmt.isNull(_columnIndexOfLastActivityDate)) {
            _tmpLastActivityDate = null
          } else {
            _tmpLastActivityDate = _stmt.getText(_columnIndexOfLastActivityDate)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpRank: Int?
          if (_stmt.isNull(_columnIndexOfRank)) {
            _tmpRank = null
          } else {
            _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          }
          val _tmpPersonalBest: Double?
          if (_stmt.isNull(_columnIndexOfPersonalBest)) {
            _tmpPersonalBest = null
          } else {
            _tmpPersonalBest = _stmt.getDouble(_columnIndexOfPersonalBest)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              ChallengeParticipationEntity(_tmpId,_tmpChallengeId,_tmpUserId,_tmpUserName,_tmpStatus,_tmpCurrentProgress,_tmpProgressPercentage,_tmpLastActivityDate,_tmpCompletedAt,_tmpJoinedAt,_tmpRank,_tmpPersonalBest,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUserParticipation(challengeId: Long, userId: String):
      ChallengeParticipationEntity? {
    val _sql: String = "SELECT * FROM challenge_participations WHERE challengeId = ? AND userId = ?"
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
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCurrentProgress: Int = getColumnIndexOrThrow(_stmt, "currentProgress")
        val _columnIndexOfProgressPercentage: Int = getColumnIndexOrThrow(_stmt,
            "progressPercentage")
        val _columnIndexOfLastActivityDate: Int = getColumnIndexOrThrow(_stmt, "lastActivityDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPersonalBest: Int = getColumnIndexOrThrow(_stmt, "personalBest")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: ChallengeParticipationEntity?
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
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentProgress: Double
          _tmpCurrentProgress = _stmt.getDouble(_columnIndexOfCurrentProgress)
          val _tmpProgressPercentage: Double
          _tmpProgressPercentage = _stmt.getDouble(_columnIndexOfProgressPercentage)
          val _tmpLastActivityDate: String?
          if (_stmt.isNull(_columnIndexOfLastActivityDate)) {
            _tmpLastActivityDate = null
          } else {
            _tmpLastActivityDate = _stmt.getText(_columnIndexOfLastActivityDate)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpRank: Int?
          if (_stmt.isNull(_columnIndexOfRank)) {
            _tmpRank = null
          } else {
            _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          }
          val _tmpPersonalBest: Double?
          if (_stmt.isNull(_columnIndexOfPersonalBest)) {
            _tmpPersonalBest = null
          } else {
            _tmpPersonalBest = _stmt.getDouble(_columnIndexOfPersonalBest)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _result =
              ChallengeParticipationEntity(_tmpId,_tmpChallengeId,_tmpUserId,_tmpUserName,_tmpStatus,_tmpCurrentProgress,_tmpProgressPercentage,_tmpLastActivityDate,_tmpCompletedAt,_tmpJoinedAt,_tmpRank,_tmpPersonalBest,_tmpNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getParticipantCount(challengeId: Long): Int {
    val _sql: String = "SELECT COUNT(*) FROM challenge_participations WHERE challengeId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, challengeId)
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

  public override suspend fun getTopParticipants(challengeId: Long, limit: Int):
      List<ChallengeParticipationEntity> {
    val _sql: String =
        "SELECT * FROM challenge_participations WHERE challengeId = ? ORDER BY currentProgress DESC LIMIT ?"
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
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCurrentProgress: Int = getColumnIndexOrThrow(_stmt, "currentProgress")
        val _columnIndexOfProgressPercentage: Int = getColumnIndexOrThrow(_stmt,
            "progressPercentage")
        val _columnIndexOfLastActivityDate: Int = getColumnIndexOrThrow(_stmt, "lastActivityDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPersonalBest: Int = getColumnIndexOrThrow(_stmt, "personalBest")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<ChallengeParticipationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChallengeParticipationEntity
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
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCurrentProgress: Double
          _tmpCurrentProgress = _stmt.getDouble(_columnIndexOfCurrentProgress)
          val _tmpProgressPercentage: Double
          _tmpProgressPercentage = _stmt.getDouble(_columnIndexOfProgressPercentage)
          val _tmpLastActivityDate: String?
          if (_stmt.isNull(_columnIndexOfLastActivityDate)) {
            _tmpLastActivityDate = null
          } else {
            _tmpLastActivityDate = _stmt.getText(_columnIndexOfLastActivityDate)
          }
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpRank: Int?
          if (_stmt.isNull(_columnIndexOfRank)) {
            _tmpRank = null
          } else {
            _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          }
          val _tmpPersonalBest: Double?
          if (_stmt.isNull(_columnIndexOfPersonalBest)) {
            _tmpPersonalBest = null
          } else {
            _tmpPersonalBest = _stmt.getDouble(_columnIndexOfPersonalBest)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              ChallengeParticipationEntity(_tmpId,_tmpChallengeId,_tmpUserId,_tmpUserName,_tmpStatus,_tmpCurrentProgress,_tmpProgressPercentage,_tmpLastActivityDate,_tmpCompletedAt,_tmpJoinedAt,_tmpRank,_tmpPersonalBest,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM challenge_participations WHERE id = ?"
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

  public override suspend fun updateProgress(
    id: Long,
    progress: Double,
    percentage: Double,
    date: String,
  ) {
    val _sql: String =
        "UPDATE challenge_participations SET currentProgress = ?, progressPercentage = ?, lastActivityDate = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, progress)
        _argIndex = 2
        _stmt.bindDouble(_argIndex, percentage)
        _argIndex = 3
        _stmt.bindText(_argIndex, date)
        _argIndex = 4
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateStatus(
    id: Long,
    status: String,
    completedAt: Long?,
  ) {
    val _sql: String =
        "UPDATE challenge_participations SET status = ?, completedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        if (completedAt == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, completedAt)
        }
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateRank(id: Long, rank: Int) {
    val _sql: String = "UPDATE challenge_participations SET rank = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, rank.toLong())
        _argIndex = 2
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
