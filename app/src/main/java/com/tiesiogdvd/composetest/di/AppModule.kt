package com.tiesiogdvd.playlistssongstest.di

import android.app.Application
import android.content.Context
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultDataSource.Factory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.room.Room
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.settings.SettingsManager
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.MusicDatabase
import com.tiesiogdvd.composetest.service.MusicSource
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavbarController
import com.tiesiogdvd.composetest.ui.musicPlayer.MusicPlayerViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton


class CustomViewModelFactory @Inject constructor(
    val viewModelProviderFactory: ViewModelProvider.Factory
) {
    inline fun <reified T : ViewModel> createViewModel(): T {
        return T::class.java.getConstructor(ViewModelProvider.Factory::class.java)
            .newInstance(viewModelProviderFactory)
    }
}

@UnstableApi @Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideServiceConnection(context:Application, preferencesManager: PreferencesManager, musicSource: MusicSource, musicDao: MusicDao)= ServiceConnector(context, preferencesManager, musicSource, musicDao)

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


    @Singleton
    @Provides
    fun provideDataSourceFactory(@ApplicationContext context: Context
    ) = DefaultDataSource.Factory(context)

    @Singleton
    @Provides
    fun provideProgressiveMediaSourceFactory(dataSourceFactory: Factory) = ProgressiveMediaSource.Factory(dataSourceFactory)

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


    @Singleton
    @Provides
    fun provideSettingsManager(context: Application)=SettingsManager(context)



    @Singleton
    @Provides
    fun provideMusicSource(musicDao: MusicDao, preferencesManager: PreferencesManager, exoPlayer: ExoPlayer) = MusicSource(musicDao,exoPlayer, preferencesManager)

    @Singleton
    @Provides
    fun provideNavbarController() = NavbarController()



    @Singleton //Equivalent of singleton
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()



    @Singleton
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ) =
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(audioAttributes,true)
            setHandleAudioBecomingNoisy(true)
            pauseAtEndOfMediaItems = false
            setForegroundMode(true)
        }

}



@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class  SingletonComponentScope