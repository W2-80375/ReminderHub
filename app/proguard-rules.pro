# Hilt / Dagger
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends java.lang.annotation.Annotation
-keep interface dagger.hilt.android.internal.lifecycle.HiltViewModelMap { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit / OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.own.remindme.data.remote.ai.** { *; }

# Keep Data Models (Important for JSON Parsing)
-keep class com.own.remindme.domain.model.** { *; }
-keep class com.own.remindme.data.local.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# Compose
-keep class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.ui.platform.**

# WorkManager
-keep class androidx.work.Worker { *; }
-keep class androidx.work.CoroutineWorker { *; }
-keep class com.own.remindme.utils.workers.** { *; }
