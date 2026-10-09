# Retrofit
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**

# Kotlin Serialization
-keep class kotlinx.serialization.** { *; }
-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,RuntimeInvisibleAnnotations,RuntimeInvisibleParameterAnnotations,RuntimeInvisibleTypeAnnotations,AnnotationDefault,MethodParameters

# Supabase
-keep class io.github.supabase.** { *; }
-keep class io.ktor.** { *; }
-keepattributes Signature

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# MLKit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-keepattributes *Annotation*
