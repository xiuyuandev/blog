# ProGuard 规则 - 素时 Sushi

# 保留行号信息供崩溃报告使用
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# 协程
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.flow.** { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# DataStore
-keep class androidx.datastore.preferences.protobuf.** { *; }
-dontwarn androidx.datastore.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson - 保留序列化字段名
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.sushi.app.data.json.** { *; }

# 应用数据类
-keep class com.sushi.app.data.entity.** { *; }
-keep class com.sushi.app.data.model.** { *; }
-keep class com.sushi.app.sync.** { *; }

# 反射使用的 ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# Hilt/SushiContainer
-keep class com.sushi.app.SushiContainer { *; }
-keep class com.sushi.app.SushiContainer$* { *; }
-keep class com.sushi.app.SushiApp { *; }

# Activity
-keep class com.sushi.app.MainActivity { *; }

# 保留 R 类的常量
-keepclassmembers class **.R$* {
    public static <fields>;
}
