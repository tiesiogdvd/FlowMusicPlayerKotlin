package com.tiesiogdvd.playlistssongstest.di

import android.app.Application
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.room.Room
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.MusicDatabase
import com.tiesiogdvd.service.MusicSource
import com.tiesiogdvd.service.ServiceConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@UnstableApi @Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideServiceConnection(context:Application)=ServiceConnector(context)


    @Provides
    @Singleton
    fun provideDatabase(
        app:Application,
        callback: MusicDatabase.Callback
    ) = Room.databaseBuilder(app, MusicDatabase::class.java, "music_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()


    @Provides
    fun provideMusicDao(db:MusicDatabase) = db.musicDao()



    @SingletonComponentScope
    @Provides
    @Singleton
    fun provideSingletonComponentScope() = CoroutineScope(SupervisorJob())

    @Singleton
    @Provides
    fun provideMusic(app: Application, dao: MusicDao) = SongDataGetMusicInfo



    @Singleton
    @Provides
    fun providePreferencesManager(context: Application)=PreferencesManager(context)

}


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class  SingletonComponentScope