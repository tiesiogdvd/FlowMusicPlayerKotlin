package com.tiesiogdvd.composetest.ui.ytDownload

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class YtDownloadViewModel @Inject constructor(
    context:Application
):ViewModel() {
    var input = mutableStateOf("")

    fun onButtonPress(){
        println(input.value)
    }

    fun onInputChanged(string: String){
        input.value = string
    }
}