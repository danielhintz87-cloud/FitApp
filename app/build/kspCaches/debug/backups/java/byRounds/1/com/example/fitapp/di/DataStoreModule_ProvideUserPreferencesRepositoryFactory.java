package com.example.fitapp.di;

import android.content.Context;
import com.example.fitapp.data.prefs.UserPreferencesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DataStoreModule_ProvideUserPreferencesRepositoryFactory implements Factory<UserPreferencesRepository> {
  private final Provider<Context> contextProvider;

  public DataStoreModule_ProvideUserPreferencesRepositoryFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserPreferencesRepository get() {
    return provideUserPreferencesRepository(contextProvider.get());
  }

  public static DataStoreModule_ProvideUserPreferencesRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new DataStoreModule_ProvideUserPreferencesRepositoryFactory(contextProvider);
  }

  public static UserPreferencesRepository provideUserPreferencesRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(DataStoreModule.INSTANCE.provideUserPreferencesRepository(context));
  }
}
