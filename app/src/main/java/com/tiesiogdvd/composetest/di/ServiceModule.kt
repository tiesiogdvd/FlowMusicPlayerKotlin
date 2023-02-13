package com.tiesiogdvd.composetest.di

import android.app.Application
import android.app.Service
import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.service.MusicSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import okhttp3.internal.userAgent
import javax.inject.Singleton

@UnstableApi @Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped //Equivalent of singleton
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ) = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes,true)
        setHandleAudioBecomingNoisy(true)
    }


    @ServiceScoped
    @Provides
    fun provideMusicSource(musicDao: MusicDao) = MusicSource(musicDao)

    @ServiceScoped //Equivalent of singleton
    @Provides
    fun provideDataSourceFactory(@ApplicationContext context: Context
    ) = DefaultDataSource.Factory(context)



}


