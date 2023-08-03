package com.tiesiogdvd.composetest.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.tiesiogdvd.composetest.data.PlayModePreferences
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.SongSortOrder
import com.tiesiogdvd.composetest.data.SortOrder
import com.tiesiogdvd.composetest.jniMethods.AudioFlux
import com.tiesiogdvd.composetest.ui.libraryPlaylist.BitmapLoader
import com.tiesiogdvd.composetest.util.BitmapPalette
import com.tiesiogdvd.composetest.util.ImagePalette
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import fetchLyrics4
import getAudioFloatArray
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import kotlin.random.Random

@UnstableApi class ServiceConnector(
    val context:Context,
    val preferencesManager: PreferencesManager,
    val musicSource: MusicSource,
    val musicDao: MusicDao
) {
    val sourcePreferencesFlow = preferencesManager.currentSongFlow
    val currentSource = sourcePreferencesFlow.flatMapLatest {
        musicDao.getSongFromId(it.currentSongID)!!
    }

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    private val _currentSong = MutableLiveData<MediaMetadata?>()
    val currentSong:  LiveData<MediaMetadata?>  = _currentSong

    private val _playbackState = MutableStateFlow<Boolean?>(false)
    val playbackState: StateFlow<Boolean?> = _playbackState

    val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))

    private var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private val job = Job()
    private val connectorScope = CoroutineScope(Dispatchers.IO + job)

    var bitmap = MutableStateFlow<ImageBitmap?>(null)
    var palette = MutableStateFlow<ImagePalette?>(BitmapPalette.defaultPalette())

    val currentSongData = MutableStateFlow<Song?>(null)
    val currentSongLyrics = MutableStateFlow<String?>(null)

    val currentSongFloatArray = MutableStateFlow<FloatArray?>(null)

    val curSongIndex = MutableStateFlow<Int?>(null)


    val seedFlow = preferencesManager.getShuffleSeed()
    val sourcePlaylistFlow = musicSource.sourcePlaylist

    val shuffleStatus = MutableStateFlow<Boolean>(false)
    val shuffleOrder = MutableStateFlow<ShuffleOrder?>(null)

    val curSongList = MutableStateFlow<List<Song>?>(null)
    var curShuffleList = MutableStateFlow<List<Int>>(emptyList())


    val playerScreenSettings = preferencesManager.currentPlayModeFlow



    val combinedFlow: Flow<Triple<PlayModePreferences, Int, List<Song>>> =
        playerScreenSettings.combine(seedFlow) { playerScreenSettings, seed ->
            Pair(playerScreenSettings, seed)
        }.combine(sourcePlaylistFlow) { pair, songList ->
            Triple(pair.first, pair.second, songList)
        }

    init{
        combinedFlow.asLiveData().observeForever{
            val prefs = it.first
            val seed = it.second
            val songs = it.third

            shuffleStatus.value = prefs.isShuffleEnabled
            curShuffleList.value = getShuffleOrder(songs.size, seed)
        }
    }

    init {
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({setController()}, MoreExecutors.directExecutor())

        currentSource.asLiveData().observeForever{
            currentSongData.value = it
            println("updating song connector ${this}")
            if(it!=null){
                loadSongInfo(it)
            }
        }
        currentSong.observeForever{}
    }

    var job2: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    fun printAvailableJniLibs(context: Context) {
        try {
            val applicationInfo: ApplicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )

            val nativeLibraryDir = applicationInfo.nativeLibraryDir
            val jniLibsFolder = File(nativeLibraryDir)

            if (jniLibsFolder.isDirectory) {
                val jniLibFiles = jniLibsFolder.listFiles()
                if (jniLibFiles != null) {
                    for (file in jniLibFiles) {
                        if (file.isFile) {
                            Log.d("JNI", file.name)
                        }
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
    private fun loadSongInfo(song:Song){
        job2?.cancel()
        job2 = coroutineScope.launch {
            if(song.songPath!=null){
                val result = BitmapLoader.loadBitmapAsync(coroutineScope, song.songPath,).await()
                bitmap.value = result
                palette.value = BitmapPalette().generatePalette(bitmap.value, isDarkTheme = isSystemInDarkTheme())
            }
            currentSongLyrics.value = fetchLyrics4(song.songArtist?:"", song.songName?:"")
            //currentSongFloatArray.value = getAudioFloatArray(song.songPath)

            //printAvailableJniLibs(context)

            //val spectrogram = AudioFlux().generateMelSpectrogram(currentSongFloatArray.value, 22050, 128)

            //println("CurrentSongFloatArray")
            //Log.d("CurrentSongFloatArray", currentSongFloatArray.value?.size.toString())
            //print(currentSongFloatArray.value)

            println(song.songName)
            if (currentSongLyrics.value != null) {
                println(currentSongLyrics.value)
            } else {
                currentSongLyrics.value = null
                println("No lyrics")
            }
        }
    }

    fun isSystemInDarkTheme(): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setController() {
        val controller = this.controller?: return
        controller.addListener(object: Player.Listener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { isPlaying }


                super.onIsPlayingChanged(isPlaying)
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
               super.onMediaMetadataChanged(mediaMetadata)
                println("METADATA CHANGED")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                println("PLAYBACK STATE")
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                println(error)
            }

            override fun onTracksChanged(tracks: Tracks) {
                if(curSongIndex.value!=controller.currentMediaItemIndex){
                    curSongIndex.update { controller.currentMediaItemIndex }
                }
                super.onTracksChanged(tracks)
            }

        })

        controller.prepare()
        curSongIndex.update { controller.currentMediaItemIndex }
        controller.addListener(object: Player.Listener{})
    }


    fun changePlaybackState(){
        when(controller?.isPlaying){
            true -> {controller?.pause()}
            false -> {
                if(controller?.playbackState == ExoPlayer.COMMAND_PLAY_PAUSE){
                    controller?.prepare()
                }
                controller?.play()}
            else -> {
                controller?.prepare()
                controller?.playWhenReady = true
                controller?.play()
            }
        }
    }

    fun playNext(){
        controller?.seekToNext()
    }

    fun playPrev(){
        controller?.seekToPrevious()
    }

    fun seekTo(position: Long){
        controller?.seekTo(position)
    }

    fun getPosition():Long = controller?.currentPosition?:0

    fun getCurSongIndex():Int? = controller?.currentMediaItemIndex


    fun getShuffleOrder(length:Int, seed:Int):ArrayList<Int>{
        println("SHUFFLING SONGS")
        val array = arrayListOf<Int>()
        val shuffleOrder = ShuffleOrder.DefaultShuffleOrder(length,seed.toLong())
        this.shuffleOrder.value = shuffleOrder

        var index = shuffleOrder.firstIndex
        if(index>=0){
            do {
                Log.d("shuffle", index.toString())
                array.add(index)
                index = shuffleOrder.getNextIndex(index)
            } while (index != shuffleOrder.firstIndex && index!=-1)
        }
        print(array)
        return array
    }

    fun shuffleSongs(shuffleList: ArrayList<Int>, songList: List<Song>): List<Song> {
        println("SHUFFLING SONGS")
        /*if (songList.size != shuffleList.size) {
            return songList
        }*/
        val shuffledSongList = mutableListOf<Song>()
        for (index in shuffleList) {
            shuffledSongList.add(songList[index])
        }

        return shuffledSongList
    }

    suspend fun toggleShuffle(shuffleStatus: Boolean){
        if(shuffleStatus && controller?.mediaItemCount!=null){
            controller?.shuffleModeEnabled = true
            val seed = Random.nextInt(0, 1000)
            curShuffleList.value = getShuffleOrder(musicSource.sourceDataCurrent.size, seed)
            curSongList.value = shuffleSongs(curShuffleList.value as ArrayList<Int>, musicSource.sourceDataCurrent)
            musicSource.setShuffleOrder(shuffleOrder.value?: ShuffleOrder.DefaultShuffleOrder(5,5))
            preferencesManager.updateShuffleSeed(seed)
        }else{
            controller?.shuffleModeEnabled = false
            curSongList.value = musicSource.sourceDataCurrent
        }
        preferencesManager.updateShuffleOrder(shuffleStatus)
    }


    fun playSongFromPlaylist(song: Song, selectedPlaylistID:Int, songSortOrder:SongSortOrder, sortOrder: SortOrder, shuffle:Boolean = false) {
        connectorScope.launch {
            if (preferencesManager.getCurrentPlaylistID() != selectedPlaylistID || preferencesManager.getCurrentSongSortMode()!= songSortOrder || preferencesManager.getCurrentSortMode()!= sortOrder) {
                preferencesManager.updateSource(song.playlistId, sortOrder, songSortOrder)
                println("SOURCE CHANGEEEEE")
            }
            musicSource.songToPlay = song
            musicSource.itemIndexById(song.id).let {
                launch(Dispatchers.Main) {
                    toggleShuffle(shuffle)
                    if (it != null && it != -1) {
                        controller?.seekTo(it, 0)
                    }
                    controller?.prepare()
                    controller?.playWhenReady = true
                    //controller?.shuffleModeEnabled = shuffle
                }
            }
        }
    }
}