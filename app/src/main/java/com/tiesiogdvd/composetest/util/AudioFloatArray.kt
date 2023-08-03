import android.media.MediaDataSource
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import androidx.core.net.toUri
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun getAudioFloatArray(filePath: String): FloatArray? {
    val extractor = MediaExtractor()
    extractor.setDataSource(filePath)

    // Find and select the audio track
    var trackIndex = -1
    for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime != null) {
            if (mime.startsWith("audio/")) {
                extractor.selectTrack(i)
                trackIndex = i
                break
            }
        }
    }

    // If no audio track found, return null
    if (trackIndex == -1) return null

    // Create a ByteBuffer to hold the audio data
    val buffer = ByteBuffer.allocate(8192) // Increased buffer size

    // Read the audio data into the ByteBuffer
    var sampleSize: Int
    val byteBuffers = mutableListOf<ByteArray>()
    var totalSize = 0

    try {
        while (extractor.readSampleData(buffer, 0).also { sampleSize = it } >= 0) {
            totalSize += sampleSize
            val bytes = ByteArray(sampleSize)
            buffer.get(bytes)  // Get the bytes from the buffer
            byteBuffers.add(bytes)  // Add the bytes to the list of byte arrays
            extractor.advance()
            buffer.clear()  // Clear the buffer for the next read
        }
    }catch (E: java.lang.Exception){

    }




    // Combine all the byte arrays into one
    val audioBytes = ByteArray(totalSize)
    var offset = 0
    for (bytes in byteBuffers) {
        System.arraycopy(bytes, 0, audioBytes, offset, bytes.size)
        offset += bytes.size
    }

    // Convert the byte array to a float array
    val audioFloats = FloatArray(totalSize / 2)
    val shortBuffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
    for (i in 0 until shortBuffer.limit()) {
        audioFloats[i] = shortBuffer.get(i).toFloat() / Short.MAX_VALUE
    }

    return audioFloats
}
