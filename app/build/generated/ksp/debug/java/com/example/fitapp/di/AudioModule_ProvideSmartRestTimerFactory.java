package com.example.fitapp.di;

import android.content.Context;
import com.example.fitapp.services.SmartRestTimer;
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
public final class AudioModule_ProvideSmartRestTimerFactory implements Factory<SmartRestTimer> {
  private final Provider<Context> contextProvider;

  public AudioModule_ProvideSmartRestTimerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SmartRestTimer get() {
    return provideSmartRestTimer(contextProvider.get());
  }

  public static AudioModule_ProvideSmartRestTimerFactory create(Provider<Context> contextProvider) {
    return new AudioModule_ProvideSmartRestTimerFactory(contextProvider);
  }

  public static SmartRestTimer provideSmartRestTimer(Context context) {
    return Preconditions.checkNotNullFromProvides(AudioModule.INSTANCE.provideSmartRestTimer(context));
  }
}
