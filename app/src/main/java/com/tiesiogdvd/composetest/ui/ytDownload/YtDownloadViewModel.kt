package com.tiesiogdvd.composetest.ui.ytDownload

import android.app.Application
import android.os.Environment
import android.os.Looper
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavbarController
import com.tiesiogdvd.composetest.ui.error.ErrorType
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class DownloadableItem(
    val name: String,
    val index: Int = 0,
    var playlist: String? = "",
    var imageSource: MutableStateFlow<String?> = MutableStateFlow(null),
    var downloadState: MutableStateFlow<DownloadState> = MutableStateFlow(DownloadState.SELECTION),
    var isSelected: MutableStateFlow<Boolean> =  MutableStateFlow(false)
)

enum class DownloadState(state: String, value: Int){
    SELECTION ("", 0),
    PREPARING ("Preparing", 1),
    DOWNLOADING ("Downloading", 2),
    PROCESSING("Processing", 3),
    FINISHED("Download finished", 4),
    ERROR("Error", 404)
}

@HiltViewModel
class YtDownloadViewModel @Inject constructor(
    private var context:Application,
    navbarController: NavbarController,
    val musicDao: MusicDao
):ViewModel() {
    var input = mutableStateOf("")
    var itemList = mutableStateMapOf<Int, DownloadableItem>()
    val itemListFlow = MutableStateFlow(itemList)

    val isNavbarVisible = navbarController.navbarEnabled
    val isSelectionBarVisible = MutableStateFlow(false)

    val error = MutableStateFlow(ErrorType.NO_ERROR)

    val selection = MutableStateFlow(0)

    val loading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(context))
            }
        }
    }

    fun loadSongsFromLink(){
        println(input.value)
        toggleLoading(true)
        if(input.value.isNotEmpty()){
            input.value = input.value.replace("\\s".toRegex(), "")
            viewModelScope.launch {
                getSongInfo(input.value)
            }
        }
    }

    suspend fun getSongInfo(url: String) = withContext(Dispatchers.IO) {

        val instance = Python.getInstance()
        val youtubeDLModule = instance.getModule("YoutubeVideoDownloader")
        youtubeDLModule.callAttr("getInfo", url, ::itemsReceivedCallback)
    }

    fun itemReceived(key: Int, name: String, playlist: String?, thumbnail: String){  //key and song name
        //item received after entering link
        if(loading.value==true){
            toggleLoading(false)
        }
        if(!isSelectionBarVisible.value){
            toggleSelectionBar(true)
        }
        itemList.put(key, DownloadableItem(name = name, imageSource= MutableStateFlow(thumbnail), playlist = playlist))
        itemListFlow.update { itemList }
    }

    fun itemsReceivedCallback(info: PyObject){  //key and song name
        //items received after entering link

            /*info object -> {
               'title',                     playlist title
               'entries' ->{                playlist songs list  || does not exist if link is a single video
                    entry -> {              single song
                        title,              song title
                        description,
                        channel...
                        thumbnails ->{      multiple res thumbnails list
                            thumbnail ->{
                                height,
                                width
                                url         thumbnail link
                            }
                        }
                    }
               }
            }

            Run printListableKeysRecursive(info) in python file to see full structure of info

            The main values of the PyObject are obtained like this:
                For a single video:
                    title:      info['title'] (probably can't be null)

                    thumbnails: info['thumbnails'] (last one is highest quality, can be null)
                    thumbnails are dictionaries too
                    to get best thumbnail: info['thumbnails'][last]['url']

                For a playlist:
                    playlist title:   info['title']
                    title of video i: info['entries'][i]['title']

                    title is [Deleted video] if video is deleted
                    title is [Private video] if video is private

                    best thumbnail of video i: info['entries'][i]['thumbnails'][last]['url']
        */

        val infoMap = info.asMap()

        if(infoMap[PyObject.fromJava("entries")] != null) {
            // info is a playlist
            val playlistTitle = infoMap[PyObject.fromJava("title")].toString()
            val entries = infoMap[PyObject.fromJava("entries")]?.asList() ?: return
            for (index in  0..entries.size-1){
                val entry = entries[index].asMap()
                addVideoToList(index, playlistTitle, entry)
                //Thread.sleep(1) // prevent freeze
            }
        } else {
            // info is a single video
            addVideoToList(0, "", infoMap)
        }

        if(loading.value){
            toggleLoading(false)
        }
        if(!isSelectionBarVisible.value){
            toggleSelectionBar(true)
        }

        viewModelScope.launch {
            updateItemListFlow()
        }

    }

    suspend fun updateItemListFlow() = withContext(Dispatchers.Main){
        if(Looper.myLooper() == Looper.getMainLooper()) {
            println("MAIN THREAD")
        }else{
            println("NOT MAIN THREAD")
        }
        itemListFlow.update { itemList }
    }

    fun addVideoToList(key: Int, playlist: String, entry: Map<PyObject?, PyObject?>) {
        val title = entry[PyObject.fromJava("title")].toString()
        if(title == "[Deleted video]" || title == "[Private video]") { return }

        var thumbnailUrl = ""
        val thumbnails = entry[PyObject.fromJava("thumbnails")]?.asList()

        if(thumbnails != null) {
            val thumbnailMap = thumbnails[thumbnails.size-1].asMap()
            thumbnailUrl = thumbnailMap[PyObject.fromJava("url")].toString()
        }

        viewModelScope.launch {
            itemList.put(key, DownloadableItem(name = title, imageSource= MutableStateFlow(thumbnailUrl), playlist = playlist))
        }
    }

    fun toggleSelection(key:Int){
        println(Environment.getExternalStorageDirectory().absolutePath)
        itemListFlow.getAndUpdate {
            if(it.get(key)?.isSelected?.value==true){
                it.get(key)?.isSelected?.value=false
                selection.value--
            }else{
                it.get(key)?.isSelected?.value=true
                selection.value++
            }
            it
        }
        itemListFlow.value = itemList
    }

    fun toggleSelectionBar(boolean: Boolean){

        if(isSelectionBarVisible.value!=boolean){
            isNavbarVisible.update { !boolean }
            isSelectionBarVisible.update { boolean }
        }
    }

    fun toggleLoading(boolean: Boolean){
        loading.update { boolean }
    }

    fun toggleSelectAll(){
        if(selection.value!=itemListFlow.value.size){
            selection.value=0
            for (item in itemListFlow.value){
                item.value.isSelected.value=true
                selection.value++
            }
        }else{
            selection.value=0
            for (item in itemListFlow.value){
                item.value.isSelected.value=false
            }
        }
    }

    fun onInputChanged(string: String){
        input.value = string
    }

    fun onDownloadSelected(){
        val songList = ArrayList<Int>()
        for(item in itemListFlow.value){
            if(item.value.isSelected.value==true){
                item.value.downloadState.value=DownloadState.PREPARING
                songList.add(item.key)
            }else{
                itemListFlow.value.remove(item.key)
            }
        }
        toggleSelectionBar(false)
        //pass songList to python
        viewModelScope.launch {
            startDownload(songList)
        }
    }

    private suspend fun startDownload(songList: ArrayList<Int>) = withContext(Dispatchers.IO){

    }

    fun errorCallback(errorType:Int){
        when(errorType){
            0 -> error.update { ErrorType.NO_ERROR }
            1 -> error.update { ErrorType.LINK_NOT_FOUND }
            2 -> error.update { ErrorType.EMPTY_PLAYLIST }
            else -> error.update { ErrorType.NO_ERROR }
        }
    }

    fun updateProgressCallback(progress: HashMap<Int,Int>){ //key and DownloadState int value
        for(item in progress){
            val state = when (item.value){
                1 -> DownloadState.PREPARING
                2 -> DownloadState.DOWNLOADING
                else -> DownloadState.ERROR
            }
            itemListFlow.value.get(item.key)?.downloadState?.value = state
        }
    }

    fun onDownloadedCallback(key:Int, filePath:String){ //key and file path
        itemListFlow.value.get(key)?.downloadState?.value = DownloadState.PROCESSING
        if(!File(Environment.getExternalStorageDirectory().absolutePath+"/Music/FlowMusic").exists()){
            File(Environment.getExternalStorageDirectory().absolutePath+"/Music/FlowMusic").mkdirs()
        }
        val file = File(filePath)
        val directory = File(Environment.getExternalStorageDirectory().absolutePath+"/Music/FlowMusic")

        if(file.exists()){
            val songFile = file.copyTo(directory)
            insertSongToDatabase(songFile)
        }
    }


    fun insertSongToDatabase(file:File){
        val musicData = MusicDataMetadata()
        musicData.setAllData(file.absolutePath)
        viewModelScope.launch {
            if(!musicDao.playlistExists("Youtube")){
                musicDao.insertPlaylist(Playlist("Youtube"))
            }
            musicDao.insertSong(Song(
                songName = musicData.title,
                songPath = file.absolutePath,
                playlistId = musicDao.getPlaylist("Youtube").id,
                year = musicData.year,
                trackNumber = musicData.trackNumber,
                genre = musicData.genre,
                album = musicData.album,
                songArtist = musicData.artist,
                albumArtist = musicData.albumArtist,
                length = musicData.length.toLong()
            ))
        }
    }
}