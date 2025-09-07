package com.example.fitapp.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CloudSyncDao_Impl(
  __db: RoomDatabase,
) : CloudSyncDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCloudSyncEntity: EntityInsertAdapter<CloudSyncEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCloudSyncEntity = object : EntityInsertAdapter<CloudSyncEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `cloud_sync_metadata` (`id`,`entityType`,`entityId`,`lastSyncTime`,`lastModifiedTime`,`syncStatus`,`deviceId`,`cloudVersion`,`conflictData`,`retryCount`,`errorMessage`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CloudSyncEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.entityType)
        statement.bindText(3, entity.entityId)
        statement.bindLong(4, entity.lastSyncTime)
        statement.bindLong(5, entity.lastModifiedTime)
        statement.bindText(6, entity.syncStatus)
        statement.bindText(7, entity.deviceId)
        val _tmpCloudVersion: String? = entity.cloudVersion
        if (_tmpCloudVersion == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpCloudVersion)
        }
        val _tmpConflictData: String? = entity.conflictData
        if (_tmpConflictData == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpConflictData)
        }
        statement.bindLong(10, entity.retryCount.toLong())
        val _tmpErrorMessage: String? = entity.errorMessage
        if (_tmpErrorMessage == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpErrorMessage)
        }
        statement.bindLong(12, entity.createdAt)
      }
    }
  }

  public override suspend fun upsertSyncMetadata(metadata: CloudSyncEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCloudSyncEntity.insert(_connection, metadata)
  }

  public override suspend fun getSyncMetadata(entityType: String, entityId: String):
      CloudSyncEntity? {
    val _sql: String = "SELECT * FROM cloud_sync_metadata WHERE entityType = ? AND entityId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, entityType)
        _argIndex = 2
        _stmt.bindText(_argIndex, entityId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfLastSyncTime: Int = getColumnIndexOrThrow(_stmt, "lastSyncTime")
        val _columnIndexOfLastModifiedTime: Int = getColumnIndexOrThrow(_stmt, "lastModifiedTime")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfCloudVersion: Int = getColumnIndexOrThrow(_stmt, "cloudVersion")
        val _columnIndexOfConflictData: Int = getColumnIndexOrThrow(_stmt, "conflictData")
        val _columnIndexOfRetryCount: Int = getColumnIndexOrThrow(_stmt, "retryCount")
        val _columnIndexOfErrorMessage: Int = getColumnIndexOrThrow(_stmt, "errorMessage")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: CloudSyncEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpLastSyncTime: Long
          _tmpLastSyncTime = _stmt.getLong(_columnIndexOfLastSyncTime)
          val _tmpLastModifiedTime: Long
          _tmpLastModifiedTime = _stmt.getLong(_columnIndexOfLastModifiedTime)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpCloudVersion: String?
          if (_stmt.isNull(_columnIndexOfCloudVersion)) {
            _tmpCloudVersion = null
          } else {
            _tmpCloudVersion = _stmt.getText(_columnIndexOfCloudVersion)
          }
          val _tmpConflictData: String?
          if (_stmt.isNull(_columnIndexOfConflictData)) {
            _tmpConflictData = null
          } else {
            _tmpConflictData = _stmt.getText(_columnIndexOfConflictData)
          }
          val _tmpRetryCount: Int
          _tmpRetryCount = _stmt.getLong(_columnIndexOfRetryCount).toInt()
          val _tmpErrorMessage: String?
          if (_stmt.isNull(_columnIndexOfErrorMessage)) {
            _tmpErrorMessage = null
          } else {
            _tmpErrorMessage = _stmt.getText(_columnIndexOfErrorMessage)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              CloudSyncEntity(_tmpId,_tmpEntityType,_tmpEntityId,_tmpLastSyncTime,_tmpLastModifiedTime,_tmpSyncStatus,_tmpDeviceId,_tmpCloudVersion,_tmpConflictData,_tmpRetryCount,_tmpErrorMessage,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByStatus(status: String): List<CloudSyncEntity> {
    val _sql: String = "SELECT * FROM cloud_sync_metadata WHERE syncStatus = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfLastSyncTime: Int = getColumnIndexOrThrow(_stmt, "lastSyncTime")
        val _columnIndexOfLastModifiedTime: Int = getColumnIndexOrThrow(_stmt, "lastModifiedTime")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfCloudVersion: Int = getColumnIndexOrThrow(_stmt, "cloudVersion")
        val _columnIndexOfConflictData: Int = getColumnIndexOrThrow(_stmt, "conflictData")
        val _columnIndexOfRetryCount: Int = getColumnIndexOrThrow(_stmt, "retryCount")
        val _columnIndexOfErrorMessage: Int = getColumnIndexOrThrow(_stmt, "errorMessage")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CloudSyncEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CloudSyncEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpLastSyncTime: Long
          _tmpLastSyncTime = _stmt.getLong(_columnIndexOfLastSyncTime)
          val _tmpLastModifiedTime: Long
          _tmpLastModifiedTime = _stmt.getLong(_columnIndexOfLastModifiedTime)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpCloudVersion: String?
          if (_stmt.isNull(_columnIndexOfCloudVersion)) {
            _tmpCloudVersion = null
          } else {
            _tmpCloudVersion = _stmt.getText(_columnIndexOfCloudVersion)
          }
          val _tmpConflictData: String?
          if (_stmt.isNull(_columnIndexOfConflictData)) {
            _tmpConflictData = null
          } else {
            _tmpConflictData = _stmt.getText(_columnIndexOfConflictData)
          }
          val _tmpRetryCount: Int
          _tmpRetryCount = _stmt.getLong(_columnIndexOfRetryCount).toInt()
          val _tmpErrorMessage: String?
          if (_stmt.isNull(_columnIndexOfErrorMessage)) {
            _tmpErrorMessage = null
          } else {
            _tmpErrorMessage = _stmt.getText(_columnIndexOfErrorMessage)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CloudSyncEntity(_tmpId,_tmpEntityType,_tmpEntityId,_tmpLastSyncTime,_tmpLastModifiedTime,_tmpSyncStatus,_tmpDeviceId,_tmpCloudVersion,_tmpConflictData,_tmpRetryCount,_tmpErrorMessage,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingSync(): List<CloudSyncEntity> {
    val _sql: String =
        "SELECT `cloud_sync_metadata`.`id` AS `id`, `cloud_sync_metadata`.`entityType` AS `entityType`, `cloud_sync_metadata`.`entityId` AS `entityId`, `cloud_sync_metadata`.`lastSyncTime` AS `lastSyncTime`, `cloud_sync_metadata`.`lastModifiedTime` AS `lastModifiedTime`, `cloud_sync_metadata`.`syncStatus` AS `syncStatus`, `cloud_sync_metadata`.`deviceId` AS `deviceId`, `cloud_sync_metadata`.`cloudVersion` AS `cloudVersion`, `cloud_sync_metadata`.`conflictData` AS `conflictData`, `cloud_sync_metadata`.`retryCount` AS `retryCount`, `cloud_sync_metadata`.`errorMessage` AS `errorMessage`, `cloud_sync_metadata`.`createdAt` AS `createdAt` FROM cloud_sync_metadata WHERE syncStatus = 'pending' OR syncStatus = 'error'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfEntityType: Int = 1
        val _columnIndexOfEntityId: Int = 2
        val _columnIndexOfLastSyncTime: Int = 3
        val _columnIndexOfLastModifiedTime: Int = 4
        val _columnIndexOfSyncStatus: Int = 5
        val _columnIndexOfDeviceId: Int = 6
        val _columnIndexOfCloudVersion: Int = 7
        val _columnIndexOfConflictData: Int = 8
        val _columnIndexOfRetryCount: Int = 9
        val _columnIndexOfErrorMessage: Int = 10
        val _columnIndexOfCreatedAt: Int = 11
        val _result: MutableList<CloudSyncEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CloudSyncEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpLastSyncTime: Long
          _tmpLastSyncTime = _stmt.getLong(_columnIndexOfLastSyncTime)
          val _tmpLastModifiedTime: Long
          _tmpLastModifiedTime = _stmt.getLong(_columnIndexOfLastModifiedTime)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpCloudVersion: String?
          if (_stmt.isNull(_columnIndexOfCloudVersion)) {
            _tmpCloudVersion = null
          } else {
            _tmpCloudVersion = _stmt.getText(_columnIndexOfCloudVersion)
          }
          val _tmpConflictData: String?
          if (_stmt.isNull(_columnIndexOfConflictData)) {
            _tmpConflictData = null
          } else {
            _tmpConflictData = _stmt.getText(_columnIndexOfConflictData)
          }
          val _tmpRetryCount: Int
          _tmpRetryCount = _stmt.getLong(_columnIndexOfRetryCount).toInt()
          val _tmpErrorMessage: String?
          if (_stmt.isNull(_columnIndexOfErrorMessage)) {
            _tmpErrorMessage = null
          } else {
            _tmpErrorMessage = _stmt.getText(_columnIndexOfErrorMessage)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CloudSyncEntity(_tmpId,_tmpEntityType,_tmpEntityId,_tmpLastSyncTime,_tmpLastModifiedTime,_tmpSyncStatus,_tmpDeviceId,_tmpCloudVersion,_tmpConflictData,_tmpRetryCount,_tmpErrorMessage,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDeviceId(deviceId: String): List<CloudSyncEntity> {
    val _sql: String = "SELECT * FROM cloud_sync_metadata WHERE deviceId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, deviceId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfLastSyncTime: Int = getColumnIndexOrThrow(_stmt, "lastSyncTime")
        val _columnIndexOfLastModifiedTime: Int = getColumnIndexOrThrow(_stmt, "lastModifiedTime")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfCloudVersion: Int = getColumnIndexOrThrow(_stmt, "cloudVersion")
        val _columnIndexOfConflictData: Int = getColumnIndexOrThrow(_stmt, "conflictData")
        val _columnIndexOfRetryCount: Int = getColumnIndexOrThrow(_stmt, "retryCount")
        val _columnIndexOfErrorMessage: Int = getColumnIndexOrThrow(_stmt, "errorMessage")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CloudSyncEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CloudSyncEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpLastSyncTime: Long
          _tmpLastSyncTime = _stmt.getLong(_columnIndexOfLastSyncTime)
          val _tmpLastModifiedTime: Long
          _tmpLastModifiedTime = _stmt.getLong(_columnIndexOfLastModifiedTime)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpCloudVersion: String?
          if (_stmt.isNull(_columnIndexOfCloudVersion)) {
            _tmpCloudVersion = null
          } else {
            _tmpCloudVersion = _stmt.getText(_columnIndexOfCloudVersion)
          }
          val _tmpConflictData: String?
          if (_stmt.isNull(_columnIndexOfConflictData)) {
            _tmpConflictData = null
          } else {
            _tmpConflictData = _stmt.getText(_columnIndexOfConflictData)
          }
          val _tmpRetryCount: Int
          _tmpRetryCount = _stmt.getLong(_columnIndexOfRetryCount).toInt()
          val _tmpErrorMessage: String?
          if (_stmt.isNull(_columnIndexOfErrorMessage)) {
            _tmpErrorMessage = null
          } else {
            _tmpErrorMessage = _stmt.getText(_columnIndexOfErrorMessage)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CloudSyncEntity(_tmpId,_tmpEntityType,_tmpEntityId,_tmpLastSyncTime,_tmpLastModifiedTime,_tmpSyncStatus,_tmpDeviceId,_tmpCloudVersion,_tmpConflictData,_tmpRetryCount,_tmpErrorMessage,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateSyncStatus(
    id: String,
    status: String,
    timestamp: Long,
  ) {
    val _sql: String =
        "UPDATE cloud_sync_metadata SET syncStatus = ?, lastSyncTime = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindLong(_argIndex, timestamp)
        _argIndex = 3
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementRetryCount(id: String, error: String?) {
    val _sql: String =
        "UPDATE cloud_sync_metadata SET retryCount = retryCount + 1, errorMessage = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (error == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, error)
        }
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSyncMetadata(entityType: String, entityId: String) {
    val _sql: String = "DELETE FROM cloud_sync_metadata WHERE entityType = ? AND entityId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, entityType)
        _argIndex = 2
        _stmt.bindText(_argIndex, entityId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun cleanupOldMetadata(cutoffTime: Long) {
    val _sql: String = "DELETE FROM cloud_sync_metadata WHERE lastSyncTime < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, cutoffTime)
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
