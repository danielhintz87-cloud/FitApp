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
public class SocialBadgeDao_Impl(
  __db: RoomDatabase,
) : SocialBadgeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSocialBadgeEntity: EntityInsertAdapter<SocialBadgeEntity>

  private val __updateAdapterOfSocialBadgeEntity: EntityDeleteOrUpdateAdapter<SocialBadgeEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSocialBadgeEntity = object : EntityInsertAdapter<SocialBadgeEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `social_badges` (`id`,`title`,`description`,`category`,`badgeType`,`iconName`,`rarity`,`requirements`,`challengeId`,`isUnlocked`,`unlockedAt`,`progress`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SocialBadgeEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.badgeType)
        statement.bindText(6, entity.iconName)
        statement.bindText(7, entity.rarity)
        statement.bindText(8, entity.requirements)
        val _tmpChallengeId: Long? = entity.challengeId
        if (_tmpChallengeId == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpChallengeId)
        }
        val _tmp: Int = if (entity.isUnlocked) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        val _tmpUnlockedAt: Long? = entity.unlockedAt
        if (_tmpUnlockedAt == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpUnlockedAt)
        }
        statement.bindDouble(12, entity.progress)
        statement.bindLong(13, entity.createdAt)
      }
    }
    this.__updateAdapterOfSocialBadgeEntity = object :
        EntityDeleteOrUpdateAdapter<SocialBadgeEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `social_badges` SET `id` = ?,`title` = ?,`description` = ?,`category` = ?,`badgeType` = ?,`iconName` = ?,`rarity` = ?,`requirements` = ?,`challengeId` = ?,`isUnlocked` = ?,`unlockedAt` = ?,`progress` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SocialBadgeEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.badgeType)
        statement.bindText(6, entity.iconName)
        statement.bindText(7, entity.rarity)
        statement.bindText(8, entity.requirements)
        val _tmpChallengeId: Long? = entity.challengeId
        if (_tmpChallengeId == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpChallengeId)
        }
        val _tmp: Int = if (entity.isUnlocked) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        val _tmpUnlockedAt: Long? = entity.unlockedAt
        if (_tmpUnlockedAt == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpUnlockedAt)
        }
        statement.bindDouble(12, entity.progress)
        statement.bindLong(13, entity.createdAt)
        statement.bindLong(14, entity.id)
      }
    }
  }

  public override suspend fun insert(badge: SocialBadgeEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfSocialBadgeEntity.insertAndReturnId(_connection, badge)
    _result
  }

  public override suspend fun update(badge: SocialBadgeEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfSocialBadgeEntity.handle(_connection, badge)
  }

  public override fun allBadgesFlow(): Flow<List<SocialBadgeEntity>> {
    val _sql: String =
        "SELECT `social_badges`.`id` AS `id`, `social_badges`.`title` AS `title`, `social_badges`.`description` AS `description`, `social_badges`.`category` AS `category`, `social_badges`.`badgeType` AS `badgeType`, `social_badges`.`iconName` AS `iconName`, `social_badges`.`rarity` AS `rarity`, `social_badges`.`requirements` AS `requirements`, `social_badges`.`challengeId` AS `challengeId`, `social_badges`.`isUnlocked` AS `isUnlocked`, `social_badges`.`unlockedAt` AS `unlockedAt`, `social_badges`.`progress` AS `progress`, `social_badges`.`createdAt` AS `createdAt` FROM social_badges ORDER BY unlockedAt DESC"
    return createFlow(__db, false, arrayOf("social_badges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfBadgeType: Int = 4
        val _columnIndexOfIconName: Int = 5
        val _columnIndexOfRarity: Int = 6
        val _columnIndexOfRequirements: Int = 7
        val _columnIndexOfChallengeId: Int = 8
        val _columnIndexOfIsUnlocked: Int = 9
        val _columnIndexOfUnlockedAt: Int = 10
        val _columnIndexOfProgress: Int = 11
        val _columnIndexOfCreatedAt: Int = 12
        val _result: MutableList<SocialBadgeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialBadgeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpBadgeType: String
          _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpRarity: String
          _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          val _tmpRequirements: String
          _tmpRequirements = _stmt.getText(_columnIndexOfRequirements)
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpIsUnlocked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUnlocked).toInt()
          _tmpIsUnlocked = _tmp != 0
          val _tmpUnlockedAt: Long?
          if (_stmt.isNull(_columnIndexOfUnlockedAt)) {
            _tmpUnlockedAt = null
          } else {
            _tmpUnlockedAt = _stmt.getLong(_columnIndexOfUnlockedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialBadgeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpBadgeType,_tmpIconName,_tmpRarity,_tmpRequirements,_tmpChallengeId,_tmpIsUnlocked,_tmpUnlockedAt,_tmpProgress,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun badgesByUnlockStatusFlow(unlocked: Boolean): Flow<List<SocialBadgeEntity>> {
    val _sql: String = "SELECT * FROM social_badges WHERE isUnlocked = ? ORDER BY unlockedAt DESC"
    return createFlow(__db, false, arrayOf("social_badges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (unlocked) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfBadgeType: Int = getColumnIndexOrThrow(_stmt, "badgeType")
        val _columnIndexOfIconName: Int = getColumnIndexOrThrow(_stmt, "iconName")
        val _columnIndexOfRarity: Int = getColumnIndexOrThrow(_stmt, "rarity")
        val _columnIndexOfRequirements: Int = getColumnIndexOrThrow(_stmt, "requirements")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfIsUnlocked: Int = getColumnIndexOrThrow(_stmt, "isUnlocked")
        val _columnIndexOfUnlockedAt: Int = getColumnIndexOrThrow(_stmt, "unlockedAt")
        val _columnIndexOfProgress: Int = getColumnIndexOrThrow(_stmt, "progress")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SocialBadgeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialBadgeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpBadgeType: String
          _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpRarity: String
          _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          val _tmpRequirements: String
          _tmpRequirements = _stmt.getText(_columnIndexOfRequirements)
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpIsUnlocked: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsUnlocked).toInt()
          _tmpIsUnlocked = _tmp_1 != 0
          val _tmpUnlockedAt: Long?
          if (_stmt.isNull(_columnIndexOfUnlockedAt)) {
            _tmpUnlockedAt = null
          } else {
            _tmpUnlockedAt = _stmt.getLong(_columnIndexOfUnlockedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialBadgeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpBadgeType,_tmpIconName,_tmpRarity,_tmpRequirements,_tmpChallengeId,_tmpIsUnlocked,_tmpUnlockedAt,_tmpProgress,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun badgesByCategoryFlow(category: String): Flow<List<SocialBadgeEntity>> {
    val _sql: String = "SELECT * FROM social_badges WHERE category = ? ORDER BY unlockedAt DESC"
    return createFlow(__db, false, arrayOf("social_badges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfBadgeType: Int = getColumnIndexOrThrow(_stmt, "badgeType")
        val _columnIndexOfIconName: Int = getColumnIndexOrThrow(_stmt, "iconName")
        val _columnIndexOfRarity: Int = getColumnIndexOrThrow(_stmt, "rarity")
        val _columnIndexOfRequirements: Int = getColumnIndexOrThrow(_stmt, "requirements")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfIsUnlocked: Int = getColumnIndexOrThrow(_stmt, "isUnlocked")
        val _columnIndexOfUnlockedAt: Int = getColumnIndexOrThrow(_stmt, "unlockedAt")
        val _columnIndexOfProgress: Int = getColumnIndexOrThrow(_stmt, "progress")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SocialBadgeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialBadgeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpBadgeType: String
          _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpRarity: String
          _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          val _tmpRequirements: String
          _tmpRequirements = _stmt.getText(_columnIndexOfRequirements)
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpIsUnlocked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUnlocked).toInt()
          _tmpIsUnlocked = _tmp != 0
          val _tmpUnlockedAt: Long?
          if (_stmt.isNull(_columnIndexOfUnlockedAt)) {
            _tmpUnlockedAt = null
          } else {
            _tmpUnlockedAt = _stmt.getLong(_columnIndexOfUnlockedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialBadgeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpBadgeType,_tmpIconName,_tmpRarity,_tmpRequirements,_tmpChallengeId,_tmpIsUnlocked,_tmpUnlockedAt,_tmpProgress,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun badgesByTypeFlow(type: String): Flow<List<SocialBadgeEntity>> {
    val _sql: String = "SELECT * FROM social_badges WHERE badgeType = ? ORDER BY unlockedAt DESC"
    return createFlow(__db, false, arrayOf("social_badges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfBadgeType: Int = getColumnIndexOrThrow(_stmt, "badgeType")
        val _columnIndexOfIconName: Int = getColumnIndexOrThrow(_stmt, "iconName")
        val _columnIndexOfRarity: Int = getColumnIndexOrThrow(_stmt, "rarity")
        val _columnIndexOfRequirements: Int = getColumnIndexOrThrow(_stmt, "requirements")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfIsUnlocked: Int = getColumnIndexOrThrow(_stmt, "isUnlocked")
        val _columnIndexOfUnlockedAt: Int = getColumnIndexOrThrow(_stmt, "unlockedAt")
        val _columnIndexOfProgress: Int = getColumnIndexOrThrow(_stmt, "progress")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SocialBadgeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SocialBadgeEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpBadgeType: String
          _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpRarity: String
          _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          val _tmpRequirements: String
          _tmpRequirements = _stmt.getText(_columnIndexOfRequirements)
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpIsUnlocked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUnlocked).toInt()
          _tmpIsUnlocked = _tmp != 0
          val _tmpUnlockedAt: Long?
          if (_stmt.isNull(_columnIndexOfUnlockedAt)) {
            _tmpUnlockedAt = null
          } else {
            _tmpUnlockedAt = _stmt.getLong(_columnIndexOfUnlockedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SocialBadgeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpBadgeType,_tmpIconName,_tmpRarity,_tmpRequirements,_tmpChallengeId,_tmpIsUnlocked,_tmpUnlockedAt,_tmpProgress,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBadge(id: Long): SocialBadgeEntity? {
    val _sql: String = "SELECT * FROM social_badges WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfBadgeType: Int = getColumnIndexOrThrow(_stmt, "badgeType")
        val _columnIndexOfIconName: Int = getColumnIndexOrThrow(_stmt, "iconName")
        val _columnIndexOfRarity: Int = getColumnIndexOrThrow(_stmt, "rarity")
        val _columnIndexOfRequirements: Int = getColumnIndexOrThrow(_stmt, "requirements")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfIsUnlocked: Int = getColumnIndexOrThrow(_stmt, "isUnlocked")
        val _columnIndexOfUnlockedAt: Int = getColumnIndexOrThrow(_stmt, "unlockedAt")
        val _columnIndexOfProgress: Int = getColumnIndexOrThrow(_stmt, "progress")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: SocialBadgeEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpBadgeType: String
          _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpRarity: String
          _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          val _tmpRequirements: String
          _tmpRequirements = _stmt.getText(_columnIndexOfRequirements)
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpIsUnlocked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUnlocked).toInt()
          _tmpIsUnlocked = _tmp != 0
          val _tmpUnlockedAt: Long?
          if (_stmt.isNull(_columnIndexOfUnlockedAt)) {
            _tmpUnlockedAt = null
          } else {
            _tmpUnlockedAt = _stmt.getLong(_columnIndexOfUnlockedAt)
          }
          val _tmpProgress: Double
          _tmpProgress = _stmt.getDouble(_columnIndexOfProgress)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              SocialBadgeEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpBadgeType,_tmpIconName,_tmpRarity,_tmpRequirements,_tmpChallengeId,_tmpIsUnlocked,_tmpUnlockedAt,_tmpProgress,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnlockedBadgeCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM social_badges WHERE isUnlocked = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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
    val _sql: String = "DELETE FROM social_badges WHERE id = ?"
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

  public override suspend fun updateUnlockStatus(
    id: Long,
    unlocked: Boolean,
    unlockedAt: Long?,
  ) {
    val _sql: String = "UPDATE social_badges SET isUnlocked = ?, unlockedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (unlocked) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        if (unlockedAt == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, unlockedAt)
        }
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateProgress(id: Long, progress: Double) {
    val _sql: String = "UPDATE social_badges SET progress = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, progress)
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
