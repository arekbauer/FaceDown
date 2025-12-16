package com.arekb.facedown.di

import com.arekb.facedown.data.audio.AndroidAudioPlayer
import com.arekb.facedown.domain.audio.AudioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(
        androidAudioPlayer: AndroidAudioPlayer
    ): AudioPlayer
}