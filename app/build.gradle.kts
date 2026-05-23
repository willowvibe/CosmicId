import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

// ── Release signing ─────────────────────────────────────────────────────────
// Reads keystore details from `keystore.properties` at project root (git-ignored).
// See keystore.properties.example for the required keys. Builds fall back to the
// debug keystore automatically if `keystore.properties` is not present.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile")?.isNotBlank() == true

android {
    namespace = "com.willowvibe.agereveal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.willowvibe.cosmicid"
        minSdk = 26  // java.time is native on API 26+; use desugaring below for API 21+
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 8  // must increment for every release; v1.0.7 was 7
        // Read version from VERSION file
        val versionFile = File(projectDir.absolutePath + "/../VERSION")
        versionName = if (versionFile.exists()) {
            versionFile.readText().trim()
        } else {
            "1.0"
        }

        testInstrumentationRunner = "com.willowvibe.agereveal.HiltTestRunner"
        vectorDrawables.useSupportLibrary = true

        // Per-app language support — enables system-level language picker integration
        resourceConfigurations += listOf("en", "hi", "ta", "te", "kn", "ko", "vi", "zh-rCN")

        // TODO(playstore): Replace with production AdMob App ID before release.
        // Test ID: ca-app-pub-3940256099942544~3347511713
        // Production ID goes here — set via local.properties `admobAppId=...` or CI env var.
        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use real release keystore when available; otherwise Gradle uses debug keystore
            // so the AAB can still be assembled locally for testing.
            signingConfig = if (hasReleaseKeystore) signingConfigs.getByName("release")
            else signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        // Enable Java 8+ API desugaring for minSdk < 26 if needed in the future
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Expose Room schemas to version control (for migration tests)
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
            )
        }
    }

    lint {
        // Don't block release builds on non-fatal lint warnings. Fatal issues still fail.
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false
    }
}

dependencies {
    // Desugaring — keeps java.time working even if minSdk is dropped to 21
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat) // AppCompatDelegate.setApplicationLocales
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM — aligns all Compose library versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager (notification scheduling)
    implementation(libs.androidx.work.runtime.ktx)

    // Glance widgets (Compose-driven AppWidget)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // AdMob
    implementation(libs.play.services.ads)
    implementation(libs.material)

    // In-app review prompt (after first share)
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)

    // Google Play Billing (v2.0 subscriptions)
    implementation(libs.billing.ktx)

    // Firebase Analytics (minimum viable analytics for beta)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // DataStore for theme / user preferences
    implementation(libs.androidx.datastore.preferences)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.json:json:20231013")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    // Hilt testing for instrumented tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
    kspAndroidTest("com.google.dagger:hilt-compiler:${libs.versions.hilt.get()}")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
