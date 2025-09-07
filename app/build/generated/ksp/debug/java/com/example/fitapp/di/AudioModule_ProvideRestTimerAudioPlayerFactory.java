package com.example.fitapp.di;

import android.content.Context;
import com.example.fitapp.services.RestTimerAudioPlayer;
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
public final class AudioModule_ProvideRestTimerAudioPlayerFactory implements Factory<RestTimerAudioPlayer> {
  private final Provider<Context> contextProvider;

  public AudioModule_ProvideRestTimerAudioPlayerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RestTimerAudioPlayer get() {
    return provideRestTimerAudioPlayer(contextProvider.get());
  }

  public static AudioModule_ProvideRestTimerAudioPlayerFactory create(
      Provider<Context> contextProvider) {
    return new AudioModule_ProvideRestTimerAudioPlayerFactory(contextProvider);
  }

  public static RestTimerAudioPlayer provideRestTimerAudioPlayer(Context context) {
    return Preconditions.checkNotNullFromProvides(AudioModule.INSTANCE.provideRestTimerAudioPlayer(context));
  }
}
