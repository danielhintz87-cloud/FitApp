package com.example.fitapp.di;

import com.example.fitapp.data.prefs.UserPreferences;
import com.example.fitapp.data.prefs.UserPreferencesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DataStoreModule_ProvideUserPreferencesFactory implements Factory<UserPreferences> {
  private final Provider<UserPreferencesRepository> repositoryProvider;

  public DataStoreModule_ProvideUserPreferencesFactory(
      Provider<UserPreferencesRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public UserPreferences get() {
    return provideUserPreferences(repositoryProvider.get());
  }

  public static DataStoreModule_ProvideUserPreferencesFactory create(
      Provider<UserPreferencesRepository> repositoryProvider) {
    return new DataStoreModule_ProvideUserPreferencesFactory(repositoryProvider);
  }

  public static UserPreferences provideUserPreferences(UserPreferencesRepository repository) {
    return Preconditions.checkNotNullFromProvides(DataStoreModule.INSTANCE.provideUserPreferences(repository));
  }
}
