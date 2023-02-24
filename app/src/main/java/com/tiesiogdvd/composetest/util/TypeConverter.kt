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
        val hours = (TimeUnit.HOURS.convert(duration, TimeUnit.MILLISECONDS)).toInt()
        val minutes = (TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
                - TimeUnit.HOURS.toMinutes(TimeUnit.HOURS.convert(duration, TimeUnit.MILLISECONDS))).toInt()
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS))).toInt()
        val sb = StringBuilder()
        if (hours > 0) {
            sb.append(String.format("%02d:", hours))
        }
        sb.append(String.format("%02d:%02d", minutes, seconds))
        return sb.toString()
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