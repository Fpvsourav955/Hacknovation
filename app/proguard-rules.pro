####################################
# GENERAL ANDROID RULES
####################################
-dontwarn javax.annotation.**
-dontwarn kotlin.**
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

####################################
# KEEP ALL APP CLASSES (SAFEST)
####################################
-keep class com.sourav.hacknovation.** { *; }

####################################
# FIREBASE (Auth, Firestore, Realtime DB, FCM, Storage)
####################################
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

####################################
# GOOGLE PLAY SERVICES
####################################
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

####################################
# GOOGLE SIGN-IN / CREDENTIALS API
####################################
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**

####################################
# GSON / FIRESTORE MODEL REFLECTION
####################################
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

-keepclassmembers class com.sourav.hacknovation.model.** {
    <fields>;
    <methods>;
}

-keepclasseswithmembers class com.sourav.hacknovation.model.** {
    public <init>();
}

####################################
# GLIDE (IMAGE LOADING)
####################################
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

####################################
# IMAGE SLIDER (denzcoskun)
####################################
-keep class com.denzcoskun.imageslider.** { *; }
-dontwarn com.denzcoskun.imageslider.**

####################################
# VIEWPAGER2
####################################
-keep class androidx.viewpager2.** { *; }

####################################
# MATERIAL COMPONENTS
####################################
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

####################################
# LOTTIE ANIMATIONS
####################################
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

####################################
# FACEBOOK SHIMMER
####################################
-keep class com.facebook.shimmer.** { *; }
-dontwarn com.facebook.shimmer.**

####################################
# CIRCLE IMAGE VIEW
####################################
-keep class de.hdodenhof.circleimageview.** { *; }

####################################
# SMOOTH BOTTOM BAR
####################################
-keep class me.ibrahimsn.lib.** { *; }
-dontwarn me.ibrahimsn.lib.**

####################################
# NAFIS BOTTOM NAV
####################################
-keep class com.foysal.** { *; }
-dontwarn com.foysal.**

####################################
# ANDROID SPINKIT
####################################
-keep class com.github.ybq.android.spinkit.** { *; }

####################################
# ROUNDED IMAGE VIEW
####################################
-keep class com.makeramen.roundedimageview.** { *; }

####################################
# DATA BINDING / VIEW BINDING
####################################
-keep class **Binding { *; }
-keep class androidx.databinding.** { *; }
-dontwarn androidx.databinding.**

####################################
# REMOVE LOGS IN RELEASE
####################################
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-keepclassmembers class * {
    public <init>(...);
}

-dontwarn com.google.android.material.**

-keep class com.sourav.hacknovation.ChatMessage { *; }
-keep class com.sourav.hacknovation.AttendanceModel { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule

-dontwarn com.bumptech.glide.**
-dontwarn okhttp3.**
-dontwarn okio.**

-keep class okio.** { *; }
-keep class org.json.** { *; }
-dontwarn org.json.**

-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-keepclassmembers class * {
    static final java.lang.String OPENAI_KEY;
}
