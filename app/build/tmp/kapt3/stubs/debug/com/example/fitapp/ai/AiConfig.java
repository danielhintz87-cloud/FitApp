package com.example.fitapp.ai;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\b"}, d2 = {"Lcom/example/fitapp/ai/AiConfig;", "", "()V", "apiKey", "", "provider", "Lcom/example/fitapp/ai/AiProvider;", "baseUrl", "app_debug"})
public final class AiConfig {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.fitapp.ai.AiConfig INSTANCE = null;
    
    private AiConfig() {
        super();
    }
    
    /**
     * Returns API key for the given provider.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String apiKey(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.ai.AiProvider provider) {
        return null;
    }
    
    /**
     * Returns base URL for the given provider.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String baseUrl(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.ai.AiProvider provider) {
        return null;
    }
}