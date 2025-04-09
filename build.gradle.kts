 plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("androidx.navigation.safeargs") version "2.8.9" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}