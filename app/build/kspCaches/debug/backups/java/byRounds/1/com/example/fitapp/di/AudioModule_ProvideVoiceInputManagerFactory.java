package com.example.fitapp.di;

import android.content.Context;
import com.example.fitapp.services.VoiceInputManager;
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
public final class AudioModule_ProvideVoiceInputManagerFactory implements Factory<VoiceInputManager> {
  private final Provider<Context> contextProvider;

  public AudioModule_ProvideVoiceInputManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VoiceInputManager get() {
    return provideVoiceInputManager(contextProvider.get());
  }

  public static AudioModule_ProvideVoiceInputManagerFactory create(
      Provider<Context> contextProvider) {
    return new AudioModule_ProvideVoiceInputManagerFactory(contextProvider);
  }

  public static VoiceInputManager provideVoiceInputManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AudioModule.INSTANCE.provideVoiceInputManager(context));
  }
}
