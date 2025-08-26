package com.example.fitapp.data.repo;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0084\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ&\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0012J\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\u00142\u0006\u0010\u0017\u001a\u00020\u0018J\u0016\u0010\u0019\u001a\u00020\u00062\u0006\u0010\u001a\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u0012\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001d0\u00150\u0014J$\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u00152\u0006\u0010 \u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010!J\u0016\u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010#0\u00142\u0006\u0010$\u001a\u00020%J\u0012\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001d0\u00150\u0014J2\u0010\'\u001a\u00020\u00062\u0006\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020\b2\u0006\u0010+\u001a\u00020\b2\n\b\u0002\u0010,\u001a\u0004\u0018\u00010\bH\u0086@\u00a2\u0006\u0002\u0010-J\u001e\u0010.\u001a\u00020\u00062\u0006\u0010$\u001a\u00020%2\u0006\u0010/\u001a\u00020)H\u0086@\u00a2\u0006\u0002\u00100J\u001e\u00101\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u00102\u001a\u000203H\u0086@\u00a2\u0006\u0002\u00104J\u001e\u00105\u001a\u00020\u00062\u0006\u0010\u001a\u001a\u00020\u00182\u0006\u00106\u001a\u000203H\u0086@\u00a2\u0006\u0002\u00107J\u0012\u00108\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002090\u00150\u0014J\u0016\u0010:\u001a\u00020)2\u0006\u0010\u0017\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006;"}, d2 = {"Lcom/example/fitapp/data/repo/NutritionRepository;", "", "db", "Lcom/example/fitapp/data/db/AppDatabase;", "(Lcom/example/fitapp/data/db/AppDatabase;)V", "addRecipeToShoppingList", "", "recipeId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "analyzeFoodImage", "Lcom/example/fitapp/ai/CalorieEstimate;", "ctx", "Landroid/content/Context;", "uri", "Landroid/net/Uri;", "provider", "Lcom/example/fitapp/ai/AiGateway$Provider;", "(Landroid/content/Context;Landroid/net/Uri;Lcom/example/fitapp/ai/AiGateway$Provider;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "dayEntriesFlow", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitapp/data/db/IntakeEntryEntity;", "epochSec", "", "deleteItem", "id", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "favorites", "Lcom/example/fitapp/data/db/RecipeEntity;", "generateAndStore", "Lcom/example/fitapp/ai/UiRecipe;", "prompt", "(Ljava/lang/String;Lcom/example/fitapp/ai/AiGateway$Provider;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "goalFlow", "Lcom/example/fitapp/data/db/DailyGoalEntity;", "date", "Ljava/time/LocalDate;", "history", "logIntake", "kcal", "", "label", "source", "refId", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setDailyGoal", "targetKcal", "(Ljava/time/LocalDate;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setFavorite", "fav", "", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setItemChecked", "checked", "(JZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "shoppingItems", "Lcom/example/fitapp/data/db/ShoppingItemEntity;", "totalForDay", "app_debug"})
public final class NutritionRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitapp.data.db.AppDatabase db = null;
    
    public NutritionRepository(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.data.db.AppDatabase db) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitapp.data.db.RecipeEntity>> favorites() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitapp.data.db.RecipeEntity>> history() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateAndStore(@org.jetbrains.annotations.NotNull()
    java.lang.String prompt, @org.jetbrains.annotations.NotNull()
    com.example.fitapp.ai.AiGateway.Provider provider, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.fitapp.ai.UiRecipe>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setFavorite(@org.jetbrains.annotations.NotNull()
    java.lang.String recipeId, boolean fav, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addRecipeToShoppingList(@org.jetbrains.annotations.NotNull()
    java.lang.String recipeId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object analyzeFoodImage(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    com.example.fitapp.ai.AiGateway.Provider provider, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.fitapp.ai.CalorieEstimate> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object logIntake(int kcal, @org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    java.lang.String source, @org.jetbrains.annotations.Nullable()
    java.lang.String refId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitapp.data.db.IntakeEntryEntity>> dayEntriesFlow(long epochSec) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.example.fitapp.data.db.DailyGoalEntity> goalFlow(@org.jetbrains.annotations.NotNull()
    java.time.LocalDate date) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setDailyGoal(@org.jetbrains.annotations.NotNull()
    java.time.LocalDate date, int targetKcal, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object totalForDay(long epochSec, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitapp.data.db.ShoppingItemEntity>> shoppingItems() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setItemChecked(long id, boolean checked, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteItem(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}