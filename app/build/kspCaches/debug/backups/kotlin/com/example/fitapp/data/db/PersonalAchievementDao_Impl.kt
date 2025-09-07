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
public class PersonalAchievementDao_Impl(
  __db: RoomDatabase,
) : PersonalAchievementDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPersonalAchievementEntity:
      EntityInsertAdapter<PersonalAchievementEntity>

  private val __updateAdapterOfPersonalAchievementEntity:
      EntityDeleteOrUpdateAdapter<PersonalAchievementEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPersonalAchievementEntity = object :
        EntityInsertAdapter<PersonalAchievementEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `personal_achievements` (`id`,`title`,`description`,`category`,`iconName`,`targetValue`,`currentValue`,`unit`,`isCompleted`,`completedAt`,`createdAt`,`badgeType`,`rarity`,`socialVisible`,`challengeId`,`shareMessage`,`pointsValue`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PersonalAchievementEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.iconName)
        val _tmpTargetValue: Double? = entity.targetValue
        if (_tmpTargetValue == null) {
          statement.bindNull(6)
        } else {
          statement.bindDouble(6, _tmpTargetValue)
        }
        statement.bindDouble(7, entity.currentValue)
        val _tmpUnit: String? = entity.unit
        if (_tmpUnit == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpUnit)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAt)
        }
        statement.bindLong(11, entity.createdAt)
        val _tmpBadgeType: String? = entity.badgeType
        if (_tmpBadgeType == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpBadgeType)
        }
        val _tmpRarity: String? = entity.rarity
        if (_tmpRarity == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpRarity)
        }
        val _tmp_1: Int = if (entity.socialVisible) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
        val _tmpChallengeId: Long? = entity.challengeId
        if (_tmpChallengeId == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpChallengeId)
        }
        val _tmpShareMessage: String? = entity.shareMessage
        if (_tmpShareMessage == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpShareMessage)
        }
        statement.bindLong(17, entity.pointsValue.toLong())
      }
    }
    this.__updateAdapterOfPersonalAchievementEntity = object :
        EntityDeleteOrUpdateAdapter<PersonalAchievementEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `personal_achievements` SET `id` = ?,`title` = ?,`description` = ?,`category` = ?,`iconName` = ?,`targetValue` = ?,`currentValue` = ?,`unit` = ?,`isCompleted` = ?,`completedAt` = ?,`createdAt` = ?,`badgeType` = ?,`rarity` = ?,`socialVisible` = ?,`challengeId` = ?,`shareMessage` = ?,`pointsValue` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PersonalAchievementEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.iconName)
        val _tmpTargetValue: Double? = entity.targetValue
        if (_tmpTargetValue == null) {
          statement.bindNull(6)
        } else {
          statement.bindDouble(6, _tmpTargetValue)
        }
        statement.bindDouble(7, entity.currentValue)
        val _tmpUnit: String? = entity.unit
        if (_tmpUnit == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpUnit)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAt)
        }
        statement.bindLong(11, entity.createdAt)
        val _tmpBadgeType: String? = entity.badgeType
        if (_tmpBadgeType == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpBadgeType)
        }
        val _tmpRarity: String? = entity.rarity
        if (_tmpRarity == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpRarity)
        }
        val _tmp_1: Int = if (entity.socialVisible) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
        val _tmpChallengeId: Long? = entity.challengeId
        if (_tmpChallengeId == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpChallengeId)
        }
        val _tmpShareMessage: String? = entity.shareMessage
        if (_tmpShareMessage == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpShareMessage)
        }
        statement.bindLong(17, entity.pointsValue.toLong())
        statement.bindLong(18, entity.id)
      }
    }
  }

  public override suspend fun insert(achievement: PersonalAchievementEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPersonalAchievementEntity.insertAndReturnId(_connection,
        achievement)
    _result
  }

  public override suspend fun update(achievement: PersonalAchievementEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPersonalAchievementEntity.handle(_connection, achievement)
  }

  public override fun allAchievementsFlow(): Flow<List<PersonalAchievementEntity>> {
    val _sql: String =
        "SELECT `personal_achievements`.`id` AS `id`, `personal_achievements`.`title` AS `title`, `personal_achievements`.`description` AS `description`, `personal_achievements`.`category` AS `category`, `personal_achievements`.`iconName` AS `iconName`, `personal_achievements`.`targetValue` AS `targetValue`, `personal_achievements`.`currentValue` AS `currentValue`, `personal_achievements`.`unit` AS `unit`, `personal_achievements`.`isCompleted` AS `isCompleted`, `personal_achievements`.`completedAt` AS `completedAt`, `personal_achievements`.`createdAt` AS `createdAt`, `personal_achievements`.`badgeType` AS `badgeType`, `personal_achievements`.`rarity` AS `rarity`, `personal_achievements`.`socialVisible` AS `socialVisible`, `personal_achievements`.`challengeId` AS `challengeId`, `personal_achievements`.`shareMessage` AS `shareMessage`, `personal_achievements`.`pointsValue` AS `pointsValue` FROM personal_achievements ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("personal_achievements")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfDescription: Int = 2
        val _columnIndexOfCategory: Int = 3
        val _columnIndexOfIconName: Int = 4
        val _columnIndexOfTargetValue: Int = 5
        val _columnIndexOfCurrentValue: Int = 6
        val _columnIndexOfUnit: Int = 7
        val _columnIndexOfIsCompleted: Int = 8
        val _columnIndexOfCompletedAt: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _columnIndexOfBadgeType: Int = 11
        val _columnIndexOfRarity: Int = 12
        val _columnIndexOfSocialVisible: Int = 13
        val _columnIndexOfChallengeId: Int = 14
        val _columnIndexOfShareMessage: Int = 15
        val _columnIndexOfPointsValue: Int = 16
        val _result: MutableList<PersonalAchievementEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalAchievementEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpTargetValue: Double?
          if (_stmt.isNull(_columnIndexOfTargetValue)) {
            _tmpTargetValue = null
          } else {
            _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          }
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String?
          if (_stmt.isNull(_columnIndexOfUnit)) {
            _tmpUnit = null
          } else {
            _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpBadgeType: String?
          if (_stmt.isNull(_columnIndexOfBadgeType)) {
            _tmpBadgeType = null
          } else {
            _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          }
          val _tmpRarity: String?
          if (_stmt.isNull(_columnIndexOfRarity)) {
            _tmpRarity = null
          } else {
            _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          }
          val _tmpSocialVisible: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfSocialVisible).toInt()
          _tmpSocialVisible = _tmp_1 != 0
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpShareMessage: String?
          if (_stmt.isNull(_columnIndexOfShareMessage)) {
            _tmpShareMessage = null
          } else {
            _tmpShareMessage = _stmt.getText(_columnIndexOfShareMessage)
          }
          val _tmpPointsValue: Int
          _tmpPointsValue = _stmt.getLong(_columnIndexOfPointsValue).toInt()
          _item =
              PersonalAchievementEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpIconName,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpIsCompleted,_tmpCompletedAt,_tmpCreatedAt,_tmpBadgeType,_tmpRarity,_tmpSocialVisible,_tmpChallengeId,_tmpShareMessage,_tmpPointsValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun achievementsByCategoryFlow(category: String):
      Flow<List<PersonalAchievementEntity>> {
    val _sql: String =
        "SELECT * FROM personal_achievements WHERE category = ? ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("personal_achievements")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIconName: Int = getColumnIndexOrThrow(_stmt, "iconName")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfBadgeType: Int = getColumnIndexOrThrow(_stmt, "badgeType")
        val _columnIndexOfRarity: Int = getColumnIndexOrThrow(_stmt, "rarity")
        val _columnIndexOfSocialVisible: Int = getColumnIndexOrThrow(_stmt, "socialVisible")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfShareMessage: Int = getColumnIndexOrThrow(_stmt, "shareMessage")
        val _columnIndexOfPointsValue: Int = getColumnIndexOrThrow(_stmt, "pointsValue")
        val _result: MutableList<PersonalAchievementEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalAchievementEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpTargetValue: Double?
          if (_stmt.isNull(_columnIndexOfTargetValue)) {
            _tmpTargetValue = null
          } else {
            _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          }
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String?
          if (_stmt.isNull(_columnIndexOfUnit)) {
            _tmpUnit = null
          } else {
            _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpBadgeType: String?
          if (_stmt.isNull(_columnIndexOfBadgeType)) {
            _tmpBadgeType = null
          } else {
            _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          }
          val _tmpRarity: String?
          if (_stmt.isNull(_columnIndexOfRarity)) {
            _tmpRarity = null
          } else {
            _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          }
          val _tmpSocialVisible: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfSocialVisible).toInt()
          _tmpSocialVisible = _tmp_1 != 0
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpShareMessage: String?
          if (_stmt.isNull(_columnIndexOfShareMessage)) {
            _tmpShareMessage = null
          } else {
            _tmpShareMessage = _stmt.getText(_columnIndexOfShareMessage)
          }
          val _tmpPointsValue: Int
          _tmpPointsValue = _stmt.getLong(_columnIndexOfPointsValue).toInt()
          _item =
              PersonalAchievementEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpIconName,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpIsCompleted,_tmpCompletedAt,_tmpCreatedAt,_tmpBadgeType,_tmpRarity,_tmpSocialVisible,_tmpChallengeId,_tmpShareMessage,_tmpPointsValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun achievementsByCompletionFlow(completed: Boolean):
      Flow<List<PersonalAchievementEntity>> {
    val _sql: String =
        "SELECT * FROM personal_achievements WHERE isCompleted = ? ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("personal_achievements")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (completed) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIconName: Int = getColumnIndexOrThrow(_stmt, "iconName")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfBadgeType: Int = getColumnIndexOrThrow(_stmt, "badgeType")
        val _columnIndexOfRarity: Int = getColumnIndexOrThrow(_stmt, "rarity")
        val _columnIndexOfSocialVisible: Int = getColumnIndexOrThrow(_stmt, "socialVisible")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfShareMessage: Int = getColumnIndexOrThrow(_stmt, "shareMessage")
        val _columnIndexOfPointsValue: Int = getColumnIndexOrThrow(_stmt, "pointsValue")
        val _result: MutableList<PersonalAchievementEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PersonalAchievementEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpTargetValue: Double?
          if (_stmt.isNull(_columnIndexOfTargetValue)) {
            _tmpTargetValue = null
          } else {
            _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          }
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String?
          if (_stmt.isNull(_columnIndexOfUnit)) {
            _tmpUnit = null
          } else {
            _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          }
          val _tmpIsCompleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_1 != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpBadgeType: String?
          if (_stmt.isNull(_columnIndexOfBadgeType)) {
            _tmpBadgeType = null
          } else {
            _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          }
          val _tmpRarity: String?
          if (_stmt.isNull(_columnIndexOfRarity)) {
            _tmpRarity = null
          } else {
            _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          }
          val _tmpSocialVisible: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfSocialVisible).toInt()
          _tmpSocialVisible = _tmp_2 != 0
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpShareMessage: String?
          if (_stmt.isNull(_columnIndexOfShareMessage)) {
            _tmpShareMessage = null
          } else {
            _tmpShareMessage = _stmt.getText(_columnIndexOfShareMessage)
          }
          val _tmpPointsValue: Int
          _tmpPointsValue = _stmt.getLong(_columnIndexOfPointsValue).toInt()
          _item =
              PersonalAchievementEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpIconName,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpIsCompleted,_tmpCompletedAt,_tmpCreatedAt,_tmpBadgeType,_tmpRarity,_tmpSocialVisible,_tmpChallengeId,_tmpShareMessage,_tmpPointsValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAchievement(id: Long): PersonalAchievementEntity? {
    val _sql: String = "SELECT * FROM personal_achievements WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIconName: Int = getColumnIndexOrThrow(_stmt, "iconName")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfBadgeType: Int = getColumnIndexOrThrow(_stmt, "badgeType")
        val _columnIndexOfRarity: Int = getColumnIndexOrThrow(_stmt, "rarity")
        val _columnIndexOfSocialVisible: Int = getColumnIndexOrThrow(_stmt, "socialVisible")
        val _columnIndexOfChallengeId: Int = getColumnIndexOrThrow(_stmt, "challengeId")
        val _columnIndexOfShareMessage: Int = getColumnIndexOrThrow(_stmt, "shareMessage")
        val _columnIndexOfPointsValue: Int = getColumnIndexOrThrow(_stmt, "pointsValue")
        val _result: PersonalAchievementEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIconName: String
          _tmpIconName = _stmt.getText(_columnIndexOfIconName)
          val _tmpTargetValue: Double?
          if (_stmt.isNull(_columnIndexOfTargetValue)) {
            _tmpTargetValue = null
          } else {
            _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          }
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String?
          if (_stmt.isNull(_columnIndexOfUnit)) {
            _tmpUnit = null
          } else {
            _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpBadgeType: String?
          if (_stmt.isNull(_columnIndexOfBadgeType)) {
            _tmpBadgeType = null
          } else {
            _tmpBadgeType = _stmt.getText(_columnIndexOfBadgeType)
          }
          val _tmpRarity: String?
          if (_stmt.isNull(_columnIndexOfRarity)) {
            _tmpRarity = null
          } else {
            _tmpRarity = _stmt.getText(_columnIndexOfRarity)
          }
          val _tmpSocialVisible: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfSocialVisible).toInt()
          _tmpSocialVisible = _tmp_1 != 0
          val _tmpChallengeId: Long?
          if (_stmt.isNull(_columnIndexOfChallengeId)) {
            _tmpChallengeId = null
          } else {
            _tmpChallengeId = _stmt.getLong(_columnIndexOfChallengeId)
          }
          val _tmpShareMessage: String?
          if (_stmt.isNull(_columnIndexOfShareMessage)) {
            _tmpShareMessage = null
          } else {
            _tmpShareMessage = _stmt.getText(_columnIndexOfShareMessage)
          }
          val _tmpPointsValue: Int
          _tmpPointsValue = _stmt.getLong(_columnIndexOfPointsValue).toInt()
          _result =
              PersonalAchievementEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpCategory,_tmpIconName,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpIsCompleted,_tmpCompletedAt,_tmpCreatedAt,_tmpBadgeType,_tmpRarity,_tmpSocialVisible,_tmpChallengeId,_tmpShareMessage,_tmpPointsValue)
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
    val _sql: String = "DELETE FROM personal_achievements WHERE id = ?"
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

  public override suspend fun markAsCompleted(
    id: Long,
    completed: Boolean,
    completedAt: Long?,
  ) {
    val _sql: String =
        "UPDATE personal_achievements SET isCompleted = ?, completedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (completed) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
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

  public override suspend fun updateProgress(id: Long, `value`: Double) {
    val _sql: String = "UPDATE personal_achievements SET currentValue = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, value)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetAllAchievements() {
    val _sql: String =
        "UPDATE personal_achievements SET isCompleted = 0, completedAt = NULL, currentValue = 0.0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM personal_achievements"
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
