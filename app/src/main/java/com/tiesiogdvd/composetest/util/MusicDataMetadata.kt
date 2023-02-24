package com.tiesiogdvd.composetest.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toFile
import com.tiesiogdvd.composetest.util.TypeConverter.formatDuration
import java.io.IOException

//This class implements storing music data and creates methods to retrieve Metadata from it
class MusicDataMetadata {
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var bitmap: Bitmap? = null
    var length = 0
    var lengthString: String? = null
    var mr: MediaMetadataRetriever

    init {
        mr = MediaMetadataRetriever()
    }

    fun setAllData(path: String) {
        mr.setDataSource(path)
        try {
            title = mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            if (title == null) {
                throw Exception()
            }
        } catch (e: Exception) {
            title = path.substring(path.lastIndexOf("/")).replace("/", "")
        }
        try {
            artist = mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        } catch (e: Exception) {
        }
        try {
            album = mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        } catch (e: Exception) {
        }
        try {
            val data = mr.embeddedPicture
            bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        } catch (e: Exception) {
        }
        try {
            length = mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt()
            lengthString = formatDuration(length.toLong())
        } catch (e: Exception) {
        }
        try {
            mr.release()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        fun getBitmap(path: String?): ImageBitmap? {
            val mr = MediaMetadataRetriever()
            try {
                mr.setDataSource(path)
                val data = mr.embeddedPicture
                val bitmap = data?.let { BitmapFactory.decodeByteArray(data, 0, it.size) }
                mr.release()
                return bitmap?.asImageBitmap()
            } catch (e: Exception) {
                try {
                    mr.release()
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }
                return null
            }
        }
    }

        fun getBitmap(uri: Uri?): Bitmap? {
            val mr = MediaMetadataRetriever()

            return try {
                val file = uri?.toFile()
                mr.setDataSource(file?.path)
                val data = mr.embeddedPicture
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
                mr.release()
                bitmap
            } catch (e: Exception) {
                try {
                    mr.release()
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }
                null
            }
        }

        fun getBitmapByteArray(path: String?): ByteArray? {
            val mr = MediaMetadataRetriever()

            return try {
                mr.setDataSource(path)
                return mr.embeddedPicture

            } catch (e: Exception) {
                try {
                    mr.release()
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }
                null
            }
        }
    }
