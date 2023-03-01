package com.tiesiogdvd.composetest.ui.libraryPlaylists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.data.PlaylistSortOrder
import com.tiesiogdvd.composetest.data.SortOrder
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavbarController
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LibraryPlaylistsViewModel @Inject constructor(
    val musicDao: MusicDao,
    navbarController: NavbarController


) : ViewModel() {


    val searchQuery = MutableStateFlow("")
    val playlistSortOrder = MutableStateFlow(PlaylistSortOrder.BY_DATE_UPDATED)
    val sortOrder = MutableStateFlow(SortOrder.A_Z)

    val isNavbarVisible = navbarController.navbarEnabled
    val isSelectionBarVisible = MutableStateFlow(false)

    val selection = MutableStateFlow(0)
    val selectionListFlow = MutableStateFlow(HashMap<Int, Playlist>())

    val playlistFlow = combine(searchQuery,playlistSortOrder,sortOrder){ query, playlistOrder, sortOrder -> Triple(query, playlistOrder, sortOrder) }
        .flatMapLatest {
                (query, playlistOrder, sortOrder) ->
        musicDao.getPlaylistsWithSongs(query = query, showHidden = false, sortOrder = sortOrder, playlistSortOrder = playlistOrder)
    }

    val fullPlayliststFlow = musicDao.getPlaylistsWithSongs(query = "", showHidden = false, sortOrder = SortOrder.A_Z, playlistSortOrder = PlaylistSortOrder.BY_NAME)


    var bitmap = MutableStateFlow<ImageBitmap?>(null)

    var playlists: List<PlaylistWithSongs>? = null


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
        fullPlayliststFlow.asLiveData().observeForever {
            playlists = it
            bitmap.update {
                MusicDataMetadata.getBitmap(playlists!!.get(0).playlist.bitmapSource)
            }
        }
    }



    fun removePlaylists(){
        val playlistsArrayList:ArrayList<Playlist> = ArrayList()
        for(playlist in selectionListFlow.value){
            playlistsArrayList.add(playlist.value)
        }
        if(playlistsArrayList.size!=0){
            viewModelScope.launch {
                musicDao.removePlaylists(playlistsArrayList)
                selectionListFlow.value.clear()
                selection.update { selectionListFlow.value.size }
            }
        }
    }

    fun hidePlaylists(){
        val playlistsArrayList:ArrayList<Int> = ArrayList()
        for(playlist in selectionListFlow.value){
            playlistsArrayList.add(playlist.value.id)
        }
        if(playlistsArrayList.size!=0){
            viewModelScope.launch {
                musicDao.setArePlaylistsHidden(playlistsArrayList, isHidden = true)
                selectionListFlow.value.clear()
                selection.update { selectionListFlow.value.size }
            }
        }
    }

    fun getSelectedSongs():MutableStateFlow<HashMap<Int,Song>>{
        val songMap: HashMap<Int,Song> = HashMap()
        val songMapMutable = MutableStateFlow(songMap)
        if(playlists!=null){
            for(playlistWithSongs in playlists!!){
                if(selectionListFlow.value.get(playlistWithSongs.playlist.id)!=null){
                    for(song in playlistWithSongs.songs){
                        songMapMutable.value?.put(song.id,song)
                    }
                }
            }
        }
        songMapMutable.update { songMap }
        return songMapMutable
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

    fun updatePlaylistSortOrder(playlistSortOrder: PlaylistSortOrder){
        this.playlistSortOrder.update { playlistSortOrder }
    }

    fun onTextFieldChanged(string:String){
        searchQuery.update { string }
    }


    fun toggleSelection(playlist: Playlist){
        if(selectionListFlow.value.get(playlist.id)!=null){
            selectionListFlow.value.remove(playlist.id)
            selection.update { selectionListFlow.value.size }
        }else{
            selectionListFlow.value.set(playlist.id,playlist)
            selection.update { selectionListFlow.value.size }
        }
    }

    fun toggleSelectAll(){
        val size = playlists?.size
        if(playlists!=null && selection.value!=size){
            for (playlistWithSongs in playlists!!){
                selectionListFlow.value.put(playlistWithSongs.playlist.id, playlistWithSongs.playlist)
            }
        }else{
            selectionListFlow.value.clear()
        }
        selection.update { selectionListFlow.value.size }
    }

}