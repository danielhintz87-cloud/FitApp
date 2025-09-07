package com.example.fitapp.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
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
public class RecipeDao_Impl(
  __db: RoomDatabase,
) : RecipeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRecipeEntity: EntityInsertAdapter<RecipeEntity>

  private val __insertAdapterOfRecipeFavoriteEntity: EntityInsertAdapter<RecipeFavoriteEntity>

  private val __insertAdapterOfRecipeHistoryEntity: EntityInsertAdapter<RecipeHistoryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfRecipeEntity = object : EntityInsertAdapter<RecipeEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `recipes` (`id`,`title`,`markdown`,`calories`,`imageUrl`,`createdAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RecipeEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.markdown)
        val _tmpCalories: Int? = entity.calories
        if (_tmpCalories == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpCalories.toLong())
        }
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpImageUrl)
        }
        statement.bindLong(6, entity.createdAt)
      }
    }
    this.__insertAdapterOfRecipeFavoriteEntity = object :
        EntityInsertAdapter<RecipeFavoriteEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `recipe_favorites` (`recipeId`,`savedAt`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RecipeFavoriteEntity) {
        statement.bindText(1, entity.recipeId)
        statement.bindLong(2, entity.savedAt)
      }
    }
    this.__insertAdapterOfRecipeHistoryEntity = object : EntityInsertAdapter<RecipeHistoryEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `recipe_history` (`id`,`recipeId`,`createdAt`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RecipeHistoryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.recipeId)
        statement.bindLong(3, entity.createdAt)
      }
    }
  }

  public override suspend fun upsertRecipe(recipe: RecipeEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfRecipeEntity.insert(_connection, recipe)
  }

  public override suspend fun addFavorite(fav: RecipeFavoriteEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfRecipeFavoriteEntity.insert(_connection, fav)
  }

  public override suspend fun insertHistory(history: RecipeHistoryEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRecipeHistoryEntity.insert(_connection, history)
  }

  public override suspend fun upsertAndAddToHistory(recipe: RecipeEntity): Unit =
      performInTransactionSuspending(__db) {
    super@RecipeDao_Impl.upsertAndAddToHistory(recipe)
  }

  public override fun favoritesFlow(): Flow<List<RecipeEntity>> {
    val _sql: String = """
        |
        |        SELECT `r`.`id`, `r`.`title`, `r`.`markdown`, `r`.`calories`, `r`.`imageUrl`, `r`.`createdAt` FROM recipes r
        |        INNER JOIN recipe_favorites f ON r.id = f.recipeId
        |        ORDER BY f.savedAt DESC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("recipes", "recipe_favorites")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfMarkdown: Int = 2
        val _columnIndexOfCalories: Int = 3
        val _columnIndexOfImageUrl: Int = 4
        val _columnIndexOfCreatedAt: Int = 5
        val _result: MutableList<RecipeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecipeEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpMarkdown: String
          _tmpMarkdown = _stmt.getText(_columnIndexOfMarkdown)
          val _tmpCalories: Int?
          if (_stmt.isNull(_columnIndexOfCalories)) {
            _tmpCalories = null
          } else {
            _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              RecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun historyFlow(): Flow<List<RecipeEntity>> {
    val _sql: String = """
        |
        |        SELECT `r`.`id`, `r`.`title`, `r`.`markdown`, `r`.`calories`, `r`.`imageUrl`, `r`.`createdAt` FROM recipes r
        |        INNER JOIN recipe_history h ON r.id = h.recipeId
        |        ORDER BY h.createdAt DESC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("recipes", "recipe_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfMarkdown: Int = 2
        val _columnIndexOfCalories: Int = 3
        val _columnIndexOfImageUrl: Int = 4
        val _columnIndexOfCreatedAt: Int = 5
        val _result: MutableList<RecipeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecipeEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpMarkdown: String
          _tmpMarkdown = _stmt.getText(_columnIndexOfMarkdown)
          val _tmpCalories: Int?
          if (_stmt.isNull(_columnIndexOfCalories)) {
            _tmpCalories = null
          } else {
            _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              RecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecipe(id: String): RecipeEntity? {
    val _sql: String = "SELECT * FROM recipes WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfMarkdown: Int = getColumnIndexOrThrow(_stmt, "markdown")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: RecipeEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpMarkdown: String
          _tmpMarkdown = _stmt.getText(_columnIndexOfMarkdown)
          val _tmpCalories: Int?
          if (_stmt.isNull(_columnIndexOfCalories)) {
            _tmpCalories = null
          } else {
            _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          }
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              RecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeFavorite(recipeId: String) {
    val _sql: String = "DELETE FROM recipe_favorites WHERE recipeId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, recipeId)
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
