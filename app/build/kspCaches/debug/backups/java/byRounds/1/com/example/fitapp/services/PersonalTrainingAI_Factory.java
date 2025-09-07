package com.example.fitapp.services;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class PersonalTrainingAI_Factory implements Factory<PersonalTrainingAI> {
  private final Provider<Context> contextProvider;

  public PersonalTrainingAI_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PersonalTrainingAI get() {
    return newInstance(contextProvider.get());
  }

  public static PersonalTrainingAI_Factory create(Provider<Context> contextProvider) {
    return new PersonalTrainingAI_Factory(contextProvider);
  }

  public static PersonalTrainingAI newInstance(Context context) {
    return new PersonalTrainingAI(context);
  }
}
