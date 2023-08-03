# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.chaquo.python.** { *; }
-dontwarn com.chaquo.python.**


-keepclassmembers class * implements com.chaquo.python.PyObject {
    *;
}

-keep class hilt_aggregated_deps._com_tiesiogdvd_composetest_ui_ytDownload_YtDownloadViewModel_HiltModules_BindsModule

-keep class hilt_aggregated_deps._com_tiesiogdvd_composetest_ui_ytDownload_YtDownloadViewModel_HiltModules_KeyModule

-keep class com.tiesiogdvd.composetest.ui.ytDownload.** {*;}

-keep class com.tiesiogdvd.composetest.api.*

-dontnote okhttp3.**, okio.**, retrofit2.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class org.apache.commons.text.similarity.JaroWinklerDistance {*;}
-keep class org.apache.commons.text.** {*;}

# Keep all classes, interfaces and their members
#-keep class * { *; }

# Keep all Kotlin classes, interfaces and their members
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Keep all classes that have native methods
-keepclasseswithmembers class * {
    native <methods>;
}


-keepattributes Exceptions, Signature, InnerClasses, *Annotation*, EnclosingMethod
# Kotlin coroutines
-keep class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keep class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Kotlin coroutines and scheduling
-keepattributes Exceptions, Signature, InnerClasses, *Annotation*, EnclosingMethod
-keep class kotlinx.coroutines.** { *; }
