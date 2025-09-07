// API-Konfigurations-Checker für FitApp
// Zum Ausführen: kotlinc -script check_api_config.kt

import java.io.File

println("=== FitApp API-Konfigurations-Checker ===\n")

// 1. Local Properties prüfen
val localPropsFile = File("local.properties")
if (localPropsFile.exists()) {
    println("✓ local.properties gefunden")
    val content = localPropsFile.readText()
    
    val geminiKey = content.lines()
        .find { it.startsWith("GEMINI_API_KEY=") }
        ?.substringAfter("=")?.trim()
    
    val perplexityKey = content.lines()
        .find { it.startsWith("PERPLEXITY_API_KEY=") }
        ?.substringAfter("=")?.trim()
    
    println("Gemini API Key: ${if (geminiKey?.isNotEmpty() == true) {
        if (geminiKey.startsWith("demo_")) "❌ Demo-Schlüssel (ungültig)" 
        else if (geminiKey.startsWith("AIza")) "✓ Gültiges Format (AIza...)"
        else "⚠️  Unbekanntes Format"
    } else "❌ Nicht gesetzt"}")
    
    println("Perplexity API Key: ${if (perplexityKey?.isNotEmpty() == true) {
        if (perplexityKey.startsWith("demo_")) "❌ Demo-Schlüssel (ungültig)"
        else if (perplexityKey.startsWith("pplx-")) "✓ Gültiges Format (pplx-...)"
        else "⚠️  Unbekanntes Format"
    } else "❌ Nicht gesetzt"}")
} else {
    println("❌ local.properties nicht gefunden")
}

// 2. Build-Konfiguration prüfen
val buildFile = File("app/build.gradle.kts")
if (buildFile.exists()) {
    println("\n✓ app/build.gradle.kts gefunden")
    val content = buildFile.readText()
    
    val hasGeminiBuild = content.contains("GEMINI_API_KEY")
    val hasPerplexityBuild = content.contains("PERPLEXITY_API_KEY")
    
    println("Build-Integration: ${if (hasGeminiBuild && hasPerplexityBuild) "✓ Konfiguriert" else "❌ Fehlt"}")
} else {
    println("❌ app/build.gradle.kts nicht gefunden")
}

// 3. App-Code-Integration prüfen
val apiKeysFile = File("app/src/main/java/com/example/fitapp/data/prefs/ApiKeys.kt")
if (apiKeysFile.exists()) {
    println("\n✓ ApiKeys.kt gefunden")
    val content = apiKeysFile.readText()
    
    val hasSharedPrefs = content.contains("SharedPreferences")
    val hasGeminiMethod = content.contains("getGeminiKey")
    val hasPerplexityMethod = content.contains("getPerplexityKey")
    
    println("SharedPreferences Integration: ${if (hasSharedPrefs) "✓" else "❌"}")
    println("Gemini Key Methode: ${if (hasGeminiMethod) "✓" else "❌"}")
    println("Perplexity Key Methode: ${if (hasPerplexityMethod) "✓" else "❌"}")
} else {
    println("❌ ApiKeys.kt nicht gefunden")
}

// 4. Provider prüfen
val geminiProviderFile = File("app/src/main/java/com/example/fitapp/infrastructure/providers/GeminiAiProvider.kt")
val perplexityProviderFile = File("app/src/main/java/com/example/fitapp/infrastructure/providers/PerplexityAiProvider.kt")

println("\n=== Provider-Integration ===")
println("GeminiAiProvider: ${if (geminiProviderFile.exists()) "✓ Vorhanden" else "❌ Fehlt"}")
println("PerplexityAiProvider: ${if (perplexityProviderFile.exists()) "✓ Vorhanden" else "❌ Fehlt"}")

if (geminiProviderFile.exists()) {
    val content = geminiProviderFile.readText()
    val usesApiKeys = content.contains("ApiKeys.getGeminiKey")
    println("  └─ Verwendet ApiKeys.getGeminiKey: ${if (usesApiKeys) "✓" else "❌"}")
}

// 5. UI-Integration prüfen
val apiKeysScreenFile = File("app/src/main/java/com/example/fitapp/ui/settings/ApiKeysScreen.kt")
println("\n=== UI-Integration ===")
println("ApiKeysScreen: ${if (apiKeysScreenFile.exists()) "✓ Vorhanden" else "❌ Fehlt"}")

if (apiKeysScreenFile.exists()) {
    val content = apiKeysScreenFile.readText()
    val hasSaveMethod = content.contains("ApiKeys.saveGeminiKey")
    println("  └─ Speicher-Funktionalität: ${if (hasSaveMethod) "✓" else "❌"}")
}

println("\n=== Zusammenfassung ===")
println("Die App ist so konfiguriert, dass:")
println("1. API-Schlüssel in der App über ApiKeysScreen eingegeben werden")
println("2. Schlüssel in SharedPreferences gespeichert werden (nicht BuildConfig)")
println("3. Provider die Schlüssel aus SharedPreferences lesen")
println("4. local.properties nur für Build-Zeit verwendet wird")
println("\nAktuelles Problem: Demo-Schlüssel in local.properties sind ungültig")
println("Lösung: Echte API-Schlüssel in der App unter Einstellungen → API-Schlüssel eingeben")
