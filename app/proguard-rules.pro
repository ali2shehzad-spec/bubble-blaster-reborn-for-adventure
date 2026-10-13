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

# Preserve line numbers and source file names for production crash reports
-keepattributes SourceFile,LineNumberTable

# Preserve game models and data classes from aggressive obfuscation
-keep class com.example.neonpop.model.** { *; }
-keep class com.example.neonpop.data.** { *; }

# Preserve AntiCheat security classes and methods
-keep class com.example.neonpop.security.** { *; }

# Preserve Room entities if used
-keep class androidx.room.** { *; }
