package com.example.fitapp.ai;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J,\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\u0010J$\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u000e\u001a\u00020\u000bH\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0012\u0010\u0013J,\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\n2\u0006\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u0017H\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0018\u0010\u0019J6\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00150\n2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u001b\u001a\u00020\u000bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001c\u0010\u001dJ$\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u000e\u001a\u00020\u000bH\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001f\u0010\u0013J,\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00150\n2\u0006\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u0017H\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b!\u0010\u0019J,\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010#\u001a\u00020$H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b%\u0010&J,\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010#\u001a\u00020(H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b)\u0010*J$\u0010+\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u000e\u001a\u00020\u000bH\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b,\u0010\u0013J,\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00150\n2\u0006\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u0017H\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b.\u0010\u0019J\u0010\u0010/\u001a\u00020\u00152\u0006\u00100\u001a\u00020\u000bH\u0002J\f\u00101\u001a\u00020\u000b*\u000202H\u0002J\f\u0010\u0007\u001a\u00020\u000b*\u00020\u000bH\u0002J\u0016\u00103\u001a\u000202*\u00020\u00172\b\b\u0002\u00104\u001a\u000205H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00066"}, d2 = {"Lcom/example/fitapp/ai/AiCore;", "", "logDao", "Lcom/example/fitapp/data/db/AiLogDao;", "(Lcom/example/fitapp/data/db/AiLogDao;)V", "http", "Lokhttp3/OkHttpClient;", "json", "Lkotlinx/serialization/json/Json;", "callText", "Lkotlin/Result;", "", "provider", "Lcom/example/fitapp/ai/AiProvider;", "prompt", "callText-0E7RQCE", "(Lcom/example/fitapp/ai/AiProvider;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deepseekText", "deepseekText-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deepseekVision", "Lcom/example/fitapp/ai/CaloriesEstimate;", "bitmap", "Landroid/graphics/Bitmap;", "deepseekVision-0E7RQCE", "(Ljava/lang/String;Landroid/graphics/Bitmap;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "estimateCaloriesFromPhoto", "note", "estimateCaloriesFromPhoto-BWLJW6A", "(Lcom/example/fitapp/ai/AiProvider;Landroid/graphics/Bitmap;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "geminiText", "geminiText-gIAlu-s", "geminiVision", "geminiVision-0E7RQCE", "generatePlan", "req", "Lcom/example/fitapp/ai/PlanRequest;", "generatePlan-0E7RQCE", "(Lcom/example/fitapp/ai/AiProvider;Lcom/example/fitapp/ai/PlanRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateRecipes", "Lcom/example/fitapp/ai/RecipeRequest;", "generateRecipes-0E7RQCE", "(Lcom/example/fitapp/ai/AiProvider;Lcom/example/fitapp/ai/RecipeRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "openAiChat", "openAiChat-gIAlu-s", "openAiVision", "openAiVision-0E7RQCE", "parseCalories", "text", "b64", "", "toJpegBytes", "quality", "", "app_debug"})
public final class AiCore {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitapp.data.db.AiLogDao logDao = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient http = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    
    public AiCore(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.data.db.AiLogDao logDao) {
        super();
    }
    
    private final com.example.fitapp.ai.CaloriesEstimate parseCalories(java.lang.String text) {
        return null;
    }
    
    private final java.lang.String json(java.lang.String $this$json) {
        return null;
    }
    
    private final byte[] toJpegBytes(android.graphics.Bitmap $this$toJpegBytes, int quality) {
        return null;
    }
    
    private final java.lang.String b64(byte[] $this$b64) {
        return null;
    }
}