# Build-Fehler Behebung

## Problem
Die Gradle-Build-Skript compilation schlug fehl mit:
```
e: .../app/build.gradle.kts:166:29: Unresolved reference: coroutines
e: .../app/build.gradle.kts:168:37: Unresolved reference: core
```

## Ursache
Die Libraries `libs.coroutines.test` und `libs.mockito.core` waren im Version Catalog (`gradle/libs.versions.toml`) definiert, aber möglicherweise waren nicht alle Mockito-Varianten verfügbar.

## Durchgeführte Fixes

### 1. Version Catalog Ergänzung
**Datei:** `gradle/libs.versions.toml`

**Hinzugefügt:**
```toml
mockito-inline = { module = "org.mockito:mockito-inline", version.ref = "mockito" }
```

Die `mockito-inline` Bibliothek ermöglicht das Mocking von final classes und static methods.

### 2. Build Script Ergänzung
**Datei:** `app/build.gradle.kts`

**Hinzugefügt in der Testing-Sektion:**
```kotlin
testImplementation(libs.mockito.kotlin)
testImplementation(libs.mockito.inline)
```

### 3. Bestehende Konfiguration
Die folgenden Libraries waren bereits korrekt definiert:

**Im Version Catalog:**
- `coroutines-test` = `"org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2"`
- `mockito-core` = `"org.mockito:mockito-core:5.12.0"`
- `mockito-kotlin` = `"org.mockito.kotlin:mockito-kotlin:6.0.0"`

**Im Build Script:**
- `testImplementation(libs.coroutines.test)` ✅
- `testImplementation(libs.mockito.core)` ✅

## Erwartetes Ergebnis
Nach diesen Änderungen sollten alle Test-Dependencies korrekt aufgelöst werden:

1. **Coroutines Testing**: Für asynchrone Test-Unterstützung
2. **Mockito Core**: Basis-Mocking-Framework
3. **Mockito Kotlin**: Kotlin-spezifische Mockito-Erweiterungen
4. **Mockito Inline**: Für final class/static method mocking

## Weitere empfohlene Schritte

1. **Gradle Sync** in Android Studio durchführen
2. **Build testen**: `./gradlew clean build`
3. **Tests ausführen**: `./gradlew test`

## Alternative (falls Version Catalogs Probleme verursachen)

Falls die Punkt-Notation weiterhin Probleme verursacht, können die Dependencies auch direkt angegeben werden:

```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
testImplementation("org.mockito:mockito-core:5.12.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")
testImplementation("org.mockito:mockito-inline:5.12.0")
```

## Test-Klassen, die diese Dependencies nutzen
- `PersonalAchievementManagerTest`
- `CloudSyncManagerUnitTest`
- Weitere Unit Tests im `test/` Verzeichnis
