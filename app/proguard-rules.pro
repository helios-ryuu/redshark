# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Firestore DTO data classes (used via reflection by Firestore SDK)
-keep class com.helios.redshark.data.remote.firestore.dto.** { *; }

# Keep Firebase / Firestore SDK models
-keepattributes Signature
-keepattributes *Annotation*

# Keep Kotlin data classes and their fields
-keepclassmembers class * {
    @com.google.firebase.firestore.IgnoreExtraProperties *;
}

# Timber
-dontwarn org.jetbrains.annotations.**

# Preserve stack trace line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Hilt-generated entry points
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }