package com.tiesiogdvd.composetest.ui.ytDownload

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavbarController
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DownloadableItem(
    val name:String,
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

    val selection = MutableStateFlow(0)

    fun onButtonPress(){
        println(input.value)
        viewModelScope.launch {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(context,))
                DownloadState.DOWNLOADING
            }

            val instance = Python.getInstance()
            val helloModule = instance.getModule("test")
            println(context.applicationInfo.nativeLibraryDir)

            val file = File(context.applicationInfo.nativeLibraryDir)
            val file2 = context.applicationInfo.nativeLibraryDir + "/ffmpeg2.so"

            file.list()?.forEach {
                println(it)
            }
            helloModule.put("progress_callback", ::updateProgressCallback)
            helloModule.callAttr("my_function", input.value, ::updateProgressCallback, file2)

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


    fun itemsReceivedCallback(itemsMap: HashMap<Int,String>){  //key and song name
        //items received after entering link
        for(song in itemsMap){
            itemList.put(song.key, DownloadableItem(song.value))
            itemListFlow.update { itemList }
            toggleSelectionBar(true)
        }
    }

    fun onDownloadSelected(){
        val songList = ArrayList<Int>()
        for(item in itemListFlow.value){
            if(item.value.isSelected.value==true){
                songList.add(item.key)
            }else{
                itemListFlow.value.remove(item.key)
            }
        }
        toggleSelectionBar(false)
        //pass songList to python
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

    fun addDummyItems() {
        toggleSelectionBar(true)
        itemList.put(0,DownloadableItem("name1"))
        itemList.put(1,DownloadableItem("name2"))
        itemList.put(2,DownloadableItem("name3"))
        itemList.put(3,DownloadableItem("name4"))
        itemList.put(4,DownloadableItem("name5"))
        itemList.put(5,DownloadableItem("name6"))
        itemList.put(6,DownloadableItem("name7"))
        itemList.put(7,DownloadableItem("name8"))
        itemList.put(8,DownloadableItem("name9"))
        itemList.put(9,DownloadableItem("name10"))
        itemList.put(10,DownloadableItem("name11"))
        itemList.put(11,DownloadableItem("name12"))

        itemListFlow.update { itemList }
    }
}