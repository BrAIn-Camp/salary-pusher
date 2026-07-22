# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK's default ProGuard configuration.
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# Keep SceneView classes
-keep class io.github.sceneview.** { *; }

# Keep Room entities
-keep class com.braincamp.salarypusher.data.db.** { *; }
