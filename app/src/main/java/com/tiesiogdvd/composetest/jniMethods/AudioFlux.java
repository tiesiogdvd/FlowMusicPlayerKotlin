package com.tiesiogdvd.composetest.jniMethods;

public class AudioFlux {
    static {
       // System.loadLibrary("audioflux"); // Load the library lib_audioflux-0.1.6-android
        System.loadLibrary("_audioflux-0.1.6-android");
        //System.loadLibrary("audiofluxtest");
        System.loadLibrary("log");
        System.loadLibrary("dl");
        System.loadLibrary("c");
        System.loadLibrary("m");
        System.loadLibrary("fftw3f"); // Load the library
    }

    // Declare the native method
    public native float[] generateMelSpectrogram(float[] audioData, int sampleRate, int numMelBins);
}