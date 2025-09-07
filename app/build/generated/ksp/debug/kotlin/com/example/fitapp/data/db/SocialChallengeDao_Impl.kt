package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class SocialChallengeDao_Impl(
  __db: RoomDatabase,
) : SocialChallengeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSocialChallengeEntity: EntityInsertAdapter<SocialChallengeEntity>

  private val __updateAdapterOfSocialChallengeEntity:
      EntityDeleteOrUpdateAdapter<SocialChallengeEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSocialChallengeEntity = object :
        EntityInsertAdapter<SocialChallengeEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `social_challenges` (`id`,`title`,`description`,`category`,`challengeType`,`targetMetric`,`targetValue`,`unit`,`duration`,`startDate`,`endDate`,`maxParticipants`,`currentParticipants`,`status`,`creatorId`,`reward`,`difficulty`,`imageUrl`,`rules`,`isOfficial`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SocialChallengeEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.challengeType)
        statement.bindText(6, entity.targetMetric)
        statement.bindDouble(7, entity.targetValue)
        statement.bindText(8, entity.unit)
        statement.bindLong(9, entity.duration.toLong())
        statement.bindText(10, entity.startDate)
        statement.bindText(11, entity.endDate)
        val _tmpMaxParticipants: Int? = entity.maxParticipants
        if (_tmpMaxParticipants == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpMaxParticipants.toLong())
        }
        statement.bindLong(13, entity.currentParticipants.toLong())
        statement.bindText(14, entity.status)
        val _tmpCreatorId: String? = entity.creatorId
        if (_tmpCreatorId == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpCreatorId)
        }
        val _tmpReward: String? = entity.reward
        if (_tmpReward == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpReward)
        }
        statement.bindText(17, entity.difficulty)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpImageUrl)
        }
        val _tmpRules: String? = entity.rules
        if (_tmpRules == null) {
          statement.bindNull(19)
        } else {
          statement.bindText(19, _tmpRules)
        }
        val _tmp: Int = if (entity.isOfficial) 1 else 0
        statement.bindLong(20, _tmp.toLong())
        statement.bindLong(21, entity.createdAt)
      }
    }
    this.__updateAdapterOfSocialChallengeEntity = object :
        EntityDeleteOrUpdateAdapter<SocialChallengeEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `social_challenges` SET `id` = ?,`title` = ?,`description` = ?,`category` = ?,`challengeType` = ?,`targetMetric` = ?,`targetValue` = ?,`unit` = ?,`duration` = ?,`startDate` = ?,`endDate` = ?,`maxParticipants` = ?,`currentParticipants` = ?,`status` = ?,`creatorId` = ?,`reward` = ?,`difficulty` = ?,`imageUrl` = ?,`rules` = ?,`isOfficial` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SocialChallengeEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.challengeType)
        statement.bindText(6, entity.targetMetric)
        statement.bindDouble(7, entity.targetValue)
        statement.bindText(8, entity.unit)
        statement.bindLong(9, entity.duration.toLong())
        statement.bindText(10, entity.startDate)
        statement.bindText(11, entity.endDate)
        val _tmpMaxParticipants: Int? = entity.maxParticipants
        if (_tmpMaxParticipants == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpMaxParticipants.toLong())
        }
        statement.bindLong(13, entity.currentParticipants.toLong())
        statement.bindText(14, entity.status)
        val _tmpCreatorId: String? = entity.creatorId
        if (_tmpCreatorId == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpCreatorId)
        }
        val _tmpReward: String? = entity.reward
        if (_tmpReward == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpReward)
        }
        statement.bindText(17, entity.difficulty)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpImageUrl)
        }
        val _tmpRules: String? = entity.rules
        if (_tmpRules == null) {
          statement.bindNull(19)
        } else {
          statement.bindText(19, _tmpRules)
        }
        val _tmp: Int = if (entity.isOfficial) 1 else 0
        statement.bindLong(20, _tmp.toLong())
        statement.bindLong(21, entity.createdAt)
        statement.bindLong(22, entity.id)
      }
    }
  }

  public override suspend fun insert(challenge: SocialChallengeEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSocialChallengeEntity.insertAndReturnId(_connection,
        challenge)
    _result
  }

  public override suspend fun update(challenge: SocialChallengeEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSocialChallengeEntity.handle(_connection, challenge)
  }

  public override fun allChallengesFlow(): Flow<List<SocialChallengeEntity>> {
    val _sql: String =
        "SELECT `social_challenges`.`id` AS `id`, `social_challenges`.`title` AS `title`, `social_challenges`.`description` AS `description`, `social_challenges`.`category` AS `category`, `social_challenges`.`challengeType` AS `challengeType`, `social_challenges`.`targetMetric` AS `targetMetric`, `social_challenges`.`targetValue` AS `targetValue`, `social_challenges`.`unit` AS `unit`, `social_challenges`.`duration` AS `duration`, `social_challenges`.`startDate` AS `startDate`, `social_challenges`.`endDate` AS `endDate`, `social_challenges`.`maxParticipants` AS `maxParticipants`, `social_challenges`.`currentParticipants` AS `currentParticipants`, `social_challenges`.`status` AS `status`, `social_challenges`.`creatorId` AS `creatorId`, `social_challenges`.`reward` AS `reward`, `social_challenges`.`difficulty` AS `difficulty`, `social_challenges`.`imageUrl` AS `imageUrl`, `social_challenges`.`rules` AS `rules`, `social_challenges`.`isOfficial` AS `isOfficial`, `social_challenges`.`createdAt` AS `createdAt` FROM social_challenges ORDER BY startDate DESC"
    return createFlow(__db, false, arrayOf("social_challenges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfChallengeType: Int = 4
        val _columnIndexOfTargetMetric: Int = 5
        val _columnIndexOfTargetValue: Int = 6
        val _columnIndexOfUnit: Int = 7
        val _columnIndexOfDuration: Int = 8
        val _columnIndexOfStartDate: Int = 9
        val _columnIndexOfEndDate: Int = 10
        val _columnIndexOfMaxParticipants: Int = 11
        val _columnIndexOfCurrentParticipants: Int = 12
        val _columnIndexOfStatus: Int = 13
        val _columnIndexOfCreatorId: Int = 14
        val _columnIndexOfReward: Int = 15
        val _columnIndexOfDifficulty: Int = 16
        val _columnIndexOfImageUrl: Int = 17
        val _columnIndexOfRules: Int = 18
        val _columnIndexOfIsOfficial: Int = 19
        val _columnIndexOfCreatedAt: Int = 20
        val _result: MutableList<SocialChallengeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialChallengeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpChallengeType: String
          _tmpChallengeType = _stmt.getText(_columnIndexOfChallengeType)
          val _tmpTargetMetric: String
          _tmpTargetMetric = _stmt.getText(_columnIndexOfTargetMetric)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String
          _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          val _tmpMaxParticipants: Int?
          if (_stmt.isNull(_columnIndexOfMaxParticipants)) {
            _tmpMaxParticipants = null
          } else {
            _tmpMaxParticipants = _stmt.getLong(_columnIndexOfMaxParticipants).toInt()
          }
          val _tmpCurrentParticipants: Int
          _tmpCurrentParticipants = _stmt.getLong(_columnIndexOfCurrentParticipants).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatorId: String?
          if (_stmt.isNull(_columnIndexOfCreatorId)) {
            _tmpCreatorId = null
          } else {
            _tmpCreatorId = _stmt.getText(_columnIndexOfCreatorId)
          }
          val _tmpReward: String?
          if (_stmt.isNull(_columnIndexOfReward)) {
            _tmpReward = null
          } else {
            _tmpReward = _stmt.getText(_columnIndexOfReward)
          }
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpRules: String?
          if (_stmt.isNull(_columnIndexOfRules)) {
            _tmpRules = null
          } else {
            _tmpRules = _stmt.getText(_columnIndexOfRules)
          }
          val _tmpIsOfficial: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOfficial).toInt()
          _tmpIsOfficial = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialChallengeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpChallengeType,_tmpTargetMetric,_tmpTargetValue,_tmpUnit,_tmpDuration,_tmpStartDate,_tmpEndDate,_tmpMaxParticipants,_tmpCurrentParticipants,_tmpStatus,_tmpCreatorId,_tmpReward,_tmpDifficulty,_tmpImageUrl,_tmpRules,_tmpIsOfficial,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun challengesByStatusFlow(status: String): Flow<List<SocialChallengeEntity>> {
    val _sql: String = "SELECT * FROM social_challenges WHERE status = ? ORDER BY startDate DESC"
    return createFlow(__db, false, arrayOf("social_challenges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfChallengeType: Int = getColumnIndexOrThrow(_stmt, "challengeType")
        val _columnIndexOfTargetMetric: Int = getColumnIndexOrThrow(_stmt, "targetMetric")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfMaxParticipants: Int = getColumnIndexOrThrow(_stmt, "maxParticipants")
        val _columnIndexOfCurrentParticipants: Int = getColumnIndexOrThrow(_stmt,
            "currentParticipants")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatorId: Int = getColumnIndexOrThrow(_stmt, "creatorId")
        val _columnIndexOfReward: Int = getColumnIndexOrThrow(_stmt, "reward")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfRules: Int = getColumnIndexOrThrow(_stmt, "rules")
        val _columnIndexOfIsOfficial: Int = getColumnIndexOrThrow(_stmt, "isOfficial")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SocialChallengeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialChallengeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpChallengeType: String
          _tmpChallengeType = _stmt.getText(_columnIndexOfChallengeType)
          val _tmpTargetMetric: String
          _tmpTargetMetric = _stmt.getText(_columnIndexOfTargetMetric)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String
          _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          val _tmpMaxParticipants: Int?
          if (_stmt.isNull(_columnIndexOfMaxParticipants)) {
            _tmpMaxParticipants = null
          } else {
            _tmpMaxParticipants = _stmt.getLong(_columnIndexOfMaxParticipants).toInt()
          }
          val _tmpCurrentParticipants: Int
          _tmpCurrentParticipants = _stmt.getLong(_columnIndexOfCurrentParticipants).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatorId: String?
          if (_stmt.isNull(_columnIndexOfCreatorId)) {
            _tmpCreatorId = null
          } else {
            _tmpCreatorId = _stmt.getText(_columnIndexOfCreatorId)
          }
          val _tmpReward: String?
          if (_stmt.isNull(_columnIndexOfReward)) {
            _tmpReward = null
          } else {
            _tmpReward = _stmt.getText(_columnIndexOfReward)
          }
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpRules: String?
          if (_stmt.isNull(_columnIndexOfRules)) {
            _tmpRules = null
          } else {
            _tmpRules = _stmt.getText(_columnIndexOfRules)
          }
          val _tmpIsOfficial: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOfficial).toInt()
          _tmpIsOfficial = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialChallengeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpChallengeType,_tmpTargetMetric,_tmpTargetValue,_tmpUnit,_tmpDuration,_tmpStartDate,_tmpEndDate,_tmpMaxParticipants,_tmpCurrentParticipants,_tmpStatus,_tmpCreatorId,_tmpReward,_tmpDifficulty,_tmpImageUrl,_tmpRules,_tmpIsOfficial,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun challengesByCategoryFlow(category: String):
      Flow<List<SocialChallengeEntity>> {
    val _sql: String = "SELECT * FROM social_challenges WHERE category = ? ORDER BY startDate DESC"
    return createFlow(__db, false, arrayOf("social_challenges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfChallengeType: Int = getColumnIndexOrThrow(_stmt, "challengeType")
        val _columnIndexOfTargetMetric: Int = getColumnIndexOrThrow(_stmt, "targetMetric")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfMaxParticipants: Int = getColumnIndexOrThrow(_stmt, "maxParticipants")
        val _columnIndexOfCurrentParticipants: Int = getColumnIndexOrThrow(_stmt,
            "currentParticipants")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatorId: Int = getColumnIndexOrThrow(_stmt, "creatorId")
        val _columnIndexOfReward: Int = getColumnIndexOrThrow(_stmt, "reward")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfRules: Int = getColumnIndexOrThrow(_stmt, "rules")
        val _columnIndexOfIsOfficial: Int = getColumnIndexOrThrow(_stmt, "isOfficial")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SocialChallengeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialChallengeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpChallengeType: String
          _tmpChallengeType = _stmt.getText(_columnIndexOfChallengeType)
          val _tmpTargetMetric: String
          _tmpTargetMetric = _stmt.getText(_columnIndexOfTargetMetric)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String
          _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          val _tmpMaxParticipants: Int?
          if (_stmt.isNull(_columnIndexOfMaxParticipants)) {
            _tmpMaxParticipants = null
          } else {
            _tmpMaxParticipants = _stmt.getLong(_columnIndexOfMaxParticipants).toInt()
          }
          val _tmpCurrentParticipants: Int
          _tmpCurrentParticipants = _stmt.getLong(_columnIndexOfCurrentParticipants).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatorId: String?
          if (_stmt.isNull(_columnIndexOfCreatorId)) {
            _tmpCreatorId = null
          } else {
            _tmpCreatorId = _stmt.getText(_columnIndexOfCreatorId)
          }
          val _tmpReward: String?
          if (_stmt.isNull(_columnIndexOfReward)) {
            _tmpReward = null
          } else {
            _tmpReward = _stmt.getText(_columnIndexOfReward)
          }
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpRules: String?
          if (_stmt.isNull(_columnIndexOfRules)) {
            _tmpRules = null
          } else {
            _tmpRules = _stmt.getText(_columnIndexOfRules)
          }
          val _tmpIsOfficial: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOfficial).toInt()
          _tmpIsOfficial = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialChallengeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpChallengeType,_tmpTargetMetric,_tmpTargetValue,_tmpUnit,_tmpDuration,_tmpStartDate,_tmpEndDate,_tmpMaxParticipants,_tmpCurrentParticipants,_tmpStatus,_tmpCreatorId,_tmpReward,_tmpDifficulty,_tmpImageUrl,_tmpRules,_tmpIsOfficial,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun activeChallengesFlow(): Flow<List<SocialChallengeEntity>> {
    val _sql: String =
        "SELECT `social_challenges`.`id` AS `id`, `social_challenges`.`title` AS `title`, `social_challenges`.`description` AS `description`, `social_challenges`.`category` AS `category`, `social_challenges`.`challengeType` AS `challengeType`, `social_challenges`.`targetMetric` AS `targetMetric`, `social_challenges`.`targetValue` AS `targetValue`, `social_challenges`.`unit` AS `unit`, `social_challenges`.`duration` AS `duration`, `social_challenges`.`startDate` AS `startDate`, `social_challenges`.`endDate` AS `endDate`, `social_challenges`.`maxParticipants` AS `maxParticipants`, `social_challenges`.`currentParticipants` AS `currentParticipants`, `social_challenges`.`status` AS `status`, `social_challenges`.`creatorId` AS `creatorId`, `social_challenges`.`reward` AS `reward`, `social_challenges`.`difficulty` AS `difficulty`, `social_challenges`.`imageUrl` AS `imageUrl`, `social_challenges`.`rules` AS `rules`, `social_challenges`.`isOfficial` AS `isOfficial`, `social_challenges`.`createdAt` AS `createdAt` FROM social_challenges WHERE status = 'active' ORDER BY startDate ASC"
    return createFlow(__db, false, arrayOf("social_challenges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfChallengeType: Int = 4
        val _columnIndexOfTargetMetric: Int = 5
        val _columnIndexOfTargetValue: Int = 6
        val _columnIndexOfUnit: Int = 7
        val _columnIndexOfDuration: Int = 8
        val _columnIndexOfStartDate: Int = 9
        val _columnIndexOfEndDate: Int = 10
        val _columnIndexOfMaxParticipants: Int = 11
        val _columnIndexOfCurrentParticipants: Int = 12
        val _columnIndexOfStatus: Int = 13
        val _columnIndexOfCreatorId: Int = 14
        val _columnIndexOfReward: Int = 15
        val _columnIndexOfDifficulty: Int = 16
        val _columnIndexOfImageUrl: Int = 17
        val _columnIndexOfRules: Int = 18
        val _columnIndexOfIsOfficial: Int = 19
        val _columnIndexOfCreatedAt: Int = 20
        val _result: MutableList<SocialChallengeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialChallengeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpChallengeType: String
          _tmpChallengeType = _stmt.getText(_columnIndexOfChallengeType)
          val _tmpTargetMetric: String
          _tmpTargetMetric = _stmt.getText(_columnIndexOfTargetMetric)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String
          _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          val _tmpMaxParticipants: Int?
          if (_stmt.isNull(_columnIndexOfMaxParticipants)) {
            _tmpMaxParticipants = null
          } else {
            _tmpMaxParticipants = _stmt.getLong(_columnIndexOfMaxParticipants).toInt()
          }
          val _tmpCurrentParticipants: Int
          _tmpCurrentParticipants = _stmt.getLong(_columnIndexOfCurrentParticipants).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatorId: String?
          if (_stmt.isNull(_columnIndexOfCreatorId)) {
            _tmpCreatorId = null
          } else {
            _tmpCreatorId = _stmt.getText(_columnIndexOfCreatorId)
          }
          val _tmpReward: String?
          if (_stmt.isNull(_columnIndexOfReward)) {
            _tmpReward = null
          } else {
            _tmpReward = _stmt.getText(_columnIndexOfReward)
          }
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpRules: String?
          if (_stmt.isNull(_columnIndexOfRules)) {
            _tmpRules = null
          } else {
            _tmpRules = _stmt.getText(_columnIndexOfRules)
          }
          val _tmpIsOfficial: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOfficial).toInt()
          _tmpIsOfficial = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialChallengeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpChallengeType,_tmpTargetMetric,_tmpTargetValue,_tmpUnit,_tmpDuration,_tmpStartDate,_tmpEndDate,_tmpMaxParticipants,_tmpCurrentParticipants,_tmpStatus,_tmpCreatorId,_tmpReward,_tmpDifficulty,_tmpImageUrl,_tmpRules,_tmpIsOfficial,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun officialChallengesFlow(): Flow<List<SocialChallengeEntity>> {
    val _sql: String =
        "SELECT `social_challenges`.`id` AS `id`, `social_challenges`.`title` AS `title`, `social_challenges`.`description` AS `description`, `social_challenges`.`category` AS `category`, `social_challenges`.`challengeType` AS `challengeType`, `social_challenges`.`targetMetric` AS `targetMetric`, `social_challenges`.`targetValue` AS `targetValue`, `social_challenges`.`unit` AS `unit`, `social_challenges`.`duration` AS `duration`, `social_challenges`.`startDate` AS `startDate`, `social_challenges`.`endDate` AS `endDate`, `social_challenges`.`maxParticipants` AS `maxParticipants`, `social_challenges`.`currentParticipants` AS `currentParticipants`, `social_challenges`.`status` AS `status`, `social_challenges`.`creatorId` AS `creatorId`, `social_challenges`.`reward` AS `reward`, `social_challenges`.`difficulty` AS `difficulty`, `social_challenges`.`imageUrl` AS `imageUrl`, `social_challenges`.`rules` AS `rules`, `social_challenges`.`isOfficial` AS `isOfficial`, `social_challenges`.`createdAt` AS `createdAt` FROM social_challenges WHERE isOfficial = 1 ORDER BY startDate DESC"
    return createFlow(__db, false, arrayOf("social_challenges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfChallengeType: Int = 4
        val _columnIndexOfTargetMetric: Int = 5
        val _columnIndexOfTargetValue: Int = 6
        val _columnIndexOfUnit: Int = 7
        val _columnIndexOfDuration: Int = 8
        val _columnIndexOfStartDate: Int = 9
        val _columnIndexOfEndDate: Int = 10
        val _columnIndexOfMaxParticipants: Int = 11
        val _columnIndexOfCurrentParticipants: Int = 12
        val _columnIndexOfStatus: Int = 13
        val _columnIndexOfCreatorId: Int = 14
        val _columnIndexOfReward: Int = 15
        val _columnIndexOfDifficulty: Int = 16
        val _columnIndexOfImageUrl: Int = 17
        val _columnIndexOfRules: Int = 18
        val _columnIndexOfIsOfficial: Int = 19
        val _columnIndexOfCreatedAt: Int = 20
        val _result: MutableList<SocialChallengeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialChallengeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpChallengeType: String
          _tmpChallengeType = _stmt.getText(_columnIndexOfChallengeType)
          val _tmpTargetMetric: String
          _tmpTargetMetric = _stmt.getText(_columnIndexOfTargetMetric)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String
          _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          val _tmpMaxParticipants: Int?
          if (_stmt.isNull(_columnIndexOfMaxParticipants)) {
            _tmpMaxParticipants = null
          } else {
            _tmpMaxParticipants = _stmt.getLong(_columnIndexOfMaxParticipants).toInt()
          }
          val _tmpCurrentParticipants: Int
          _tmpCurrentParticipants = _stmt.getLong(_columnIndexOfCurrentParticipants).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatorId: String?
          if (_stmt.isNull(_columnIndexOfCreatorId)) {
            _tmpCreatorId = null
          } else {
            _tmpCreatorId = _stmt.getText(_columnIndexOfCreatorId)
          }
          val _tmpReward: String?
          if (_stmt.isNull(_columnIndexOfReward)) {
            _tmpReward = null
          } else {
            _tmpReward = _stmt.getText(_columnIndexOfReward)
          }
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpRules: String?
          if (_stmt.isNull(_columnIndexOfRules)) {
            _tmpRules = null
          } else {
            _tmpRules = _stmt.getText(_columnIndexOfRules)
          }
          val _tmpIsOfficial: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOfficial).toInt()
          _tmpIsOfficial = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialChallengeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpChallengeType,_tmpTargetMetric,_tmpTargetValue,_tmpUnit,_tmpDuration,_tmpStartDate,_tmpEndDate,_tmpMaxParticipants,_tmpCurrentParticipants,_tmpStatus,_tmpCreatorId,_tmpReward,_tmpDifficulty,_tmpImageUrl,_tmpRules,_tmpIsOfficial,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getChallenge(id: Long): SocialChallengeEntity? {
    val _sql: String = "SELECT * FROM social_challenges WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfChallengeType: Int = getColumnIndexOrThrow(_stmt, "challengeType")
        val _columnIndexOfTargetMetric: Int = getColumnIndexOrThrow(_stmt, "targetMetric")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfMaxParticipants: Int = getColumnIndexOrThrow(_stmt, "maxParticipants")
        val _columnIndexOfCurrentParticipants: Int = getColumnIndexOrThrow(_stmt,
            "currentParticipants")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatorId: Int = getColumnIndexOrThrow(_stmt, "creatorId")
        val _columnIndexOfReward: Int = getColumnIndexOrThrow(_stmt, "reward")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfRules: Int = getColumnIndexOrThrow(_stmt, "rules")
        val _columnIndexOfIsOfficial: Int = getColumnIndexOrThrow(_stmt, "isOfficial")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: SocialChallengeEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpChallengeType: String
          _tmpChallengeType = _stmt.getText(_columnIndexOfChallengeType)
          val _tmpTargetMetric: String
          _tmpTargetMetric = _stmt.getText(_columnIndexOfTargetMetric)
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpStartDate: String
          _tmpStartDate = _stmt.getText(_columnIndexOfStartDate)
          val _tmpEndDate: String
          _tmpEndDate = _stmt.getText(_columnIndexOfEndDate)
          val _tmpMaxParticipants: Int?
          if (_stmt.isNull(_columnIndexOfMaxParticipants)) {
            _tmpMaxParticipants = null
          } else {
            _tmpMaxParticipants = _stmt.getLong(_columnIndexOfMaxParticipants).toInt()
          }
          val _tmpCurrentParticipants: Int
          _tmpCurrentParticipants = _stmt.getLong(_columnIndexOfCurrentParticipants).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatorId: String?
          if (_stmt.isNull(_columnIndexOfCreatorId)) {
            _tmpCreatorId = null
          } else {
            _tmpCreatorId = _stmt.getText(_columnIndexOfCreatorId)
          }
          val _tmpReward: String?
          if (_stmt.isNull(_columnIndexOfReward)) {
            _tmpReward = null
          } else {
            _tmpReward = _stmt.getText(_columnIndexOfReward)
          }
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpRules: String?
          if (_stmt.isNull(_columnIndexOfRules)) {
            _tmpRules = null
          } else {
            _tmpRules = _stmt.getText(_columnIndexOfRules)
          }
          val _tmpIsOfficial: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOfficial).toInt()
          _tmpIsOfficial = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              SocialChallengeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpChallengeType,_tmpTargetMetric,_tmpTargetValue,_tmpUnit,_tmpDuration,_tmpStartDate,_tmpEndDate,_tmpMaxParticipants,_tmpCurrentParticipants,_tmpStatus,_tmpCreatorId,_tmpReward,_tmpDifficulty,_tmpImageUrl,_tmpRules,_tmpIsOfficial,_tmpCreatedAt)
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
    val _sql: String = "DELETE FROM social_challenges WHERE id = ?"
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

  public override suspend fun updateParticipantCount(id: Long, count: Int) {
    val _sql: String = "UPDATE social_challenges SET currentParticipants = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, count.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateStatus(id: Long, status: String) {
    val _sql: String = "UPDATE social_challenges SET status = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
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
