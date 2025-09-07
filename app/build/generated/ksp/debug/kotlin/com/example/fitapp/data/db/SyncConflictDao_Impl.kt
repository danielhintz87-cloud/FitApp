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
public class SyncConflictDao_Impl(
  __db: RoomDatabase,
) : SyncConflictDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSyncConflictEntity: EntityInsertAdapter<SyncConflictEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSyncConflictEntity = object : EntityInsertAdapter<SyncConflictEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `sync_conflicts` (`id`,`entityType`,`entityId`,`localData`,`remoteData`,`localTimestamp`,`remoteTimestamp`,`status`,`resolution`,`resolvedData`,`resolvedBy`,`createdAt`,`resolvedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SyncConflictEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.entityType)
        statement.bindText(3, entity.entityId)
        statement.bindText(4, entity.localData)
        statement.bindText(5, entity.remoteData)
        statement.bindLong(6, entity.localTimestamp)
        statement.bindLong(7, entity.remoteTimestamp)
        statement.bindText(8, entity.status)
        val _tmpResolution: String? = entity.resolution
        if (_tmpResolution == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpResolution)
        }
        val _tmpResolvedData: String? = entity.resolvedData
        if (_tmpResolvedData == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpResolvedData)
        }
        val _tmpResolvedBy: String? = entity.resolvedBy
        if (_tmpResolvedBy == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpResolvedBy)
        }
        statement.bindLong(12, entity.createdAt)
        val _tmpResolvedAt: Long? = entity.resolvedAt
        if (_tmpResolvedAt == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpResolvedAt)
        }
      }
    }
  }

  public override suspend fun insertConflict(conflict: SyncConflictEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSyncConflictEntity.insert(_connection, conflict)
  }

  public override suspend fun getPendingConflicts(): List<SyncConflictEntity> {
    val _sql: String =
        "SELECT `sync_conflicts`.`id` AS `id`, `sync_conflicts`.`entityType` AS `entityType`, `sync_conflicts`.`entityId` AS `entityId`, `sync_conflicts`.`localData` AS `localData`, `sync_conflicts`.`remoteData` AS `remoteData`, `sync_conflicts`.`localTimestamp` AS `localTimestamp`, `sync_conflicts`.`remoteTimestamp` AS `remoteTimestamp`, `sync_conflicts`.`status` AS `status`, `sync_conflicts`.`resolution` AS `resolution`, `sync_conflicts`.`resolvedData` AS `resolvedData`, `sync_conflicts`.`resolvedBy` AS `resolvedBy`, `sync_conflicts`.`createdAt` AS `createdAt`, `sync_conflicts`.`resolvedAt` AS `resolvedAt` FROM sync_conflicts WHERE status = 'pending'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfEntityType: Int = 1
        val _columnIndexOfEntityId: Int = 2
        val _columnIndexOfLocalData: Int = 3
        val _columnIndexOfRemoteData: Int = 4
        val _columnIndexOfLocalTimestamp: Int = 5
        val _columnIndexOfRemoteTimestamp: Int = 6
        val _columnIndexOfStatus: Int = 7
        val _columnIndexOfResolution: Int = 8
        val _columnIndexOfResolvedData: Int = 9
        val _columnIndexOfResolvedBy: Int = 10
        val _columnIndexOfCreatedAt: Int = 11
        val _columnIndexOfResolvedAt: Int = 12
        val _result: MutableList<SyncConflictEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SyncConflictEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpLocalData: String
          _tmpLocalData = _stmt.getText(_columnIndexOfLocalData)
          val _tmpRemoteData: String
          _tmpRemoteData = _stmt.getText(_columnIndexOfRemoteData)
          val _tmpLocalTimestamp: Long
          _tmpLocalTimestamp = _stmt.getLong(_columnIndexOfLocalTimestamp)
          val _tmpRemoteTimestamp: Long
          _tmpRemoteTimestamp = _stmt.getLong(_columnIndexOfRemoteTimestamp)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpResolution: String?
          if (_stmt.isNull(_columnIndexOfResolution)) {
            _tmpResolution = null
          } else {
            _tmpResolution = _stmt.getText(_columnIndexOfResolution)
          }
          val _tmpResolvedData: String?
          if (_stmt.isNull(_columnIndexOfResolvedData)) {
            _tmpResolvedData = null
          } else {
            _tmpResolvedData = _stmt.getText(_columnIndexOfResolvedData)
          }
          val _tmpResolvedBy: String?
          if (_stmt.isNull(_columnIndexOfResolvedBy)) {
            _tmpResolvedBy = null
          } else {
            _tmpResolvedBy = _stmt.getText(_columnIndexOfResolvedBy)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpResolvedAt: Long?
          if (_stmt.isNull(_columnIndexOfResolvedAt)) {
            _tmpResolvedAt = null
          } else {
            _tmpResolvedAt = _stmt.getLong(_columnIndexOfResolvedAt)
          }
          _item =
              SyncConflictEntity(_tmpId,_tmpEntityType,_tmpEntityId,_tmpLocalData,_tmpRemoteData,_tmpLocalTimestamp,_tmpRemoteTimestamp,_tmpStatus,_tmpResolution,_tmpResolvedData,_tmpResolvedBy,_tmpCreatedAt,_tmpResolvedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getConflictForEntity(entityType: String, entityId: String):
      SyncConflictEntity? {
    val _sql: String =
        "SELECT * FROM sync_conflicts WHERE entityType = ? AND entityId = ? AND status = 'pending'"
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
        val _columnIndexOfLocalData: Int = getColumnIndexOrThrow(_stmt, "localData")
        val _columnIndexOfRemoteData: Int = getColumnIndexOrThrow(_stmt, "remoteData")
        val _columnIndexOfLocalTimestamp: Int = getColumnIndexOrThrow(_stmt, "localTimestamp")
        val _columnIndexOfRemoteTimestamp: Int = getColumnIndexOrThrow(_stmt, "remoteTimestamp")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfResolution: Int = getColumnIndexOrThrow(_stmt, "resolution")
        val _columnIndexOfResolvedData: Int = getColumnIndexOrThrow(_stmt, "resolvedData")
        val _columnIndexOfResolvedBy: Int = getColumnIndexOrThrow(_stmt, "resolvedBy")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfResolvedAt: Int = getColumnIndexOrThrow(_stmt, "resolvedAt")
        val _result: SyncConflictEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpLocalData: String
          _tmpLocalData = _stmt.getText(_columnIndexOfLocalData)
          val _tmpRemoteData: String
          _tmpRemoteData = _stmt.getText(_columnIndexOfRemoteData)
          val _tmpLocalTimestamp: Long
          _tmpLocalTimestamp = _stmt.getLong(_columnIndexOfLocalTimestamp)
          val _tmpRemoteTimestamp: Long
          _tmpRemoteTimestamp = _stmt.getLong(_columnIndexOfRemoteTimestamp)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpResolution: String?
          if (_stmt.isNull(_columnIndexOfResolution)) {
            _tmpResolution = null
          } else {
            _tmpResolution = _stmt.getText(_columnIndexOfResolution)
          }
          val _tmpResolvedData: String?
          if (_stmt.isNull(_columnIndexOfResolvedData)) {
            _tmpResolvedData = null
          } else {
            _tmpResolvedData = _stmt.getText(_columnIndexOfResolvedData)
          }
          val _tmpResolvedBy: String?
          if (_stmt.isNull(_columnIndexOfResolvedBy)) {
            _tmpResolvedBy = null
          } else {
            _tmpResolvedBy = _stmt.getText(_columnIndexOfResolvedBy)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpResolvedAt: Long?
          if (_stmt.isNull(_columnIndexOfResolvedAt)) {
            _tmpResolvedAt = null
          } else {
            _tmpResolvedAt = _stmt.getLong(_columnIndexOfResolvedAt)
          }
          _result =
              SyncConflictEntity(_tmpId,_tmpEntityType,_tmpEntityId,_tmpLocalData,_tmpRemoteData,_tmpLocalTimestamp,_tmpRemoteTimestamp,_tmpStatus,_tmpResolution,_tmpResolvedData,_tmpResolvedBy,_tmpCreatedAt,_tmpResolvedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPendingConflictCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM sync_conflicts WHERE status = 'pending'"
    return createFlow(__db, false, arrayOf("sync_conflicts")) { _connection ->
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

  public override suspend fun resolveConflict(
    id: String,
    status: String,
    resolution: String,
    resolvedData: String?,
    resolvedBy: String,
    resolvedAt: Long,
  ) {
    val _sql: String =
        "UPDATE sync_conflicts SET status = ?, resolution = ?, resolvedData = ?, resolvedBy = ?, resolvedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindText(_argIndex, resolution)
        _argIndex = 3
        if (resolvedData == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, resolvedData)
        }
        _argIndex = 4
        _stmt.bindText(_argIndex, resolvedBy)
        _argIndex = 5
        _stmt.bindLong(_argIndex, resolvedAt)
        _argIndex = 6
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteConflict(id: String) {
    val _sql: String = "DELETE FROM sync_conflicts WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun cleanupResolvedConflicts(cutoffTime: Long) {
    val _sql: String = "DELETE FROM sync_conflicts WHERE createdAt < ? AND status != 'pending'"
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
