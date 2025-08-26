package com.example.fitapp.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\'J\u0018\u0010\u000b\u001a\u0004\u0018\u00010\n2\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0014\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\'J\u0016\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0015\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u0016\u001a\u00020\u00032\u0006\u0010\u0017\u001a\u00020\nH\u0097@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u00032\u0006\u0010\u0017\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0018\u00a8\u0006\u001a"}, d2 = {"Lcom/example/fitapp/data/db/RecipeDao;", "", "addFavorite", "", "fav", "Lcom/example/fitapp/data/db/RecipeFavoriteEntity;", "(Lcom/example/fitapp/data/db/RecipeFavoriteEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "favoritesFlow", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitapp/data/db/RecipeEntity;", "getRecipe", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "historyFlow", "insertHistory", "history", "Lcom/example/fitapp/data/db/RecipeHistoryEntity;", "(Lcom/example/fitapp/data/db/RecipeHistoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "removeFavorite", "recipeId", "upsertAndAddToHistory", "recipe", "(Lcom/example/fitapp/data/db/RecipeEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertRecipe", "app_debug"})
@androidx.room.Dao()
public abstract interface RecipeDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertRecipe(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.data.db.RecipeEntity recipe, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Transaction()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertAndAddToHistory(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.data.db.RecipeEntity recipe, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 5)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addFavorite(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.data.db.RecipeFavoriteEntity fav, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM recipe_favorites WHERE recipeId = :recipeId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object removeFavorite(@org.jetbrains.annotations.NotNull()
    java.lang.String recipeId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "\n        SELECT r.* FROM recipes r\n        INNER JOIN recipe_favorites f ON r.id = f.recipeId\n        ORDER BY f.savedAt DESC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitapp.data.db.RecipeEntity>> favoritesFlow();
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertHistory(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.data.db.RecipeHistoryEntity history, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "\n        SELECT r.* FROM recipes r\n        INNER JOIN recipe_history h ON r.id = h.recipeId\n        ORDER BY h.createdAt DESC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitapp.data.db.RecipeEntity>> historyFlow();
    
    @androidx.room.Query(value = "SELECT * FROM recipes WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getRecipe(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.fitapp.data.db.RecipeEntity> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
        
        @androidx.room.Transaction()
        @org.jetbrains.annotations.Nullable()
        public static java.lang.Object upsertAndAddToHistory(@org.jetbrains.annotations.NotNull()
        com.example.fitapp.data.db.RecipeDao $this, @org.jetbrains.annotations.NotNull()
        com.example.fitapp.data.db.RecipeEntity recipe, @org.jetbrains.annotations.NotNull()
        kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
            return null;
        }
    }
}