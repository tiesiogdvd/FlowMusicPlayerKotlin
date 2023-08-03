package com.tiesiogdvd.composetest.ui.libraryPlaylist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Float.max
import java.lang.StrictMath.ceil
import java.lang.StrictMath.min
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Supplier

object BitmapLoader {
    private val executor = Executors.newFixedThreadPool(2)

    fun loadBitmapAsync(scope: CoroutineScope, path: String): Deferred<ImageBitmap?> {
        return scope.async(executor.asCoroutineDispatcher()) {
            loadBitmap(path)
        }
    }

    private fun loadBitmap(path: String): ImageBitmap? {
        val mr = MediaMetadataRetriever()
        return try {
            mr.setDataSource(path,)
            val data = mr.embeddedPicture
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
            }

            BitmapFactory.decodeByteArray(data, 0, data?.size ?: 0, options).asImageBitmap()
        } catch (e: Exception) {
            try {
                mr.release()
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadBitmap2(path: String, maxWidth: Int, maxHeight: Int): ImageBitmap? {
        val mr = MediaMetadataRetriever()
        try {
            mr.primaryImage
            return mr.primaryImage?.asImageBitmap()
        }catch(e:java.lang.Exception){
            return null
        }

    }

    private fun loadBitmap3(path: String, maxWidth: Int, maxHeight: Int, ffmpegPath: String?): ImageBitmap? {
        val ffmpegCommand = arrayOf(
            ffmpegPath, // Path to ffmpeg executable
            "-i", path, // Input file path
            "-an", // Disable audio
            "-vframes", "1", // Extract one frame
            "-s", "${maxWidth}x$maxHeight", // Scale to size
            "-f", "image2pipe", // Output format
            "-vcodec", "bmp", // Output codec
            "pipe:1" // Output to stdout
        )

        val process = ProcessBuilder(*ffmpegCommand)
            .redirectErrorStream(true)
            .start()

        val inputStream = BufferedInputStream(process.inputStream)
        val outputStream = ByteArrayOutputStream()

        val buffer = ByteArray(8192)
        var read: Int

        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }

        val bytes = outputStream.toByteArray()

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }


    private fun calculateScaleFactor(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
        val (imageWidth, imageHeight) = options.run { outWidth to outHeight }
        var scaleFactor = 1
        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            val widthRatio = imageWidth.toFloat() / maxWidth.toFloat()
            val heightRatio = imageHeight.toFloat() / maxHeight.toFloat()
            scaleFactor = ceil(max(widthRatio, heightRatio).toDouble()).toInt()
        }
        return scaleFactor
    }
}