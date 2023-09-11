package com.tiesiogdvd.composetest.ui.ytDownload

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.Keep
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavRoutes
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavbarController
import com.tiesiogdvd.composetest.ui.error.ErrorType
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject

@Keep
data class DownloadableItem(
    val name: String,
    val index: Int = 0,
    var playlist: String? = "",
    var progress: MutableStateFlow<Float?> = MutableStateFlow(null),
    var imageSource: MutableStateFlow<String?> = MutableStateFlow(null),
    var downloadState: MutableStateFlow<DownloadState> = MutableStateFlow(DownloadState.SELECTION),
    var isSelected: MutableStateFlow<Boolean> =  MutableStateFlow(false)
)

@Keep
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
    val currentNavItem = MutableLiveData<String>()
    var input = mutableStateOf("")
    var itemList = mutableStateMapOf<String, DownloadableItem>()
    val itemListFlow = MutableStateFlow(itemList)

    val isNavbarVisible = navbarController.navbarEnabled
    val isSelectionBarVisible = MutableStateFlow(false)

    val error = MutableStateFlow(ErrorType.NO_ERROR)
    val selection = MutableStateFlow(0)

    val loading = MutableStateFlow(false)

    lateinit var instance:Python
    lateinit var youtubeDLModule:PyObject

    val ffmpegPath = File(context.applicationInfo.nativeLibraryDir+"/lib_ffmpeg.so").absolutePath

    private val pythonInit: Deferred<PyObject> by lazy {
        viewModelScope.async(Dispatchers.Default) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
            }
            instance = Python.getInstance()
            youtubeDLModule = instance.getModule("YoutubeVideoDownloader")
            youtubeDLModule.put("progressCallback", ::updateProgressCallback)
            youtubeDLModule.put("videoDownloadedCallback", ::onDownloadProcessedCallback)
            youtubeDLModule.put("itemErrorCallback", ::itemErrorCallback)
            youtubeDLModule.put("ffmpegPath", ffmpegPath)
        }
    }

    init {
        pythonInit.start()
    }

    fun loadSongsFromLink(){
        println(input.value)
        if(input.value.isNotEmpty() and input.value.isNotBlank()){
            toggleLoading(true)
            input.value = input.value.replace("\\s".toRegex(), "")

            viewModelScope.launch {
                pythonInit.await()
                getSongInfo(input.value)
            }
        }
    }

    suspend fun getSongInfo(url: String) = withContext(Dispatchers.IO) {
       // val instance = Python.getInstance()
       // val youtubeDLModule = instance.getModule("YoutubeVideoDownloader")
        itemListFlow.value.clear()
        selection.value=0
        /*val callback = object : Function1<PyObject, Unit> {
            override fun invoke(info: PyObject) {
                itemsReceivedCallback(info)
         D   }
        }
        val pythonCallback = callback as Function1<PyObject, Unit>*/


        //youtubeDLModule.callAttr("getInfo", url, pythonCallback)
        youtubeDLModule.callAttr("getInfo", url, ::itemsReceivedCallback)
    }

    fun itemReceived(key: String, name: String, playlist: String?, thumbnail: String){  //key and song name
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


    @Keep
    fun itemsReceivedCallback(info: PyObject?){  //key and song name
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

        if(info!=null){
            val infoMap = info.asMap()
            var entriesNo:Int = 0

            if(infoMap[PyObject.fromJava("entries")] != null) {
                // info is a playlist
                val playlistTitle = infoMap[PyObject.fromJava("title")].toString()
                val entries = infoMap[PyObject.fromJava("entries")]?.asList() ?: return
                entriesNo = entries.size

                for (index in  0..entries.size-1){
                    val entry = entries[index].asMap()
                    val videoId = entry[PyObject.fromJava("id")]?.toString()

                    println(videoId)
                    addVideoToList(videoId.toString(), playlistTitle, entry, index)
                    //Thread.sleep(1) // prevent freeze
                }
            } else {
                // info is a single video
                val videoId = infoMap[PyObject.fromJava("id")]?.toString()
                addVideoToList(videoId.toString(), "Youtube", infoMap, 0)

                entriesNo = 1
            }
            if(entriesNo!=0){
                if(!isSelectionBarVisible.value){
                    toggleSelectionBar(true)
                }
            }else{
                error.value=ErrorType.EMPTY_PLAYLIST
            }

            viewModelScope.launch {
                updateItemListFlow()
            }
        }else{
            error.value=ErrorType.LINK_NOT_FOUND
        }
        if(loading.value){
            toggleLoading(false)
        }
    }

    suspend fun updateItemListFlow() = withContext(Dispatchers.Main){
        itemListFlow.update { itemList }
    }

    fun addVideoToList(key: String, playlist: String, entry: Map<PyObject?, PyObject?>, index: Int) {
        val title = entry[PyObject.fromJava("title")].toString()
        if(title == "[Deleted video]" || title == "[Private video]") { return }

        var thumbnailUrl = ""
        val thumbnails = entry[PyObject.fromJava("thumbnails")]?.asList()

        if(thumbnails != null) {
            val thumbnailMap = thumbnails[thumbnails.size-1].asMap()
            thumbnailUrl = thumbnailMap[PyObject.fromJava("url")].toString()
        }

        viewModelScope.launch {
            itemList.put(key, DownloadableItem(name = title, imageSource= MutableStateFlow(thumbnailUrl), playlist = playlist, index = index))
        }
    }

    fun toggleSelection(key:String){
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
        if(currentNavItem.value==NavRoutes.YT_DOWNLOAD.name){
            if(isSelectionBarVisible.value!=boolean){
                isNavbarVisible.update { !boolean }
                isSelectionBarVisible.update { boolean }
            }
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
        val songList = ArrayList<String>()
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

    private suspend fun startDownload(songList: ArrayList<String>) = withContext(Dispatchers.IO){
        val ffmpeg = File(context.applicationInfo.nativeLibraryDir)
        ffmpeg.listFiles()?.forEach {
            println(it.absolutePath)

        }
        viewModelScope.launch(Dispatchers.IO) {
            for(id in songList){
                youtubeDLModule.callAttr("downloadVideo", "https://www.youtube.com/watch?v=$id")
            }
        }
    }

    @Keep
    fun itemErrorCallback(string: String){
        val key = string.substringAfterLast("=")
        itemListFlow.value.get(key)?.downloadState?.value = DownloadState.ERROR
    }

    @Keep
    fun updateProgressCallback(id:String, progress: Float){ //key and DownloadState int value
        println("$id has progress of $progress")
            itemListFlow.value.get(id)?.progress?.value = progress
            itemListFlow.value.get(id)?.downloadState?.value = DownloadState.DOWNLOADING
    }

    @Keep
    fun onDownloadProcessedCallback(key:String, filePath:String){ //key and file path
        itemListFlow.value.get(key)?.downloadState?.value = DownloadState.PROCESSING
        if(!File(Environment.getExternalStorageDirectory().absolutePath+"/Music/FlowMusic").exists()){
            File(Environment.getExternalStorageDirectory().absolutePath+"/Music/FlowMusic").mkdirs()
        }

        val file = File(filePath)

        println(file.name + " FILE NAME")
        val targetPath = File(Environment.getExternalStorageDirectory().absolutePath+"/Music/FlowMusic/"+file.name)

        if(file.exists()){
            val songFile = file.copyTo(targetPath)
            insertSongToDatabase(songFile, key)
            file.delete()
        }
    }


    fun insertSongToDatabase(file:File, key:String){
        viewModelScope.launch(Dispatchers.IO) {
            val musicData = MusicDataMetadata()
            musicData.setAllData(file.absolutePath)
            val playlistName = itemListFlow.value.get(key)?.playlist ?: "Youtube"

            if(!musicDao.playlistExists(playlistName)){
                musicDao.insertPlaylist(Playlist(playlistName))
            }
            val playlist = musicDao.getPlaylist(playlistName)
            musicDao.insertSongToPlaylist(song=Song(
                songName = musicData.title,
                songPath = file.absolutePath,
                year = musicData.year,
                trackNumber = musicData.trackNumber,
                genre = musicData.genre,
                album = musicData.album,
                songArtist = musicData.artist,
                albumArtist = musicData.albumArtist,
                length = musicData.length.toLong()
            ), playlistId = playlist.id)
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _, _ -> }
            itemListFlow.value.get(key)?.downloadState?.value = DownloadState.FINISHED
        }
    }

}