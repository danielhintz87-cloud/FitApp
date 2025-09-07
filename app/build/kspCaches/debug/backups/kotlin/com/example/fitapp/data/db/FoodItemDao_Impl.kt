package com.example.fitapp.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Float
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
public class FoodItemDao_Impl(
  __db: RoomDatabase,
) : FoodItemDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFoodItemEntity: EntityInsertAdapter<FoodItemEntity>

  private val __updateAdapterOfFoodItemEntity: EntityDeleteOrUpdateAdapter<FoodItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfFoodItemEntity = object : EntityInsertAdapter<FoodItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `food_items` (`id`,`name`,`barcode`,`calories`,`carbs`,`protein`,`fat`,`createdAt`,`fiber`,`sugar`,`sodium`,`brands`,`categories`,`imageUrl`,`servingSize`,`ingredients`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FoodItemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpBarcode: String? = entity.barcode
        if (_tmpBarcode == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpBarcode)
        }
        statement.bindLong(4, entity.calories.toLong())
        statement.bindDouble(5, entity.carbs.toDouble())
        statement.bindDouble(6, entity.protein.toDouble())
        statement.bindDouble(7, entity.fat.toDouble())
        statement.bindLong(8, entity.createdAt)
        val _tmpFiber: Float? = entity.fiber
        if (_tmpFiber == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpFiber.toDouble())
        }
        val _tmpSugar: Float? = entity.sugar
        if (_tmpSugar == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpSugar.toDouble())
        }
        val _tmpSodium: Float? = entity.sodium
        if (_tmpSodium == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpSodium.toDouble())
        }
        val _tmpBrands: String? = entity.brands
        if (_tmpBrands == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpBrands)
        }
        val _tmpCategories: String? = entity.categories
        if (_tmpCategories == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpCategories)
        }
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpImageUrl)
        }
        val _tmpServingSize: String? = entity.servingSize
        if (_tmpServingSize == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpServingSize)
        }
        val _tmpIngredients: String? = entity.ingredients
        if (_tmpIngredients == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpIngredients)
        }
      }
    }
    this.__updateAdapterOfFoodItemEntity = object : EntityDeleteOrUpdateAdapter<FoodItemEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `food_items` SET `id` = ?,`name` = ?,`barcode` = ?,`calories` = ?,`carbs` = ?,`protein` = ?,`fat` = ?,`createdAt` = ?,`fiber` = ?,`sugar` = ?,`sodium` = ?,`brands` = ?,`categories` = ?,`imageUrl` = ?,`servingSize` = ?,`ingredients` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FoodItemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpBarcode: String? = entity.barcode
        if (_tmpBarcode == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpBarcode)
        }
        statement.bindLong(4, entity.calories.toLong())
        statement.bindDouble(5, entity.carbs.toDouble())
        statement.bindDouble(6, entity.protein.toDouble())
        statement.bindDouble(7, entity.fat.toDouble())
        statement.bindLong(8, entity.createdAt)
        val _tmpFiber: Float? = entity.fiber
        if (_tmpFiber == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpFiber.toDouble())
        }
        val _tmpSugar: Float? = entity.sugar
        if (_tmpSugar == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpSugar.toDouble())
        }
        val _tmpSodium: Float? = entity.sodium
        if (_tmpSodium == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpSodium.toDouble())
        }
        val _tmpBrands: String? = entity.brands
        if (_tmpBrands == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpBrands)
        }
        val _tmpCategories: String? = entity.categories
        if (_tmpCategories == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpCategories)
        }
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpImageUrl)
        }
        val _tmpServingSize: String? = entity.servingSize
        if (_tmpServingSize == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpServingSize)
        }
        val _tmpIngredients: String? = entity.ingredients
        if (_tmpIngredients == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpIngredients)
        }
        statement.bindText(17, entity.id)
      }
    }
  }

  public override suspend fun insert(foodItem: FoodItemEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfFoodItemEntity.insert(_connection, foodItem)
  }

  public override suspend fun update(foodItem: FoodItemEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfFoodItemEntity.handle(_connection, foodItem)
  }

  public override suspend fun getById(id: String): FoodItemEntity? {
    val _sql: String = "SELECT * FROM food_items WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfCarbs: Int = getColumnIndexOrThrow(_stmt, "carbs")
        val _columnIndexOfProtein: Int = getColumnIndexOrThrow(_stmt, "protein")
        val _columnIndexOfFat: Int = getColumnIndexOrThrow(_stmt, "fat")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfFiber: Int = getColumnIndexOrThrow(_stmt, "fiber")
        val _columnIndexOfSugar: Int = getColumnIndexOrThrow(_stmt, "sugar")
        val _columnIndexOfSodium: Int = getColumnIndexOrThrow(_stmt, "sodium")
        val _columnIndexOfBrands: Int = getColumnIndexOrThrow(_stmt, "brands")
        val _columnIndexOfCategories: Int = getColumnIndexOrThrow(_stmt, "categories")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfServingSize: Int = getColumnIndexOrThrow(_stmt, "servingSize")
        val _columnIndexOfIngredients: Int = getColumnIndexOrThrow(_stmt, "ingredients")
        val _result: FoodItemEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCalories: Int
          _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          val _tmpCarbs: Float
          _tmpCarbs = _stmt.getDouble(_columnIndexOfCarbs).toFloat()
          val _tmpProtein: Float
          _tmpProtein = _stmt.getDouble(_columnIndexOfProtein).toFloat()
          val _tmpFat: Float
          _tmpFat = _stmt.getDouble(_columnIndexOfFat).toFloat()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpFiber: Float?
          if (_stmt.isNull(_columnIndexOfFiber)) {
            _tmpFiber = null
          } else {
            _tmpFiber = _stmt.getDouble(_columnIndexOfFiber).toFloat()
          }
          val _tmpSugar: Float?
          if (_stmt.isNull(_columnIndexOfSugar)) {
            _tmpSugar = null
          } else {
            _tmpSugar = _stmt.getDouble(_columnIndexOfSugar).toFloat()
          }
          val _tmpSodium: Float?
          if (_stmt.isNull(_columnIndexOfSodium)) {
            _tmpSodium = null
          } else {
            _tmpSodium = _stmt.getDouble(_columnIndexOfSodium).toFloat()
          }
          val _tmpBrands: String?
          if (_stmt.isNull(_columnIndexOfBrands)) {
            _tmpBrands = null
          } else {
            _tmpBrands = _stmt.getText(_columnIndexOfBrands)
          }
          val _tmpCategories: String?
          if (_stmt.isNull(_columnIndexOfCategories)) {
            _tmpCategories = null
          } else {
            _tmpCategories = _stmt.getText(_columnIndexOfCategories)
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpServingSize: String?
          if (_stmt.isNull(_columnIndexOfServingSize)) {
            _tmpServingSize = null
          } else {
            _tmpServingSize = _stmt.getText(_columnIndexOfServingSize)
          }
          val _tmpIngredients: String?
          if (_stmt.isNull(_columnIndexOfIngredients)) {
            _tmpIngredients = null
          } else {
            _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          }
          _result =
              FoodItemEntity(_tmpId,_tmpName,_tmpBarcode,_tmpCalories,_tmpCarbs,_tmpProtein,_tmpFat,_tmpCreatedAt,_tmpFiber,_tmpSugar,_tmpSodium,_tmpBrands,_tmpCategories,_tmpImageUrl,_tmpServingSize,_tmpIngredients)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByBarcode(barcode: String): FoodItemEntity? {
    val _sql: String = "SELECT * FROM food_items WHERE barcode = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, barcode)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfCarbs: Int = getColumnIndexOrThrow(_stmt, "carbs")
        val _columnIndexOfProtein: Int = getColumnIndexOrThrow(_stmt, "protein")
        val _columnIndexOfFat: Int = getColumnIndexOrThrow(_stmt, "fat")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfFiber: Int = getColumnIndexOrThrow(_stmt, "fiber")
        val _columnIndexOfSugar: Int = getColumnIndexOrThrow(_stmt, "sugar")
        val _columnIndexOfSodium: Int = getColumnIndexOrThrow(_stmt, "sodium")
        val _columnIndexOfBrands: Int = getColumnIndexOrThrow(_stmt, "brands")
        val _columnIndexOfCategories: Int = getColumnIndexOrThrow(_stmt, "categories")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfServingSize: Int = getColumnIndexOrThrow(_stmt, "servingSize")
        val _columnIndexOfIngredients: Int = getColumnIndexOrThrow(_stmt, "ingredients")
        val _result: FoodItemEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCalories: Int
          _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          val _tmpCarbs: Float
          _tmpCarbs = _stmt.getDouble(_columnIndexOfCarbs).toFloat()
          val _tmpProtein: Float
          _tmpProtein = _stmt.getDouble(_columnIndexOfProtein).toFloat()
          val _tmpFat: Float
          _tmpFat = _stmt.getDouble(_columnIndexOfFat).toFloat()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpFiber: Float?
          if (_stmt.isNull(_columnIndexOfFiber)) {
            _tmpFiber = null
          } else {
            _tmpFiber = _stmt.getDouble(_columnIndexOfFiber).toFloat()
          }
          val _tmpSugar: Float?
          if (_stmt.isNull(_columnIndexOfSugar)) {
            _tmpSugar = null
          } else {
            _tmpSugar = _stmt.getDouble(_columnIndexOfSugar).toFloat()
          }
          val _tmpSodium: Float?
          if (_stmt.isNull(_columnIndexOfSodium)) {
            _tmpSodium = null
          } else {
            _tmpSodium = _stmt.getDouble(_columnIndexOfSodium).toFloat()
          }
          val _tmpBrands: String?
          if (_stmt.isNull(_columnIndexOfBrands)) {
            _tmpBrands = null
          } else {
            _tmpBrands = _stmt.getText(_columnIndexOfBrands)
          }
          val _tmpCategories: String?
          if (_stmt.isNull(_columnIndexOfCategories)) {
            _tmpCategories = null
          } else {
            _tmpCategories = _stmt.getText(_columnIndexOfCategories)
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpServingSize: String?
          if (_stmt.isNull(_columnIndexOfServingSize)) {
            _tmpServingSize = null
          } else {
            _tmpServingSize = _stmt.getText(_columnIndexOfServingSize)
          }
          val _tmpIngredients: String?
          if (_stmt.isNull(_columnIndexOfIngredients)) {
            _tmpIngredients = null
          } else {
            _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          }
          _result =
              FoodItemEntity(_tmpId,_tmpName,_tmpBarcode,_tmpCalories,_tmpCarbs,_tmpProtein,_tmpFat,_tmpCreatedAt,_tmpFiber,_tmpSugar,_tmpSodium,_tmpBrands,_tmpCategories,_tmpImageUrl,_tmpServingSize,_tmpIngredients)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun searchByName(query: String, limit: Int): List<FoodItemEntity> {
    val _sql: String =
        "SELECT * FROM food_items WHERE name LIKE '%' || ? || '%' ORDER BY name LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfCarbs: Int = getColumnIndexOrThrow(_stmt, "carbs")
        val _columnIndexOfProtein: Int = getColumnIndexOrThrow(_stmt, "protein")
        val _columnIndexOfFat: Int = getColumnIndexOrThrow(_stmt, "fat")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfFiber: Int = getColumnIndexOrThrow(_stmt, "fiber")
        val _columnIndexOfSugar: Int = getColumnIndexOrThrow(_stmt, "sugar")
        val _columnIndexOfSodium: Int = getColumnIndexOrThrow(_stmt, "sodium")
        val _columnIndexOfBrands: Int = getColumnIndexOrThrow(_stmt, "brands")
        val _columnIndexOfCategories: Int = getColumnIndexOrThrow(_stmt, "categories")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfServingSize: Int = getColumnIndexOrThrow(_stmt, "servingSize")
        val _columnIndexOfIngredients: Int = getColumnIndexOrThrow(_stmt, "ingredients")
        val _result: MutableList<FoodItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCalories: Int
          _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          val _tmpCarbs: Float
          _tmpCarbs = _stmt.getDouble(_columnIndexOfCarbs).toFloat()
          val _tmpProtein: Float
          _tmpProtein = _stmt.getDouble(_columnIndexOfProtein).toFloat()
          val _tmpFat: Float
          _tmpFat = _stmt.getDouble(_columnIndexOfFat).toFloat()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpFiber: Float?
          if (_stmt.isNull(_columnIndexOfFiber)) {
            _tmpFiber = null
          } else {
            _tmpFiber = _stmt.getDouble(_columnIndexOfFiber).toFloat()
          }
          val _tmpSugar: Float?
          if (_stmt.isNull(_columnIndexOfSugar)) {
            _tmpSugar = null
          } else {
            _tmpSugar = _stmt.getDouble(_columnIndexOfSugar).toFloat()
          }
          val _tmpSodium: Float?
          if (_stmt.isNull(_columnIndexOfSodium)) {
            _tmpSodium = null
          } else {
            _tmpSodium = _stmt.getDouble(_columnIndexOfSodium).toFloat()
          }
          val _tmpBrands: String?
          if (_stmt.isNull(_columnIndexOfBrands)) {
            _tmpBrands = null
          } else {
            _tmpBrands = _stmt.getText(_columnIndexOfBrands)
          }
          val _tmpCategories: String?
          if (_stmt.isNull(_columnIndexOfCategories)) {
            _tmpCategories = null
          } else {
            _tmpCategories = _stmt.getText(_columnIndexOfCategories)
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpServingSize: String?
          if (_stmt.isNull(_columnIndexOfServingSize)) {
            _tmpServingSize = null
          } else {
            _tmpServingSize = _stmt.getText(_columnIndexOfServingSize)
          }
          val _tmpIngredients: String?
          if (_stmt.isNull(_columnIndexOfIngredients)) {
            _tmpIngredients = null
          } else {
            _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          }
          _item =
              FoodItemEntity(_tmpId,_tmpName,_tmpBarcode,_tmpCalories,_tmpCarbs,_tmpProtein,_tmpFat,_tmpCreatedAt,_tmpFiber,_tmpSugar,_tmpSodium,_tmpBrands,_tmpCategories,_tmpImageUrl,_tmpServingSize,_tmpIngredients)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecent(limit: Int): List<FoodItemEntity> {
    val _sql: String = "SELECT * FROM food_items ORDER BY createdAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBarcode: Int = getColumnIndexOrThrow(_stmt, "barcode")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfCarbs: Int = getColumnIndexOrThrow(_stmt, "carbs")
        val _columnIndexOfProtein: Int = getColumnIndexOrThrow(_stmt, "protein")
        val _columnIndexOfFat: Int = getColumnIndexOrThrow(_stmt, "fat")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfFiber: Int = getColumnIndexOrThrow(_stmt, "fiber")
        val _columnIndexOfSugar: Int = getColumnIndexOrThrow(_stmt, "sugar")
        val _columnIndexOfSodium: Int = getColumnIndexOrThrow(_stmt, "sodium")
        val _columnIndexOfBrands: Int = getColumnIndexOrThrow(_stmt, "brands")
        val _columnIndexOfCategories: Int = getColumnIndexOrThrow(_stmt, "categories")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfServingSize: Int = getColumnIndexOrThrow(_stmt, "servingSize")
        val _columnIndexOfIngredients: Int = getColumnIndexOrThrow(_stmt, "ingredients")
        val _result: MutableList<FoodItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCalories: Int
          _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          val _tmpCarbs: Float
          _tmpCarbs = _stmt.getDouble(_columnIndexOfCarbs).toFloat()
          val _tmpProtein: Float
          _tmpProtein = _stmt.getDouble(_columnIndexOfProtein).toFloat()
          val _tmpFat: Float
          _tmpFat = _stmt.getDouble(_columnIndexOfFat).toFloat()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpFiber: Float?
          if (_stmt.isNull(_columnIndexOfFiber)) {
            _tmpFiber = null
          } else {
            _tmpFiber = _stmt.getDouble(_columnIndexOfFiber).toFloat()
          }
          val _tmpSugar: Float?
          if (_stmt.isNull(_columnIndexOfSugar)) {
            _tmpSugar = null
          } else {
            _tmpSugar = _stmt.getDouble(_columnIndexOfSugar).toFloat()
          }
          val _tmpSodium: Float?
          if (_stmt.isNull(_columnIndexOfSodium)) {
            _tmpSodium = null
          } else {
            _tmpSodium = _stmt.getDouble(_columnIndexOfSodium).toFloat()
          }
          val _tmpBrands: String?
          if (_stmt.isNull(_columnIndexOfBrands)) {
            _tmpBrands = null
          } else {
            _tmpBrands = _stmt.getText(_columnIndexOfBrands)
          }
          val _tmpCategories: String?
          if (_stmt.isNull(_columnIndexOfCategories)) {
            _tmpCategories = null
          } else {
            _tmpCategories = _stmt.getText(_columnIndexOfCategories)
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpServingSize: String?
          if (_stmt.isNull(_columnIndexOfServingSize)) {
            _tmpServingSize = null
          } else {
            _tmpServingSize = _stmt.getText(_columnIndexOfServingSize)
          }
          val _tmpIngredients: String?
          if (_stmt.isNull(_columnIndexOfIngredients)) {
            _tmpIngredients = null
          } else {
            _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          }
          _item =
              FoodItemEntity(_tmpId,_tmpName,_tmpBarcode,_tmpCalories,_tmpCarbs,_tmpProtein,_tmpFat,_tmpCreatedAt,_tmpFiber,_tmpSugar,_tmpSodium,_tmpBrands,_tmpCategories,_tmpImageUrl,_tmpServingSize,_tmpIngredients)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun allFoodItemsFlow(): Flow<List<FoodItemEntity>> {
    val _sql: String =
        "SELECT `food_items`.`id` AS `id`, `food_items`.`name` AS `name`, `food_items`.`barcode` AS `barcode`, `food_items`.`calories` AS `calories`, `food_items`.`carbs` AS `carbs`, `food_items`.`protein` AS `protein`, `food_items`.`fat` AS `fat`, `food_items`.`createdAt` AS `createdAt`, `food_items`.`fiber` AS `fiber`, `food_items`.`sugar` AS `sugar`, `food_items`.`sodium` AS `sodium`, `food_items`.`brands` AS `brands`, `food_items`.`categories` AS `categories`, `food_items`.`imageUrl` AS `imageUrl`, `food_items`.`servingSize` AS `servingSize`, `food_items`.`ingredients` AS `ingredients` FROM food_items ORDER BY name"
    return createFlow(__db, false, arrayOf("food_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfName: Int = 1
        val _columnIndexOfBarcode: Int = 2
        val _columnIndexOfCalories: Int = 3
        val _columnIndexOfCarbs: Int = 4
        val _columnIndexOfProtein: Int = 5
        val _columnIndexOfFat: Int = 6
        val _columnIndexOfCreatedAt: Int = 7
        val _columnIndexOfFiber: Int = 8
        val _columnIndexOfSugar: Int = 9
        val _columnIndexOfSodium: Int = 10
        val _columnIndexOfBrands: Int = 11
        val _columnIndexOfCategories: Int = 12
        val _columnIndexOfImageUrl: Int = 13
        val _columnIndexOfServingSize: Int = 14
        val _columnIndexOfIngredients: Int = 15
        val _result: MutableList<FoodItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBarcode: String?
          if (_stmt.isNull(_columnIndexOfBarcode)) {
            _tmpBarcode = null
          } else {
            _tmpBarcode = _stmt.getText(_columnIndexOfBarcode)
          }
          val _tmpCalories: Int
          _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          val _tmpCarbs: Float
          _tmpCarbs = _stmt.getDouble(_columnIndexOfCarbs).toFloat()
          val _tmpProtein: Float
          _tmpProtein = _stmt.getDouble(_columnIndexOfProtein).toFloat()
          val _tmpFat: Float
          _tmpFat = _stmt.getDouble(_columnIndexOfFat).toFloat()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpFiber: Float?
          if (_stmt.isNull(_columnIndexOfFiber)) {
            _tmpFiber = null
          } else {
            _tmpFiber = _stmt.getDouble(_columnIndexOfFiber).toFloat()
          }
          val _tmpSugar: Float?
          if (_stmt.isNull(_columnIndexOfSugar)) {
            _tmpSugar = null
          } else {
            _tmpSugar = _stmt.getDouble(_columnIndexOfSugar).toFloat()
          }
          val _tmpSodium: Float?
          if (_stmt.isNull(_columnIndexOfSodium)) {
            _tmpSodium = null
          } else {
            _tmpSodium = _stmt.getDouble(_columnIndexOfSodium).toFloat()
          }
          val _tmpBrands: String?
          if (_stmt.isNull(_columnIndexOfBrands)) {
            _tmpBrands = null
          } else {
            _tmpBrands = _stmt.getText(_columnIndexOfBrands)
          }
          val _tmpCategories: String?
          if (_stmt.isNull(_columnIndexOfCategories)) {
            _tmpCategories = null
          } else {
            _tmpCategories = _stmt.getText(_columnIndexOfCategories)
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpServingSize: String?
          if (_stmt.isNull(_columnIndexOfServingSize)) {
            _tmpServingSize = null
          } else {
            _tmpServingSize = _stmt.getText(_columnIndexOfServingSize)
          }
          val _tmpIngredients: String?
          if (_stmt.isNull(_columnIndexOfIngredients)) {
            _tmpIngredients = null
          } else {
            _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          }
          _item =
              FoodItemEntity(_tmpId,_tmpName,_tmpBarcode,_tmpCalories,_tmpCarbs,_tmpProtein,_tmpFat,_tmpCreatedAt,_tmpFiber,_tmpSugar,_tmpSodium,_tmpBrands,_tmpCategories,_tmpImageUrl,_tmpServingSize,_tmpIngredients)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM food_items WHERE id = ?"
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

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM food_items"
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
