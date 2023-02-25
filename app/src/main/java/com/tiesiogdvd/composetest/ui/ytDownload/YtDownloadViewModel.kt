package com.tiesiogdvd.composetest.ui.ytDownload

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class YtDownloadViewModel @Inject constructor(
    private var context:Application
):ViewModel() {
    var input = mutableStateOf("")

    fun onButtonPress(){
        println(input.value)
        viewModelScope.launch {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(context,))
            }

            val instance = Python.getInstance()
            val helloModule = instance.getModule("test")
            println(context.applicationInfo.nativeLibraryDir)
            val file = File(context.applicationInfo.nativeLibraryDir)

            file.list()?.forEach {
                println(it)
            }
            helloModule.put("progress_callback", ::onCallback)
            helloModule.callAttr("my_function", input.value, ::onCallback)
        }


    }

    fun onInputChanged(string: String){
        input.value = string
    }

    suspend fun haha() = withContext(Dispatchers.IO){

    }

    fun onCallback(string: String){
        println(string)
    }
}