package com.tiesiogdvd.composetest.util

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

//Static class to be used for converting dates, paths and etc.
object TypeConverter {

    fun convertPath(filePath: String): String {
        var fileName = filePath.substring(filePath.lastIndexOf("/")).replace("/", "")
        fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".png"
        return fileName
    }

    fun removeExtras(filePath: String): String {
        var fileName = filePath.substring(filePath.lastIndexOf("/")).replace("/", "")
        fileName = fileName.substring(0, fileName.lastIndexOf("."))
        return fileName
    }

    fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MINUTES.convert(duration.toLong(), TimeUnit.MILLISECONDS).toInt()
        val seconds = (TimeUnit.SECONDS.convert(duration.toLong(), TimeUnit.MILLISECONDS)
                - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getFilePath(file: String?): String {
        return File(file).parent
    }

    val dateString: String
        get() {
            val date = Date()
            val dateFormat = "dd/MM/Y hh:mm:ss a"
            val sdf = SimpleDateFormat(dateFormat)
            return sdf.format(date)
        }
}