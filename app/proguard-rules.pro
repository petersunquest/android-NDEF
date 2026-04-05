# Add project specific ProGuard rules here.
#
# Play Console: upload deobfuscation file = mapping.txt (build after minify:
#   app/build/outputs/mapping/release/mapping.txt )

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Web3j / crypto ---
-keep class org.web3j.** { *; }
-dontwarn org.web3j.**

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**

# --- ZXing (embedded scanner) ---
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.journeyapps.** { *; }

# --- AndroidX Security / Tink (transitive optional annotations & SLF4J) ---
-keep class androidx.security.crypto.** { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.slf4j.**
-dontwarn org.slf4j.impl.**

# --- Coil SVG (tier / Pass artwork) ---
-keep class com.caverock.androidsvg.** { *; }
-dontwarn com.caverock.androidsvg.**

# --- Kotlin / coroutines (common R8) ---
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
