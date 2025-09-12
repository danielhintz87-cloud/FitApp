# Feature-Modul Implementierungsleitfaden

Dieses Dokument beschreibt die Schritte zur Implementierung eines neuen Feature-Moduls in der FitApp.

## 1. Modul-Struktur erstellen

Ein typisches Feature-Modul sollte folgende Struktur haben:

```
feature/myfeature/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/de/hhn/fitapp/feature/myfeature/
│   │   │   ├── di/                   # Hilt-Module
│   │   │   ├── domain/               # Feature-spezifische Domain-Klassen
│   │   │   │   ├── model/            # Datenmodelle
│   │   │   │   ├── repository/       # Repository-Interfaces
│   │   │   │   └── usecase/          # Use Cases
│   │   │   ├── data/                 # Datenquellen
│   │   │   │   ├── local/            # Lokale Persistenz (Room, DataStore)
│   │   │   │   ├── remote/           # API-Clients
│   │   │   │   └── repository/       # Repository-Implementierungen
│   │   │   ├── ui/                   # UI-Komponenten
│   │   │   │   ├── screens/          # Hauptbildschirme
│   │   │   │   ├── components/       # Wiederverwendbare UI-Komponenten
│   │   │   │   └── viewmodel/        # ViewModels
│   │   │   └── navigation/           # Navigation-Integration
│   │   │       └── MyFeatureNavigationProvider.kt
│   │   └── res/                     # Ressourcen
│   └── test/                        # Unit-Tests
└── schemas/                         # Room-Schemas (wenn benötigt)
```

## 2. build.gradle.kts konfigurieren

```kotlin
// Verwendung der gemeinsamen Konfiguration
configureAndroidLibrary(
    namespace = "de.hhn.fitapp.feature.myfeature",
    dependencies = ModuleDependencies.FEATURE_BASIC,  // oder FEATURE_FULL für Datenbank/DataStore
    enableCompose = true
)

// Abhängigkeiten von Core-Modulen
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    
    // Optional, wenn Datenpersistenz benötigt wird
    implementation(project(":core:data"))
}
```

## 3. Navigation einrichten

Erstellen Sie einen NavigationProvider für das Feature:

```kotlin
@Singleton
class MyFeatureNavigationProvider @Inject constructor() : NavigationProvider {
    override fun registerDestinations(registry: NavigationRegistry) {
        // Hauptscreen registrieren
        registry.registerDestination(
            composeDestination(NavigationRoutes.MyFeature.MAIN)
                .setContent { navController, _ ->
                    MyFeatureScreen(navController)
                }
                .build()
        )
        
        // Weitere Screens registrieren
        registry.registerDestination(
            composeDestination(NavigationRoutes.MyFeature.DETAILS)
                .addArgument("itemId", NavArgumentType.STRING)
                .setContent { navController, backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                    MyFeatureDetailsScreen(navController, itemId)
                }
                .build()
        )
    }
}
```

## 4. Hilt-Module erstellen

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class MyFeatureModule {
    
    @Binds
    abstract fun bindMyFeatureRepository(
        repository: MyFeatureRepositoryImpl
    ): MyFeatureRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideNavigationProvider(
            provider: MyFeatureNavigationProvider
        ): NavigationProvider = provider
    }
}
```

## 5. Module in app/build.gradle.kts einbinden

```kotlin
dependencies {
    // Bestehende Abhängigkeiten...
    
    // Feature-Module einbinden
    implementation(project(":feature:myfeature"))
}
```

## 6. Navigation in settings.gradle.kts aktivieren

```kotlin
// Feature Module
include(":feature:myfeature")
```

## 7. Navigation in App-Modul registrieren

Im Application-Klasse oder einem zentralen Hilt-Modul:

```kotlin
@Singleton
class NavigationProviderRegistrar @Inject constructor(
    private val navigationRegistry: NavigationRegistry,
    private val myFeatureNavigationProvider: NavigationProvider,
    // Weitere NavigationProvider...
) {
    fun registerProviders() {
        navigationRegistry.registerNavigationProvider(myFeatureNavigationProvider)
        // Weitere Provider registrieren...
    }
}
```

## 8. Testen

- Erstellen Sie Unit-Tests für Repository und UseCase-Implementierungen
- Fügen Sie Tests für kritische UI-Funktionalitäten hinzu
- Stellen Sie sicher, dass die Navigation korrekt funktioniert