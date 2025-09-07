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
public class MealEntryDao_Impl(
  __db: RoomDatabase,
) : MealEntryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMealEntryEntity: EntityInsertAdapter<MealEntryEntity>

  private val __updateAdapterOfMealEntryEntity: EntityDeleteOrUpdateAdapter<MealEntryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMealEntryEntity = object : EntityInsertAdapter<MealEntryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `meal_entries` (`id`,`foodItemId`,`date`,`mealType`,`quantityGrams`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MealEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.foodItemId)
        statement.bindText(3, entity.date)
        statement.bindText(4, entity.mealType)
        statement.bindDouble(5, entity.quantityGrams.toDouble())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNotes)
        }
      }
    }
    this.__updateAdapterOfMealEntryEntity = object : EntityDeleteOrUpdateAdapter<MealEntryEntity>()
        {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `meal_entries` SET `id` = ?,`foodItemId` = ?,`date` = ?,`mealType` = ?,`quantityGrams` = ?,`notes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MealEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.foodItemId)
        statement.bindText(3, entity.date)
        statement.bindText(4, entity.mealType)
        statement.bindDouble(5, entity.quantityGrams.toDouble())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNotes)
        }
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun insert(mealEntry: MealEntryEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfMealEntryEntity.insertAndReturnId(_connection, mealEntry)
    _result
  }

  public override suspend fun update(mealEntry: MealEntryEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfMealEntryEntity.handle(_connection, mealEntry)
  }

  public override suspend fun getByDate(date: String): List<MealEntryEntity> {
    val _sql: String = "SELECT * FROM meal_entries WHERE date = ? ORDER BY id"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFoodItemId: Int = getColumnIndexOrThrow(_stmt, "foodItemId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfQuantityGrams: Int = getColumnIndexOrThrow(_stmt, "quantityGrams")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<MealEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MealEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFoodItemId: String
          _tmpFoodItemId = _stmt.getText(_columnIndexOfFoodItemId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpQuantityGrams: Float
          _tmpQuantityGrams = _stmt.getDouble(_columnIndexOfQuantityGrams).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              MealEntryEntity(_tmpId,_tmpFoodItemId,_tmpDate,_tmpMealType,_tmpQuantityGrams,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getByDateFlow(date: String): Flow<List<MealEntryEntity>> {
    val _sql: String = "SELECT * FROM meal_entries WHERE date = ? ORDER BY id"
    return createFlow(__db, false, arrayOf("meal_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFoodItemId: Int = getColumnIndexOrThrow(_stmt, "foodItemId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfQuantityGrams: Int = getColumnIndexOrThrow(_stmt, "quantityGrams")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<MealEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MealEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFoodItemId: String
          _tmpFoodItemId = _stmt.getText(_columnIndexOfFoodItemId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpQuantityGrams: Float
          _tmpQuantityGrams = _stmt.getDouble(_columnIndexOfQuantityGrams).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              MealEntryEntity(_tmpId,_tmpFoodItemId,_tmpDate,_tmpMealType,_tmpQuantityGrams,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateAndMealType(date: String, mealType: String):
      List<MealEntryEntity> {
    val _sql: String = "SELECT * FROM meal_entries WHERE date = ? AND mealType = ? ORDER BY id"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindText(_argIndex, mealType)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFoodItemId: Int = getColumnIndexOrThrow(_stmt, "foodItemId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfQuantityGrams: Int = getColumnIndexOrThrow(_stmt, "quantityGrams")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<MealEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MealEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFoodItemId: String
          _tmpFoodItemId = _stmt.getText(_columnIndexOfFoodItemId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpQuantityGrams: Float
          _tmpQuantityGrams = _stmt.getDouble(_columnIndexOfQuantityGrams).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              MealEntryEntity(_tmpId,_tmpFoodItemId,_tmpDate,_tmpMealType,_tmpQuantityGrams,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getByDateAndMealTypeFlow(date: String, mealType: String):
      Flow<List<MealEntryEntity>> {
    val _sql: String = "SELECT * FROM meal_entries WHERE date = ? AND mealType = ? ORDER BY id"
    return createFlow(__db, false, arrayOf("meal_entries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindText(_argIndex, mealType)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFoodItemId: Int = getColumnIndexOrThrow(_stmt, "foodItemId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMealType: Int = getColumnIndexOrThrow(_stmt, "mealType")
        val _columnIndexOfQuantityGrams: Int = getColumnIndexOrThrow(_stmt, "quantityGrams")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<MealEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MealEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFoodItemId: String
          _tmpFoodItemId = _stmt.getText(_columnIndexOfFoodItemId)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMealType: String
          _tmpMealType = _stmt.getText(_columnIndexOfMealType)
          val _tmpQuantityGrams: Float
          _tmpQuantityGrams = _stmt.getDouble(_columnIndexOfQuantityGrams).toFloat()
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item =
              MealEntryEntity(_tmpId,_tmpFoodItemId,_tmpDate,_tmpMealType,_tmpQuantityGrams,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalCaloriesForDate(date: String): Float? {
    val _sql: String = """
        |
        |        SELECT SUM(
        |            CASE mealType
        |                WHEN 'breakfast' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
        |                WHEN 'lunch' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
        |                WHEN 'dinner' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
        |                WHEN 'snack' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
        |                ELSE 0
        |            END
        |        ) FROM meal_entries WHERE date = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalCarbsForDate(date: String): Float? {
    val _sql: String = """
        |
        |        SELECT SUM((quantityGrams / 100.0) * (SELECT carbs FROM food_items WHERE id = foodItemId))
        |        FROM meal_entries WHERE date = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalProteinForDate(date: String): Float? {
    val _sql: String = """
        |
        |        SELECT SUM((quantityGrams / 100.0) * (SELECT protein FROM food_items WHERE id = foodItemId))
        |        FROM meal_entries WHERE date = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalFatForDate(date: String): Float? {
    val _sql: String = """
        |
        |        SELECT SUM((quantityGrams / 100.0) * (SELECT fat FROM food_items WHERE id = foodItemId))
        |        FROM meal_entries WHERE date = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
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
    val _sql: String = "DELETE FROM meal_entries WHERE id = ?"
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

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM meal_entries"
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
