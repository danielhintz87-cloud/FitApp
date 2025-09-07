package com.example.fitapp.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
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
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserProfileDao_Impl(
  __db: RoomDatabase,
) : UserProfileDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUserProfileEntity: EntityInsertAdapter<UserProfileEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfUserProfileEntity = object : EntityInsertAdapter<UserProfileEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `user_profiles` (`id`,`userId`,`email`,`displayName`,`deviceName`,`deviceId`,`lastSyncTime`,`syncPreferences`,`encryptionKey`,`isActive`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserProfileEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.userId)
        statement.bindText(3, entity.email)
        val _tmpDisplayName: String? = entity.displayName
        if (_tmpDisplayName == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpDisplayName)
        }
        statement.bindText(5, entity.deviceName)
        statement.bindText(6, entity.deviceId)
        statement.bindLong(7, entity.lastSyncTime)
        statement.bindText(8, entity.syncPreferences)
        val _tmpEncryptionKey: String? = entity.encryptionKey
        if (_tmpEncryptionKey == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpEncryptionKey)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        statement.bindLong(11, entity.createdAt)
      }
    }
  }

  public override suspend fun upsertUserProfile(profile: UserProfileEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUserProfileEntity.insert(_connection, profile)
  }

  public override suspend fun getUserProfile(userId: String): UserProfileEntity? {
    val _sql: String = "SELECT * FROM user_profiles WHERE userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfDisplayName: Int = getColumnIndexOrThrow(_stmt, "displayName")
        val _columnIndexOfDeviceName: Int = getColumnIndexOrThrow(_stmt, "deviceName")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfLastSyncTime: Int = getColumnIndexOrThrow(_stmt, "lastSyncTime")
        val _columnIndexOfSyncPreferences: Int = getColumnIndexOrThrow(_stmt, "syncPreferences")
        val _columnIndexOfEncryptionKey: Int = getColumnIndexOrThrow(_stmt, "encryptionKey")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: UserProfileEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpDisplayName: String?
          if (_stmt.isNull(_columnIndexOfDisplayName)) {
            _tmpDisplayName = null
          } else {
            _tmpDisplayName = _stmt.getText(_columnIndexOfDisplayName)
          }
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpLastSyncTime: Long
          _tmpLastSyncTime = _stmt.getLong(_columnIndexOfLastSyncTime)
          val _tmpSyncPreferences: String
          _tmpSyncPreferences = _stmt.getText(_columnIndexOfSyncPreferences)
          val _tmpEncryptionKey: String?
          if (_stmt.isNull(_columnIndexOfEncryptionKey)) {
            _tmpEncryptionKey = null
          } else {
            _tmpEncryptionKey = _stmt.getText(_columnIndexOfEncryptionKey)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              UserProfileEntity(_tmpId,_tmpUserId,_tmpEmail,_tmpDisplayName,_tmpDeviceName,_tmpDeviceId,_tmpLastSyncTime,_tmpSyncPreferences,_tmpEncryptionKey,_tmpIsActive,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveUserProfile(): UserProfileEntity? {
    val _sql: String =
        "SELECT `user_profiles`.`id` AS `id`, `user_profiles`.`userId` AS `userId`, `user_profiles`.`email` AS `email`, `user_profiles`.`displayName` AS `displayName`, `user_profiles`.`deviceName` AS `deviceName`, `user_profiles`.`deviceId` AS `deviceId`, `user_profiles`.`lastSyncTime` AS `lastSyncTime`, `user_profiles`.`syncPreferences` AS `syncPreferences`, `user_profiles`.`encryptionKey` AS `encryptionKey`, `user_profiles`.`isActive` AS `isActive`, `user_profiles`.`createdAt` AS `createdAt` FROM user_profiles WHERE isActive = 1 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfUserId: Int = 1
        val _columnIndexOfEmail: Int = 2
        val _columnIndexOfDisplayName: Int = 3
        val _columnIndexOfDeviceName: Int = 4
        val _columnIndexOfDeviceId: Int = 5
        val _columnIndexOfLastSyncTime: Int = 6
        val _columnIndexOfSyncPreferences: Int = 7
        val _columnIndexOfEncryptionKey: Int = 8
        val _columnIndexOfIsActive: Int = 9
        val _columnIndexOfCreatedAt: Int = 10
        val _result: UserProfileEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpDisplayName: String?
          if (_stmt.isNull(_columnIndexOfDisplayName)) {
            _tmpDisplayName = null
          } else {
            _tmpDisplayName = _stmt.getText(_columnIndexOfDisplayName)
          }
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpLastSyncTime: Long
          _tmpLastSyncTime = _stmt.getLong(_columnIndexOfLastSyncTime)
          val _tmpSyncPreferences: String
          _tmpSyncPreferences = _stmt.getText(_columnIndexOfSyncPreferences)
          val _tmpEncryptionKey: String?
          if (_stmt.isNull(_columnIndexOfEncryptionKey)) {
            _tmpEncryptionKey = null
          } else {
            _tmpEncryptionKey = _stmt.getText(_columnIndexOfEncryptionKey)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              UserProfileEntity(_tmpId,_tmpUserId,_tmpEmail,_tmpDisplayName,_tmpDeviceName,_tmpDeviceId,_tmpLastSyncTime,_tmpSyncPreferences,_tmpEncryptionKey,_tmpIsActive,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDeviceId(deviceId: String): UserProfileEntity? {
    val _sql: String = "SELECT * FROM user_profiles WHERE deviceId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, deviceId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfDisplayName: Int = getColumnIndexOrThrow(_stmt, "displayName")
        val _columnIndexOfDeviceName: Int = getColumnIndexOrThrow(_stmt, "deviceName")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfLastSyncTime: Int = getColumnIndexOrThrow(_stmt, "lastSyncTime")
        val _columnIndexOfSyncPreferences: Int = getColumnIndexOrThrow(_stmt, "syncPreferences")
        val _columnIndexOfEncryptionKey: Int = getColumnIndexOrThrow(_stmt, "encryptionKey")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: UserProfileEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpDisplayName: String?
          if (_stmt.isNull(_columnIndexOfDisplayName)) {
            _tmpDisplayName = null
          } else {
            _tmpDisplayName = _stmt.getText(_columnIndexOfDisplayName)
          }
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpLastSyncTime: Long
          _tmpLastSyncTime = _stmt.getLong(_columnIndexOfLastSyncTime)
          val _tmpSyncPreferences: String
          _tmpSyncPreferences = _stmt.getText(_columnIndexOfSyncPreferences)
          val _tmpEncryptionKey: String?
          if (_stmt.isNull(_columnIndexOfEncryptionKey)) {
            _tmpEncryptionKey = null
          } else {
            _tmpEncryptionKey = _stmt.getText(_columnIndexOfEncryptionKey)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              UserProfileEntity(_tmpId,_tmpUserId,_tmpEmail,_tmpDisplayName,_tmpDeviceName,_tmpDeviceId,_tmpLastSyncTime,_tmpSyncPreferences,_tmpEncryptionKey,_tmpIsActive,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateLastSyncTime(userId: String, timestamp: Long) {
    val _sql: String = "UPDATE user_profiles SET lastSyncTime = ? WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, timestamp)
        _argIndex = 2
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateSyncPreferences(userId: String, preferences: String) {
    val _sql: String = "UPDATE user_profiles SET syncPreferences = ? WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, preferences)
        _argIndex = 2
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deactivateAllProfiles() {
    val _sql: String = "UPDATE user_profiles SET isActive = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun activateProfile(userId: String) {
    val _sql: String = "UPDATE user_profiles SET isActive = 1 WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteUserProfile(userId: String) {
    val _sql: String = "DELETE FROM user_profiles WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
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
