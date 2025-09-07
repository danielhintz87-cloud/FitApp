package com.example.fitapp.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
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
public class ShoppingCategoryDao_Impl(
  __db: RoomDatabase,
) : ShoppingCategoryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfShoppingCategoryEntity: EntityInsertAdapter<ShoppingCategoryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfShoppingCategoryEntity = object :
        EntityInsertAdapter<ShoppingCategoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `shopping_list_categories` (`name`,`order`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ShoppingCategoryEntity) {
        statement.bindText(1, entity.name)
        statement.bindLong(2, entity.order.toLong())
      }
    }
  }

  public override suspend fun insert(category: ShoppingCategoryEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfShoppingCategoryEntity.insert(_connection, category)
  }

  public override fun categoriesFlow(): Flow<List<ShoppingCategoryEntity>> {
    val _sql: String =
        "SELECT `shopping_list_categories`.`name` AS `name`, `shopping_list_categories`.`order` AS `order` FROM shopping_list_categories ORDER BY \"order\""
    return createFlow(__db, false, arrayOf("shopping_list_categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfName: Int = 0
        val _columnIndexOfOrder: Int = 1
        val _result: MutableList<ShoppingCategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ShoppingCategoryEntity
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          _item = ShoppingCategoryEntity(_tmpName,_tmpOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCategories(): List<ShoppingCategoryEntity> {
    val _sql: String =
        "SELECT `shopping_list_categories`.`name` AS `name`, `shopping_list_categories`.`order` AS `order` FROM shopping_list_categories ORDER BY \"order\""
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfName: Int = 0
        val _columnIndexOfOrder: Int = 1
        val _result: MutableList<ShoppingCategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ShoppingCategoryEntity
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          _item = ShoppingCategoryEntity(_tmpName,_tmpOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM shopping_list_categories"
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
