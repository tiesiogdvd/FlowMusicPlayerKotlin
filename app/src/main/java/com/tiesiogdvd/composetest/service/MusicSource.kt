package com.tiesiogdvd.composetest.service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.SourcePreferences
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import kotlin.collections.HashMap

data class MediaItemCombined(
    var mediaItem: MediaItem,
    var prevIndex: Int?,
    var currentIndex: Int
)

@UnstableApi class MusicSource @Inject constructor(
    musicDao: MusicDao,
    private var exoPlayer: ExoPlayer,
    private var preferencesManager: PreferencesManager
){
    var songs = emptyList<MediaItem>()
    val concatenatingMediaSource = ConcatenatingMediaSource()
    val concatenatingSource = MutableLiveData(ConcatenatingMediaSource())

    var songToPlay:Song? = null
    var currentSong : MediaItem? = null
    var currentPosition: Int = 0


    val currentPlaylistID = preferencesManager.currentSourceFlow
    val sourcePlaylist = currentPlaylistID.flatMapLatest {
        currentSourceSettings = it
        musicDao.getPlaylistSongs(it.currentSource, "", sortOrder = it.sortOrder, songSortOrder = it.songSortOrder)
    }
    val sourceLiveData = sourcePlaylist.asLiveData()


    var prevSourceSettings: SourcePreferences? = null
    var currentSourceSettings:SourcePreferences? = null

    var songsMapPrevious = mutableMapOf<Int,MediaItemCombined>()
    var songsMapCurrent = mutableMapOf<Int,MediaItemCombined>()

    var indexesToRemove = mutableListOf<Int>()

    var sourceDataCurrent = emptyList<Song>()
    var sourceDataPrevious = emptyList<Song>()


    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    init {
        sourceLiveData.observeForever{
            sourceDataCurrent = it
            serviceScope.launch {
                fetchSource()
            }
        }

        concatenatingSource.observeForever ({
            println("OBSERVER ACTIVATED")
            serviceScope.launch {
                println(sourceLiveData.value?.size)
                if (sourceLiveData.value?.size!=null){
                    setMediaSource()
                }
            }
        })
    }


    suspend fun fetchSource() = withContext(Dispatchers.IO) {
        println("STARTING FETCH")
        songsMapCurrent.clear()
        songs = sourceDataCurrent.map { song ->
            MediaItem.Builder().setMediaId(song.id.toString()).setUri(song.songPath).setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.songName)
                    .setAlbumTitle(song.album)
                    .setAlbumArtist(song.albumArtist)
                    .setGenre(song.genre)
                    .setTrackNumber(song.trackNumber?.toInt())
                    .setReleaseYear(song.year?.toInt())
                    .setArtist(song.songArtist)
                    .setIsPlayable(true)
                    .build()
            ).build()
        }

        songs.forEachIndexed{index, mediaItem ->
            if(songsMapPrevious.containsKey(mediaItem.mediaId.toInt())){
                songsMapCurrent.put(mediaItem.mediaId.toInt(),
                    MediaItemCombined(mediaItem = mediaItem, prevIndex = songsMapPrevious.get(mediaItem.mediaId.toInt())?.currentIndex, currentIndex = index)
                )
            }else{
                // println("SHOULD PUT SOME KIND OF ITEM HEREEEE")
                songsMapCurrent.put(mediaItem.mediaId.toInt(),
                    MediaItemCombined(mediaItem = mediaItem, prevIndex = null, currentIndex = index)
                )
            }
        }

        if(songsMapPrevious.isNotEmpty()){
            songsMapPrevious.forEach{
                if(!songsMapCurrent.contains(it.key)){
                    indexesToRemove.add(it.key)
                }
            }
        }
        concatenatingSource.postValue(concatenatingMediaSource)
    }

    fun itemIndexById(int: Int?):Int?{
        return songsMapCurrent.get(int)?.currentIndex
    }

    fun itemIndexByIdPrevious(int: Int?):Int?{
        return songsMapPrevious.get(int)?.currentIndex
    }

    private suspend fun setMediaSource() = withContext(Dispatchers.Main){
        if(prevSourceSettings==currentSourceSettings){
            if(songsMapPrevious.keys!=songsMapCurrent.keys || sourceDataPrevious!=sourceDataCurrent){
                indexesToRemove.forEach{songId-> removeIndex(songId)}
                indexesToRemove.clear()
                updatePrevIndexes()
                val sortedMap = songsMapCurrent.toList().sortedBy { (_, value) -> value.currentIndex }.toMap()
                sortedMap.forEach{
                    // println("KEY: ${it.key} || NAME: ${it.value.mediaItem.mediaMetadata.title} || from ${it.value.prevIndex} goes to ${it.value.currentIndex}")
                    val prevIndex = it.value.prevIndex
                    if(prevIndex!=null){
                        exoPlayer.moveMediaItem(prevIndex,it.value.currentIndex)
                        moveItemRangeMap(prevIndex, it.value.currentIndex, sortedMap)
                    }else{
                        exoPlayer.addMediaItem(it.value.mediaItem)
                        shiftIndexMap(it.value.currentIndex,true, sortedMap)
                    }
                }
            }
        }else{
            val seekItem = if(exoPlayer.currentMediaItem!=null){getValidIndexExo()}else{getValidIndex()}
            if (seekItem != null) {
                exoPlayer.setMediaItems(songs, seekItem, 0)
            }else{
                exoPlayer.setMediaItems(songs)
            }
        }


        prevSourceSettings = currentSourceSettings?.copy()
        songsMapPrevious = clone(songsMapCurrent)
        sourceDataPrevious = sourceDataCurrent.map { it.copy() }
        println("AFTER")
    }


    private fun shiftIndexMap(fromIndex: Int, toRight:Boolean, map:Map<Int,MediaItemCombined>){
        map.forEach{
            val prevIndex = it.value.prevIndex
            if(prevIndex!=null){
                if(prevIndex>fromIndex){
                    if(toRight){
                        it.value.prevIndex = it.value.prevIndex!! + 1
                    }else{
                        it.value.prevIndex = it.value.prevIndex!! + -1
                    }
                }
            }
        }
    }

    private fun moveItemRangeMap(fromIndex: Int, toIndex:Int, map:Map<Int,MediaItemCombined>){
        map.forEach{
            val prevIndex = it.value.prevIndex
            if(prevIndex!=null){
                if(fromIndex<toIndex){
                    if(prevIndex>=fromIndex && prevIndex<toIndex) {
                        it.value.prevIndex = it.value.prevIndex!! - 1
                    }
                }else{
                    if(prevIndex<fromIndex && prevIndex>=toIndex) {
                        it.value.prevIndex = it.value.prevIndex!! + 1
                    }
                }
            }
        }
    }


    private fun removeIndex(songId:Int){
        println("$songId removing ID")
        val indexToRemove = itemIndexByIdPrevious(songId)
        if(indexToRemove!=null){
            exoPlayer.removeMediaItem(indexToRemove)
            shiftIndexPrevious(fromIndex = indexToRemove, toRight = false)
        }
    }

    private fun updatePrevIndexes(){
        songsMapPrevious.forEach{
            if(songsMapCurrent.contains(it.key)){
                songsMapCurrent.get(it.key)?.prevIndex = it.value.currentIndex
            }
        }
    }

    private fun shiftIndexPrevious(fromIndex: Int, toRight:Boolean){
        songsMapPrevious.forEach{
            val currIndex = it.value.currentIndex
            if(currIndex>fromIndex){
                if(toRight){
                    it.value.currentIndex = it.value.currentIndex + 1
                }else{
                    it.value.currentIndex = it.value.currentIndex - 1
                }
            }
        }
    }

    private fun getValidIndex():Int?{
        return itemIndexById(preferencesManager.getSongID())
    }

    private fun getValidIndexExo():Int?{
        if(songToPlay!=null){
            return itemIndexById(songToPlay!!.id)!!
        }else{
            return sourceLiveData.value?.indexOfFirst{ it.id.toString() == exoPlayer.currentMediaItem!!.mediaId }
        }
    }
}


fun <K, V> clone(original: Map<K, V>): MutableMap<K, V> {
    val copy: MutableMap<K, V> = HashMap()
    original.forEach { (key, value) -> copy[key] = value }
    return copy
}


