package com.example.fitapp;

import com.example.fitapp.data.prefs.UserPreferencesRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class FitAppApplication_MembersInjector implements MembersInjector<FitAppApplication> {
  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  public FitAppApplication_MembersInjector(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  public static MembersInjector<FitAppApplication> create(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new FitAppApplication_MembersInjector(userPreferencesRepositoryProvider);
  }

  @Override
  public void injectMembers(FitAppApplication instance) {
    injectUserPreferencesRepository(instance, userPreferencesRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.example.fitapp.FitAppApplication.userPreferencesRepository")
  public static void injectUserPreferencesRepository(FitAppApplication instance,
      UserPreferencesRepository userPreferencesRepository) {
    instance.userPreferencesRepository = userPreferencesRepository;
  }
}
