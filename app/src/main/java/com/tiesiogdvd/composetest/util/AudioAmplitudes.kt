package com.tiesiogdvd.composetest.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import java.io.BufferedInputStream
import java.io.InterruptedIOException
import kotlin.math.abs

object AudioAmplitudes {
    private const val tag = "ffmpeg"
    private var ffmpegProcess: Process? = null

    fun getAudioAmplitudes(path: String, ffmpegPath: String?, context: CoroutineScope): List<Int> {

        ffmpegProcess?.destroyForcibly()

        val ffmpegCommand = arrayOf(
            ffmpegPath,
            "-i", path,
            "-vn",
            "-ac", "1",
            "-af", "highpass=f=20,lowpass=f=14000", // Apply high-pass filter with a cutoff frequency of 500 Hz
            "-ar", "1000",
            "-f", "s16le",
            "pipe:1"
        )

        Log.d(tag, ffmpegCommand.joinToString(" "))

        val process = ProcessBuilder(*ffmpegCommand)
            .redirectErrorStream(true)
            .start()

        ffmpegProcess = process

        val inputStream = BufferedInputStream(process.inputStream)
        val amplitudes = mutableListOf<Int>()

        val buffer = ByteArray(2)
        var read: Int
        var sampleCount = 0
        val samplesToSkip = 3000


        try {
            while (inputStream.read(buffer).also { read = it } != -1) {

                if (read == 2) {
                    val amplitude = ((buffer[0].toInt() and 0xFF) or (buffer[1].toInt() shl 8)).toShort()
                    if (sampleCount >= samplesToSkip) {
                        amplitudes.add(amplitude.toInt())
                    }
                    sampleCount++
                }
            }
        }catch (e: Exception){

        }


        val amplitudesPerSecond = mutableListOf<Int>()
        val samplesPerSecond = 1000
        val smoothingFactor = 0.7

        var previousAmplitude = 0

        for (i in amplitudes.indices step samplesPerSecond) {
            val endIndex = minOf(i + samplesPerSecond, amplitudes.size)
            val sublist = amplitudes.subList(i, endIndex)
            val sortedAmplitudes = sublist.map { abs(it) }.sorted()
            val medianAmplitude = if (sortedAmplitudes.size % 2 == 0) {
                (sortedAmplitudes[sortedAmplitudes.size / 2 - 1] + sortedAmplitudes[sortedAmplitudes.size / 2]) / 2
            } else {
                sortedAmplitudes[sortedAmplitudes.size / 2]
            }

            val smoothedAmplitude = (previousAmplitude * (1 - smoothingFactor) + medianAmplitude * smoothingFactor).toInt()
            amplitudesPerSecond.add(smoothedAmplitude)
            previousAmplitude = smoothedAmplitude
        }

        return amplitudesPerSecond
    }
}
