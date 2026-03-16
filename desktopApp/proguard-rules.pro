################################################################################
# General Suppressions & Fixes
################################################################################

# Suppress all "Note" messages (duplicates, dynamic access, entry points, etc.)
-dontnote **

# Suppress warnings for optional/platform-specific dependencies that are missing
-dontwarn org.apache.commons.logging.**
-dontwarn com.google.common.**
-dontwarn org.apache.avalon.**
-dontwarn org.apache.log4j.**
-dontwarn io.grpc.**
-dontwarn io.ktor.**
-dontwarn android.**
-dontwarn com.google.appengine.**
-dontwarn javax.servlet.**

# Specific fix for Guava's usage of Java 9+ MethodHandles/VarHandles
-dontwarn com.google.common.hash.**
-dontwarn com.google.common.util.concurrent.**

# If you still encounter "unresolved references" that don't cause runtime crashes,
# you can use this as a last resort, but -dontwarn is preferred for specific packages.
# -ignorewarnings

################################################################################
# Kotlin Serialization
################################################################################
# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
# noinspection ShrinkerUnresolvedReference
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    # noinspection ShrinkerUnresolvedReference
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    # noinspection ShrinkerUnresolvedReference
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences

# Disable optimisation for descriptor field because in some versions of ProGuard, optimization
# generates incorrect bytecode that causes a verification error
# see https://github.com/Kotlin/kotlinx.serialization/issues/2719
-keepclassmembers public class **$$serializer {
    private ** descriptor;
}

################################################################################
# Compose Desktop & UI (AWT/Swing/Skiko)
################################################################################
# Ensure the Main entry point and Compose Window internals are not renamed
-keep class com.truepineapps.photouploader.MainKt { *; }
-keep class androidx.compose.ui.window.** { *; }

# Prevent stripping of native UI bridge methods
-keep class java.awt.** { *; }
-keep class javax.swing.** { *; }
-keep class sun.awt.** { *; }

# Prevent stripping of native AWT event listeners
# Protects the native event listeners that receive the shutdown signal
-keepclassmembers class * extends java.awt.event.EventListener {
    *** on*(...);
}

# Skiko (Graphics engine) - Keep JNI bridge methods
-keep,includedescriptorclasses class org.jetbrains.skiko.** {
    native <methods>;
}

# Protect the Quit/Close handlers in Skiko
-keep class org.jetbrains.skiko.SkiaLayer { *; }

# Ignore Compose Preview warnings in release builds
-dontwarn **$Preview*
-dontwarn **$DefaultImpls

################################################################################
# Image Loading & Networking (Ktor CIO & Okio)
################################################################################
# Coil (Image Loading)
-keep class coil3.** { *; }
-dontwarn coil3.**

# Keep OkHttp (Coil's default network layer)
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Preserve ServiceLoader configurations for Ktor
-keepdirectories META-INF/services/**

# Keep Ktor Serialization Providers and make sure that they are not renamed
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class io.ktor.serialization.kotlinx.json.** { *; }

# Prevent R8 from stripping the ServiceLoader lookups
-keepnames class io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider
-keepnames class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider

# Keep the CIO engine factory and its internal structure
-keep class io.ktor.client.engine.cio.** { *; }
# Keep the coroutine utilities used for the pipeline
-keep class io.ktor.utils.io.** { *; }

# Okio (used by Calf and Ktor) - Uses reflection for filesystem optimizations
-dontwarn okio.**
-keep class okio.** { *; }

################################################################################
# Dependency Injection (Koin)
################################################################################
# Ensures the Koin framework itself isn't stripped.
-keep class org.koin.** { *; }
# Instead of keeping all classes, keep only those used by Koin's KSP/Annotations
-keep @org.koin.core.annotation.* class * {
    public <init>(...); 
}

################################################################################
# Google API (Google Photos)
################################################################################

# Google API Client
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**

# Specifically preserve enums used by Google API reflection
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Specific utility classes with heavy reflection must not be renamed
-keep class com.google.api.client.util.GenericData { *; }
-keep class com.google.api.client.util.GenericData$Flags { *; }

# Google OAuth uses the built-in JDK HTTP Server
-keep class com.sun.net.httpserver.** { *; }
-dontwarn com.sun.net.httpserver.**

################################################################################
# Core Kotlin & Coroutines
################################################################################
# Keep AtomicUpdaters used by Coroutines to prevent runtime crashes
-keepclassmembers class java.util.concurrent.atomic.Atomic*FieldUpdater {
    <fields>;
    <methods>;
}

# ProGuard/R8 handles Coroutines well, we just ensure descriptors are preserved
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
