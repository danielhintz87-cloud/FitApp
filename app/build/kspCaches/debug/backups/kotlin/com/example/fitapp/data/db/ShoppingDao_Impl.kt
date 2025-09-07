package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
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
public class ShoppingDao_Impl(
  __db: RoomDatabase,
) : ShoppingDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfShoppingItemEntity: EntityInsertAdapter<ShoppingItemEntity>

  private val __updateAdapterOfShoppingItemEntity: EntityDeleteOrUpdateAdapter<ShoppingItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfShoppingItemEntity = object : EntityInsertAdapter<ShoppingItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `shopping_items` (`id`,`name`,`quantity`,`unit`,`checked`,`category`,`fromRecipeId`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ShoppingItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpQuantity: String? = entity.quantity
        if (_tmpQuantity == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpQuantity)
        }
        val _tmpUnit: String? = entity.unit
        if (_tmpUnit == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpUnit)
        }
        val _tmp: Int = if (entity.checked) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        val _tmpCategory: String? = entity.category
        if (_tmpCategory == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpCategory)
        }
        val _tmpFromRecipeId: String? = entity.fromRecipeId
        if (_tmpFromRecipeId == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpFromRecipeId)
        }
        statement.bindLong(8, entity.createdAt)
      }
    }
    this.__updateAdapterOfShoppingItemEntity = object :
        EntityDeleteOrUpdateAdapter<ShoppingItemEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `shopping_items` SET `id` = ?,`name` = ?,`quantity` = ?,`unit` = ?,`checked` = ?,`category` = ?,`fromRecipeId` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ShoppingItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpQuantity: String? = entity.quantity
        if (_tmpQuantity == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpQuantity)
        }
        val _tmpUnit: String? = entity.unit
        if (_tmpUnit == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpUnit)
        }
        val _tmp: Int = if (entity.checked) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        val _tmpCategory: String? = entity.category
        if (_tmpCategory == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpCategory)
        }
        val _tmpFromRecipeId: String? = entity.fromRecipeId
        if (_tmpFromRecipeId == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpFromRecipeId)
        }
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insert(item: ShoppingItemEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfShoppingItemEntity.insert(_connection, item)
  }

  public override suspend fun update(item: ShoppingItemEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfShoppingItemEntity.handle(_connection, item)
  }

  public override fun itemsFlow(): Flow<List<ShoppingItemEntity>> {
    val _sql: String =
        "SELECT `shopping_items`.`id` AS `id`, `shopping_items`.`name` AS `name`, `shopping_items`.`quantity` AS `quantity`, `shopping_items`.`unit` AS `unit`, `shopping_items`.`checked` AS `checked`, `shopping_items`.`category` AS `category`, `shopping_items`.`fromRecipeId` AS `fromRecipeId`, `shopping_items`.`createdAt` AS `createdAt` FROM shopping_items ORDER BY category, name"
    return createFlow(__db, false, arrayOf("shopping_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfQuantity: Int = 2
        val _columnIndexOfUnit: Int = 3
        val _columnIndexOfChecked: Int = 4
        val _columnIndexOfCategory: Int = 5
        val _columnIndexOfFromRecipeId: Int = 6
        val _columnIndexOfCreatedAt: Int = 7
        val _result: MutableList<ShoppingItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ShoppingItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpQuantity: String?
          if (_stmt.isNull(_columnIndexOfQuantity)) {
            _tmpQuantity = null
          } else {
            _tmpQuantity = _stmt.getText(_columnIndexOfQuantity)
          }
          val _tmpUnit: String?
          if (_stmt.isNull(_columnIndexOfUnit)) {
            _tmpUnit = null
          } else {
            _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          }
          val _tmpChecked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfChecked).toInt()
          _tmpChecked = _tmp != 0
          val _tmpCategory: String?
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          }
          val _tmpFromRecipeId: String?
          if (_stmt.isNull(_columnIndexOfFromRecipeId)) {
            _tmpFromRecipeId = null
          } else {
            _tmpFromRecipeId = _stmt.getText(_columnIndexOfFromRecipeId)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ShoppingItemEntity(_tmpId,_tmpName,_tmpQuantity,_tmpUnit,_tmpChecked,_tmpCategory,_tmpFromRecipeId,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun itemsFlowByDate(): Flow<List<ShoppingItemEntity>> {
    val _sql: String =
        "SELECT `shopping_items`.`id` AS `id`, `shopping_items`.`name` AS `name`, `shopping_items`.`quantity` AS `quantity`, `shopping_items`.`unit` AS `unit`, `shopping_items`.`checked` AS `checked`, `shopping_items`.`category` AS `category`, `shopping_items`.`fromRecipeId` AS `fromRecipeId`, `shopping_items`.`createdAt` AS `createdAt` FROM shopping_items ORDER BY checked, createdAt DESC"
    return createFlow(__db, false, arrayOf("shopping_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfQuantity: Int = 2
        val _columnIndexOfUnit: Int = 3
        val _columnIndexOfChecked: Int = 4
        val _columnIndexOfCategory: Int = 5
        val _columnIndexOfFromRecipeId: Int = 6
        val _columnIndexOfCreatedAt: Int = 7
        val _result: MutableList<ShoppingItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ShoppingItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpQuantity: String?
          if (_stmt.isNull(_columnIndexOfQuantity)) {
            _tmpQuantity = null
          } else {
            _tmpQuantity = _stmt.getText(_columnIndexOfQuantity)
          }
          val _tmpUnit: String?
          if (_stmt.isNull(_columnIndexOfUnit)) {
            _tmpUnit = null
          } else {
            _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          }
          val _tmpChecked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfChecked).toInt()
          _tmpChecked = _tmp != 0
          val _tmpCategory: String?
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          }
          val _tmpFromRecipeId: String?
          if (_stmt.isNull(_columnIndexOfFromRecipeId)) {
            _tmpFromRecipeId = null
          } else {
            _tmpFromRecipeId = _stmt.getText(_columnIndexOfFromRecipeId)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ShoppingItemEntity(_tmpId,_tmpName,_tmpQuantity,_tmpUnit,_tmpChecked,_tmpCategory,_tmpFromRecipeId,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM shopping_items WHERE id = ?"
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

  public override suspend fun setChecked(id: Long, checked: Boolean) {
    val _sql: String = "UPDATE shopping_items SET checked = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (checked) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCheckedItems() {
    val _sql: String = "DELETE FROM shopping_items WHERE checked = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateQuantityAndUnit(
    id: Long,
    quantity: Double,
    unit: String,
  ) {
    val _sql: String = "UPDATE shopping_items SET quantity = ?, unit = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, quantity)
        _argIndex = 2
        _stmt.bindText(_argIndex, unit)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM shopping_items"
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
