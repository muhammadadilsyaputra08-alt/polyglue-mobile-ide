-keep class com.polyglue.ide.** { *; }
-keepclassmembers class com.polyglue.ide.** { *; }
-keep class org.luaj.** { *; }
-keepclassmembers class org.luaj.** { *; }
-keep class com.quickjs.** { *; }
-keep class com.chaquo.python.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class com.google.gson.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
