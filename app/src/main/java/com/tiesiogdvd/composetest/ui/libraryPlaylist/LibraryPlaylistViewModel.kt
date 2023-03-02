package com.tiesiogdvd.composetest.ui.library

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.SongSortOrder
import com.tiesiogdvd.composetest.data.SortOrder
import com.tiesiogdvd.composetest.service.MusicSource
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavbarController
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*

import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskFilter(
    val sourcePlaylistID: Int,
    val searchQuery: String,
    val songSortOrder: SongSortOrder,
    val sortOrder: SortOrder
)

@UnstableApi @HiltViewModel
class LibraryPlaylistViewModel @Inject constructor(
    val musicDao: MusicDao,
    val application: Application,
    val preferencesManager: PreferencesManager,
    private val serviceConnector: ServiceConnector,
    var musicSource: MusicSource,
    val navbarController: NavbarController
): ViewModel(){
    var source = MutableStateFlow(-1)
    val searchQuery = MutableStateFlow("")
    val songSortOrder = MutableStateFlow(SongSortOrder.BY_NAME)
    val sortOrder = MutableStateFlow(SortOrder.A_Z)

    val isNavbarVisible = navbarController.navbarEnabled
    val isSelectionBarVisible = MutableStateFlow(false)

    val selection = MutableStateFlow(0)

    val selectionListFlow = MutableStateFlow(HashMap<Int,Song>())

    val playlistFlow = combine(source,searchQuery,songSortOrder,sortOrder){
            playlistId,query, sortOrderParameters, sortOrder -> TaskFilter(playlistId,query, sortOrderParameters, sortOrder)
    }.flatMapLatest {
            (playlistId, query, sortOrderParameters, sortOrder) ->
        if(source.value!=-1){
            musicDao.getPlaylistSongs(playlistId,query,sortOrderParameters,sortOrder)
        }else{
            emptyFlow()
        }
    }

    val fullPlaylistFlow = source.flatMapLatest {
        musicDao.getPlaylistSongs(source.value,"", songSortOrder = songSortOrder.value, showHidden = false, sortOrder = sortOrder.value)
    }

    val playlist = source.flatMapLatest {
        if(it!=-1){
            musicDao.getPlaylist(it)
        }else{
            emptyFlow()
        }
    }

    var bitmap = MutableStateFlow<ImageBitmap?>(null)

    var songs: List<Song>? = null


    var isSortDialogShown by mutableStateOf(false)
        private set
    fun openSortDialog(){
        isSortDialogShown = true
    }
    fun dismissSortDialog(){
        isSortDialogShown = false
    }


    var isPlaylistsDialogShown by mutableStateOf(false)
        private set

    fun openPlaylistsDialog(){
        isPlaylistsDialogShown = true
    }
    fun dismissPlaylistsDialog(){
        isPlaylistsDialogShown = false
    }


    init {
        fullPlaylistFlow.asLiveData().observeForever {
            songs = it
        }

        playlist.asLiveData().observeForever({
            val bm = it?.bitmapSource
            bitmap.update {
                MusicDataMetadata.getBitmap(bm)
            }
        })
    }

    fun removeSongs(){
        val songsArrayList:ArrayList<Song> = ArrayList()
        for(song in selectionListFlow.value){
            songsArrayList.add(song.value)
        }
        if(songsArrayList.size!=0){
            viewModelScope.launch {
                musicDao.removeSongs(songsArrayList)
                selectionListFlow.value.clear()
                selection.update { selectionListFlow.value.size }
            }
        }
    }

    fun hideSongs(){
        val songsArrayList:ArrayList<Song> = ArrayList()
        for(song in selectionListFlow.value){
            songsArrayList.add(song.value)
        }
        if(songsArrayList.size!=0){
            viewModelScope.launch {
                musicDao.setAreSongsHidden(songsArrayList, isHidden = true)
                selectionListFlow.value.clear()
                selection.update { selectionListFlow.value.size }
            }
        }
    }


    fun setPlaylistCover(){
        viewModelScope.launch {
            if(selection.value==1){
                val song = selectionListFlow.value.values.first()
                musicDao.setPlaylistBitmapSource(song.playlistId, song.songPath)
            }
        }
    }

    fun isSelectionBarSelected(boolean: Boolean){
        if(isSelectionBarVisible.value!=boolean){
            isNavbarVisible.update { !boolean }
            isSelectionBarVisible.update { boolean }
            if(!boolean){
                selectionListFlow.value.clear()
            }
        }
    }



    fun updateSortOrder(sortOrder: SortOrder){
        this.sortOrder.update { sortOrder }
    }
    fun updateSongSortOrder(songSortOrder: SongSortOrder){
        this.songSortOrder.update { songSortOrder }
    }

    fun onTextFieldChanged(string:String){
        searchQuery.update { string }
    }
    fun setSource(playlistId: Int){
        if(playlistId!=source.value || playlistId!=-1){
            println("SOURCE SET $playlistId")
            source.update { playlistId }
        }
    }

    fun toggleSelection(song:Song){
        if(selectionListFlow.value.get(song.id)!=null){
            selectionListFlow.value.remove(song.id)
            selection.update { selectionListFlow.value.size }
        }else{
            selectionListFlow.value.set(song.id,song)
            selection.update { selectionListFlow.value.size }
        }
    }

    suspend fun getPlaylistBitmap(playlist: Playlist): ImageBitmap?{
        var bitmap: ImageBitmap? = null
        if (playlist.bitmapSource!=null){
            if(musicDao.getSong(playlistId = playlist.id, songPath = playlist.bitmapSource)!=null){
                bitmap = MusicDataMetadata.getBitmap(playlist.bitmapSource)
            }else{
                musicDao.setPlaylistBitmapSource(playlist.id,null)
            }
        }
        return bitmap
    }

    fun onSongSelected(song: Song) {
        viewModelScope.launch {
            if (preferencesManager.getCurrentPlaylistID() != source.value || preferencesManager.getCurrentSongSortMode()!= songSortOrder.value || preferencesManager.getCurrentSortMode()!= sortOrder.value) {
                preferencesManager.updateSource(song.playlistId, sortOrder.value, songSortOrder.value)
                println("SOURCE CHANGEEEEE")
            }
            musicSource.songToPlay = song
            musicSource.itemIndexById(song.id).let {
                if (it != null && it != -1) { serviceConnector.controller?.seekTo(it, 0) }
                serviceConnector.controller?.playWhenReady = true
            }
        }
    }

    fun toggleSelectAll(){
        val size = songs?.size
        if(songs!=null && selection.value!=size){
            for (song in songs!!){
                selectionListFlow.value.put(song.id, song)
            }
        }else{
            selectionListFlow.value.clear()
        }
        selection.update { selectionListFlow.value.size }
    }


}
