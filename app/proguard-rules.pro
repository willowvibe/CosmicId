# Add project specific ProGuard rules here.

##------------------ Kotlin ------------------##
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlin.Metadata { *; }

##------------------ Kotlin Coroutines ------------------##
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

##------------------ AndroidX / Jetpack ------------------##
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }

##------------------ Hilt / Dagger ------------------##
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

##------------------ Room ------------------##
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *
-keepclassmembers @androidx.room.Dao interface * { *; }

##------------------ WorkManager ------------------##
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

##------------------ AppWidget / Glance ------------------##
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class androidx.glance.** { *; }

##------------------ Google AdMob ------------------##
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

##------------------ App data models ------------------##
-keep class com.willowvibe.agereveal.data.model.** { *; }

##------------------ Miscellaneous ------------------##
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
