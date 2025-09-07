package com.example.fitapp.ui.settings;

import com.example.fitapp.data.prefs.UserPreferencesRepository;
import com.example.fitapp.network.healthconnect.HealthConnectManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class HealthConnectSettingsViewModel_Factory implements Factory<HealthConnectSettingsViewModel> {
  private final Provider<HealthConnectManager> healthConnectManagerProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  public HealthConnectSettingsViewModel_Factory(
      Provider<HealthConnectManager> healthConnectManagerProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.healthConnectManagerProvider = healthConnectManagerProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public HealthConnectSettingsViewModel get() {
    return newInstance(healthConnectManagerProvider.get(), userPreferencesRepositoryProvider.get());
  }

  public static HealthConnectSettingsViewModel_Factory create(
      Provider<HealthConnectManager> healthConnectManagerProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new HealthConnectSettingsViewModel_Factory(healthConnectManagerProvider, userPreferencesRepositoryProvider);
  }

  public static HealthConnectSettingsViewModel newInstance(
      HealthConnectManager healthConnectManager,
      UserPreferencesRepository userPreferencesRepository) {
    return new HealthConnectSettingsViewModel(healthConnectManager, userPreferencesRepository);
  }
}
