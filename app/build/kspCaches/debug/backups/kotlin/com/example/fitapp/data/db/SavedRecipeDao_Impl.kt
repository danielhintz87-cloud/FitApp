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
public class SavedRecipeDao_Impl(
  __db: RoomDatabase,
) : SavedRecipeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSavedRecipeEntity: EntityInsertAdapter<SavedRecipeEntity>

  private val __updateAdapterOfSavedRecipeEntity: EntityDeleteOrUpdateAdapter<SavedRecipeEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSavedRecipeEntity = object : EntityInsertAdapter<SavedRecipeEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `saved_recipes` (`id`,`title`,`markdown`,`calories`,`imageUrl`,`ingredients`,`tags`,`prepTime`,`difficulty`,`servings`,`isFavorite`,`createdAt`,`lastCookedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SavedRecipeEntity) {
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
        statement.bindText(6, entity.ingredients)
        statement.bindText(7, entity.tags)
        val _tmpPrepTime: Int? = entity.prepTime
        if (_tmpPrepTime == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpPrepTime.toLong())
        }
        val _tmpDifficulty: String? = entity.difficulty
        if (_tmpDifficulty == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpDifficulty)
        }
        val _tmpServings: Int? = entity.servings
        if (_tmpServings == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpServings.toLong())
        }
        val _tmp: Int = if (entity.isFavorite) 1 else 0
        statement.bindLong(11, _tmp.toLong())
        statement.bindLong(12, entity.createdAt)
        val _tmpLastCookedAt: Long? = entity.lastCookedAt
        if (_tmpLastCookedAt == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpLastCookedAt)
        }
      }
    }
    this.__updateAdapterOfSavedRecipeEntity = object :
        EntityDeleteOrUpdateAdapter<SavedRecipeEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `saved_recipes` SET `id` = ?,`title` = ?,`markdown` = ?,`calories` = ?,`imageUrl` = ?,`ingredients` = ?,`tags` = ?,`prepTime` = ?,`difficulty` = ?,`servings` = ?,`isFavorite` = ?,`createdAt` = ?,`lastCookedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SavedRecipeEntity) {
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
        statement.bindText(6, entity.ingredients)
        statement.bindText(7, entity.tags)
        val _tmpPrepTime: Int? = entity.prepTime
        if (_tmpPrepTime == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpPrepTime.toLong())
        }
        val _tmpDifficulty: String? = entity.difficulty
        if (_tmpDifficulty == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpDifficulty)
        }
        val _tmpServings: Int? = entity.servings
        if (_tmpServings == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpServings.toLong())
        }
        val _tmp: Int = if (entity.isFavorite) 1 else 0
        statement.bindLong(11, _tmp.toLong())
        statement.bindLong(12, entity.createdAt)
        val _tmpLastCookedAt: Long? = entity.lastCookedAt
        if (_tmpLastCookedAt == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpLastCookedAt)
        }
        statement.bindText(14, entity.id)
      }
    }
  }

  public override suspend fun insert(recipe: SavedRecipeEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfSavedRecipeEntity.insert(_connection, recipe)
  }

  public override suspend fun update(recipe: SavedRecipeEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfSavedRecipeEntity.handle(_connection, recipe)
  }

  public override fun allRecipesFlow(): Flow<List<SavedRecipeEntity>> {
    val _sql: String =
        "SELECT `saved_recipes`.`id` AS `id`, `saved_recipes`.`title` AS `title`, `saved_recipes`.`markdown` AS `markdown`, `saved_recipes`.`calories` AS `calories`, `saved_recipes`.`imageUrl` AS `imageUrl`, `saved_recipes`.`ingredients` AS `ingredients`, `saved_recipes`.`tags` AS `tags`, `saved_recipes`.`prepTime` AS `prepTime`, `saved_recipes`.`difficulty` AS `difficulty`, `saved_recipes`.`servings` AS `servings`, `saved_recipes`.`isFavorite` AS `isFavorite`, `saved_recipes`.`createdAt` AS `createdAt`, `saved_recipes`.`lastCookedAt` AS `lastCookedAt` FROM saved_recipes ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("saved_recipes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfMarkdown: Int = 2
        val _columnIndexOfCalories: Int = 3
        val _columnIndexOfImageUrl: Int = 4
        val _columnIndexOfIngredients: Int = 5
        val _columnIndexOfTags: Int = 6
        val _columnIndexOfPrepTime: Int = 7
        val _columnIndexOfDifficulty: Int = 8
        val _columnIndexOfServings: Int = 9
        val _columnIndexOfIsFavorite: Int = 10
        val _columnIndexOfCreatedAt: Int = 11
        val _columnIndexOfLastCookedAt: Int = 12
        val _result: MutableList<SavedRecipeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedRecipeEntity
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
          val _tmpIngredients: String
          _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpPrepTime: Int?
          if (_stmt.isNull(_columnIndexOfPrepTime)) {
            _tmpPrepTime = null
          } else {
            _tmpPrepTime = _stmt.getLong(_columnIndexOfPrepTime).toInt()
          }
          val _tmpDifficulty: String?
          if (_stmt.isNull(_columnIndexOfDifficulty)) {
            _tmpDifficulty = null
          } else {
            _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          }
          val _tmpServings: Int?
          if (_stmt.isNull(_columnIndexOfServings)) {
            _tmpServings = null
          } else {
            _tmpServings = _stmt.getLong(_columnIndexOfServings).toInt()
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastCookedAt: Long?
          if (_stmt.isNull(_columnIndexOfLastCookedAt)) {
            _tmpLastCookedAt = null
          } else {
            _tmpLastCookedAt = _stmt.getLong(_columnIndexOfLastCookedAt)
          }
          _item =
              SavedRecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpIngredients,_tmpTags,_tmpPrepTime,_tmpDifficulty,_tmpServings,_tmpIsFavorite,_tmpCreatedAt,_tmpLastCookedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun favoriteRecipesFlow(): Flow<List<SavedRecipeEntity>> {
    val _sql: String =
        "SELECT `saved_recipes`.`id` AS `id`, `saved_recipes`.`title` AS `title`, `saved_recipes`.`markdown` AS `markdown`, `saved_recipes`.`calories` AS `calories`, `saved_recipes`.`imageUrl` AS `imageUrl`, `saved_recipes`.`ingredients` AS `ingredients`, `saved_recipes`.`tags` AS `tags`, `saved_recipes`.`prepTime` AS `prepTime`, `saved_recipes`.`difficulty` AS `difficulty`, `saved_recipes`.`servings` AS `servings`, `saved_recipes`.`isFavorite` AS `isFavorite`, `saved_recipes`.`createdAt` AS `createdAt`, `saved_recipes`.`lastCookedAt` AS `lastCookedAt` FROM saved_recipes WHERE isFavorite = 1 ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("saved_recipes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfTitle: Int = 1
        val _columnIndexOfMarkdown: Int = 2
        val _columnIndexOfCalories: Int = 3
        val _columnIndexOfImageUrl: Int = 4
        val _columnIndexOfIngredients: Int = 5
        val _columnIndexOfTags: Int = 6
        val _columnIndexOfPrepTime: Int = 7
        val _columnIndexOfDifficulty: Int = 8
        val _columnIndexOfServings: Int = 9
        val _columnIndexOfIsFavorite: Int = 10
        val _columnIndexOfCreatedAt: Int = 11
        val _columnIndexOfLastCookedAt: Int = 12
        val _result: MutableList<SavedRecipeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedRecipeEntity
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
          val _tmpIngredients: String
          _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpPrepTime: Int?
          if (_stmt.isNull(_columnIndexOfPrepTime)) {
            _tmpPrepTime = null
          } else {
            _tmpPrepTime = _stmt.getLong(_columnIndexOfPrepTime).toInt()
          }
          val _tmpDifficulty: String?
          if (_stmt.isNull(_columnIndexOfDifficulty)) {
            _tmpDifficulty = null
          } else {
            _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          }
          val _tmpServings: Int?
          if (_stmt.isNull(_columnIndexOfServings)) {
            _tmpServings = null
          } else {
            _tmpServings = _stmt.getLong(_columnIndexOfServings).toInt()
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastCookedAt: Long?
          if (_stmt.isNull(_columnIndexOfLastCookedAt)) {
            _tmpLastCookedAt = null
          } else {
            _tmpLastCookedAt = _stmt.getLong(_columnIndexOfLastCookedAt)
          }
          _item =
              SavedRecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpIngredients,_tmpTags,_tmpPrepTime,_tmpDifficulty,_tmpServings,_tmpIsFavorite,_tmpCreatedAt,_tmpLastCookedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun recipesByTagFlow(tag: String): Flow<List<SavedRecipeEntity>> {
    val _sql: String =
        "SELECT * FROM saved_recipes WHERE tags LIKE '%' || ? || '%' ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("saved_recipes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, tag)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfMarkdown: Int = getColumnIndexOrThrow(_stmt, "markdown")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfIngredients: Int = getColumnIndexOrThrow(_stmt, "ingredients")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfPrepTime: Int = getColumnIndexOrThrow(_stmt, "prepTime")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfServings: Int = getColumnIndexOrThrow(_stmt, "servings")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastCookedAt: Int = getColumnIndexOrThrow(_stmt, "lastCookedAt")
        val _result: MutableList<SavedRecipeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedRecipeEntity
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
          val _tmpIngredients: String
          _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpPrepTime: Int?
          if (_stmt.isNull(_columnIndexOfPrepTime)) {
            _tmpPrepTime = null
          } else {
            _tmpPrepTime = _stmt.getLong(_columnIndexOfPrepTime).toInt()
          }
          val _tmpDifficulty: String?
          if (_stmt.isNull(_columnIndexOfDifficulty)) {
            _tmpDifficulty = null
          } else {
            _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          }
          val _tmpServings: Int?
          if (_stmt.isNull(_columnIndexOfServings)) {
            _tmpServings = null
          } else {
            _tmpServings = _stmt.getLong(_columnIndexOfServings).toInt()
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastCookedAt: Long?
          if (_stmt.isNull(_columnIndexOfLastCookedAt)) {
            _tmpLastCookedAt = null
          } else {
            _tmpLastCookedAt = _stmt.getLong(_columnIndexOfLastCookedAt)
          }
          _item =
              SavedRecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpIngredients,_tmpTags,_tmpPrepTime,_tmpDifficulty,_tmpServings,_tmpIsFavorite,_tmpCreatedAt,_tmpLastCookedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun searchRecipesFlow(query: String): Flow<List<SavedRecipeEntity>> {
    val _sql: String =
        "SELECT * FROM saved_recipes WHERE title LIKE '%' || ? || '%' OR tags LIKE '%' || ? || '%' ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("saved_recipes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        _argIndex = 2
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfMarkdown: Int = getColumnIndexOrThrow(_stmt, "markdown")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfIngredients: Int = getColumnIndexOrThrow(_stmt, "ingredients")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfPrepTime: Int = getColumnIndexOrThrow(_stmt, "prepTime")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfServings: Int = getColumnIndexOrThrow(_stmt, "servings")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastCookedAt: Int = getColumnIndexOrThrow(_stmt, "lastCookedAt")
        val _result: MutableList<SavedRecipeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedRecipeEntity
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
          val _tmpIngredients: String
          _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpPrepTime: Int?
          if (_stmt.isNull(_columnIndexOfPrepTime)) {
            _tmpPrepTime = null
          } else {
            _tmpPrepTime = _stmt.getLong(_columnIndexOfPrepTime).toInt()
          }
          val _tmpDifficulty: String?
          if (_stmt.isNull(_columnIndexOfDifficulty)) {
            _tmpDifficulty = null
          } else {
            _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          }
          val _tmpServings: Int?
          if (_stmt.isNull(_columnIndexOfServings)) {
            _tmpServings = null
          } else {
            _tmpServings = _stmt.getLong(_columnIndexOfServings).toInt()
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastCookedAt: Long?
          if (_stmt.isNull(_columnIndexOfLastCookedAt)) {
            _tmpLastCookedAt = null
          } else {
            _tmpLastCookedAt = _stmt.getLong(_columnIndexOfLastCookedAt)
          }
          _item =
              SavedRecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpIngredients,_tmpTags,_tmpPrepTime,_tmpDifficulty,_tmpServings,_tmpIsFavorite,_tmpCreatedAt,_tmpLastCookedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecipe(id: String): SavedRecipeEntity? {
    val _sql: String = "SELECT * FROM saved_recipes WHERE id = ?"
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
        val _columnIndexOfIngredients: Int = getColumnIndexOrThrow(_stmt, "ingredients")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfPrepTime: Int = getColumnIndexOrThrow(_stmt, "prepTime")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfServings: Int = getColumnIndexOrThrow(_stmt, "servings")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastCookedAt: Int = getColumnIndexOrThrow(_stmt, "lastCookedAt")
        val _result: SavedRecipeEntity?
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
          val _tmpIngredients: String
          _tmpIngredients = _stmt.getText(_columnIndexOfIngredients)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpPrepTime: Int?
          if (_stmt.isNull(_columnIndexOfPrepTime)) {
            _tmpPrepTime = null
          } else {
            _tmpPrepTime = _stmt.getLong(_columnIndexOfPrepTime).toInt()
          }
          val _tmpDifficulty: String?
          if (_stmt.isNull(_columnIndexOfDifficulty)) {
            _tmpDifficulty = null
          } else {
            _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          }
          val _tmpServings: Int?
          if (_stmt.isNull(_columnIndexOfServings)) {
            _tmpServings = null
          } else {
            _tmpServings = _stmt.getLong(_columnIndexOfServings).toInt()
          }
          val _tmpIsFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastCookedAt: Long?
          if (_stmt.isNull(_columnIndexOfLastCookedAt)) {
            _tmpLastCookedAt = null
          } else {
            _tmpLastCookedAt = _stmt.getLong(_columnIndexOfLastCookedAt)
          }
          _result =
              SavedRecipeEntity(_tmpId,_tmpTitle,_tmpMarkdown,_tmpCalories,_tmpImageUrl,_tmpIngredients,_tmpTags,_tmpPrepTime,_tmpDifficulty,_tmpServings,_tmpIsFavorite,_tmpCreatedAt,_tmpLastCookedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM saved_recipes WHERE id = ?"
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

  public override suspend fun setFavorite(id: String, favorite: Boolean) {
    val _sql: String = "UPDATE saved_recipes SET isFavorite = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (favorite) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsCooked(id: String, timestamp: Long) {
    val _sql: String = "UPDATE saved_recipes SET lastCookedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, timestamp)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM saved_recipes"
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
