# Keep models
-keep class com.savage.anime.domain.models.** { *; }
-keep class com.savage.anime.data.local.entity.** { *; }
-keep class com.savage.anime.data.network.dto.** { *; }

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep Retrofit
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
