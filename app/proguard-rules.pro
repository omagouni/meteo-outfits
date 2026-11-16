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
# --- Météo Outfits ---
# Gson sérialise nos modèles par réflexion : garder leurs champs.
-keepclassmembers class com.example.myapplication.data.model.** { <fields>; <init>(); }
-keepclassmembers class com.example.myapplication.data.local.OutfitSnapshot { <fields>; <init>(); }
-keepclassmembers class com.example.myapplication.data.remote.LinkPreview { <fields>; }
-keepclassmembers enum com.example.myapplication.** { *; }
-keepattributes Signature, *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
# SDK Anthropic (Jackson) : non utilisé en release sans clé, on garde le minimum
-dontwarn com.anthropic.**
-dontwarn com.fasterxml.jackson.**
-dontwarn org.slf4j.**
-dontwarn io.opentelemetry.**
-dontwarn java.lang.management.**
-dontwarn javax.**
# WorkManager / Room : les bases générées sont instanciées par réflexion (crash sinon en release)
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
-keepclassmembers class * extends androidx.work.ListenableWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }

# Règles officielles Retrofit (R8 full mode) : types de retour des méthodes @GET conservés
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * { @retrofit2.http.* <methods>; }
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
# Modèles JSON (Gson) : conservés entièrement, c'est léger et sans surprise
-keep class com.example.myapplication.data.model.** { *; }
-keep class com.example.myapplication.data.local.OutfitSnapshot { *; }
-keep class com.example.myapplication.data.remote.LinkPreview { *; }
-keep class com.example.myapplication.domain.ClaudeOutfitPrompt$* { *; }
-keepclassmembers,allowobfuscation class * { @com.google.gson.annotations.SerializedName <fields>; }
