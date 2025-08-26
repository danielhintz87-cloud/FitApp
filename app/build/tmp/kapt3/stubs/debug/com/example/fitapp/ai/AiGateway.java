package com.example.fitapp.ai;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001(B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J(\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\b\u0010\u0012\u001a\u00020\u000fH\u0002J\u0018\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u000fH\u0003J\u0018\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u000fH\u0003J&\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001a2\u0006\u0010\u0018\u001a\u00020\u000f2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0018\u0010\u001d\u001a\u00020\u000f2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\nH\u0002J\u0018\u0010!\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u000fH\u0003J\u0016\u0010\"\u001a\u00020\u00112\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00110\u001aH\u0003J\b\u0010$\u001a\u00020\u000fH\u0002J\b\u0010%\u001a\u00020\u000fH\u0002J\u0016\u0010&\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001a2\u0006\u0010\'\u001a\u00020\u000fH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/example/fitapp/ai/AiGateway;", "", "()V", "http", "Lokhttp3/OkHttpClient;", "analyzeFoodImage", "Lcom/example/fitapp/ai/CalorieEstimate;", "context", "Landroid/content/Context;", "imageUri", "Landroid/net/Uri;", "provider", "Lcom/example/fitapp/ai/AiGateway$Provider;", "(Landroid/content/Context;Landroid/net/Uri;Lcom/example/fitapp/ai/AiGateway$Provider;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractContentText", "", "json", "Lorg/json/JSONObject;", "geminiKey", "geminiText", "system", "user", "geminiVision", "base64Jpeg", "prompt", "generateRecipes", "", "Lcom/example/fitapp/ai/UiRecipe;", "(Ljava/lang/String;Lcom/example/fitapp/ai/AiGateway$Provider;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadBitmapBase64", "cr", "Landroid/content/ContentResolver;", "uri", "openAiChat", "openAiChatRaw", "messages", "openAiKey", "openAiModel", "parseMarkdownRecipes", "markdown", "Provider", "app_debug"})
public final class AiGateway {
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient http = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.fitapp.ai.AiGateway INSTANCE = null;
    
    private AiGateway() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateRecipes(@org.jetbrains.annotations.NotNull()
    java.lang.String prompt, @org.jetbrains.annotations.NotNull()
    com.example.fitapp.ai.AiGateway.Provider provider, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.fitapp.ai.UiRecipe>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object analyzeFoodImage(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri imageUri, @org.jetbrains.annotations.NotNull()
    com.example.fitapp.ai.AiGateway.Provider provider, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.fitapp.ai.CalorieEstimate> $completion) {
        return null;
    }
    
    private final java.lang.String openAiKey() {
        return null;
    }
    
    private final java.lang.String openAiModel() {
        return null;
    }
    
    @androidx.annotation.WorkerThread()
    private final java.lang.String openAiChat(java.lang.String system, java.lang.String user) {
        return null;
    }
    
    @androidx.annotation.WorkerThread()
    private final org.json.JSONObject openAiChatRaw(java.util.List<? extends org.json.JSONObject> messages) {
        return null;
    }
    
    private final java.lang.String geminiKey() {
        return null;
    }
    
    @androidx.annotation.WorkerThread()
    private final java.lang.String geminiText(java.lang.String system, java.lang.String user) {
        return null;
    }
    
    @androidx.annotation.WorkerThread()
    private final org.json.JSONObject geminiVision(java.lang.String base64Jpeg, java.lang.String prompt) {
        return null;
    }
    
    private final java.lang.String extractContentText(org.json.JSONObject json) {
        return null;
    }
    
    private final java.lang.String loadBitmapBase64(android.content.ContentResolver cr, android.net.Uri uri) {
        return null;
    }
    
    private final java.util.List<com.example.fitapp.ai.UiRecipe> parseMarkdownRecipes(java.lang.String markdown) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/example/fitapp/ai/AiGateway$Provider;", "", "(Ljava/lang/String;I)V", "OPENAI", "GEMINI", "app_debug"})
    public static enum Provider {
        /*public static final*/ OPENAI /* = new OPENAI() */,
        /*public static final*/ GEMINI /* = new GEMINI() */;
        
        Provider() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.example.fitapp.ai.AiGateway.Provider> getEntries() {
            return null;
        }
    }
}