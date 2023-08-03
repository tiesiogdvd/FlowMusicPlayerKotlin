#include "com_tiesiogdvd_composetest_jniMethods_AudioFlux.h"
#include <vector>

JNIEXPORT jfloatArray JNICALL Java_com_tiesiogdvd_composetest_jniMethods_AudioFlux_generateMelSpectrogram
  (JNIEnv *env, jobject obj, jfloatArray audio_data, jint sample_rate, jint num_mel_bins) {
    // Get the length of the audio data
    jsize audio_data_length = env->GetArrayLength(audio_data);

    // Get a pointer to the audio data
    jfloat* audio_data_ptr = env->GetFloatArrayElements(audio_data, NULL);

    // Convert the audio data to a std::vector
    std::vector<float> audio_data_vector(audio_data_ptr, audio_data_ptr + audio_data_length);

    // Call the AudioFlux mel_spectrogram function
    std::vector<float> mel_spectrogram = audioflux::mel_spectrogram(
        audio_data_vector, num_mel_bins, 12, sample_rate, 0.0, sample_rate / 2.0
    );

    // Create a new Java float array for the result
    jfloatArray result = env->NewFloatArray(mel_spectrogram.size());

    // Convert the std::vector to a float array
    float* mel_spectrogram_array = &mel_spectrogram[0];

    // Set the result array
    env->SetFloatArrayRegion(result, 0, mel_spectrogram.size(), mel_spectrogram_array);

    // Release the audio data array
    env->ReleaseFloatArrayElements(audio_data, audio_data_ptr, 0);

    // Return the result
    return result;
}