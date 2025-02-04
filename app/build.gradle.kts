plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.fpt.edu.healthtracking"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fpt.edu.healthtracking"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        compose = true
    }

    buildTypes {

        debug {
            buildConfigField("boolean", "DEBUG", "true")
            buildConfigField("String", "API_KEY", "\"AIzaSyDQDXFzl0HjB60EACA0ok9TeRp11iPSTpo\"")
        }

        release {
            buildConfigField("String", "API_KEY", "\"AIzaSyDQDXFzl0HjB60EACA0ok9TeRp11iPSTpo\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8

        }

        kotlinOptions {
            jvmTarget = "1.8"
        }

        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.1"
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }

    dependencies {
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.circleimageview)
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)

        // Hilt for dependency injection
        implementation("com.google.dagger:hilt-android:2.44")
        implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
        implementation("javax.inject:javax.inject:1")
        implementation("com.github.dhaval2404:imagepicker:2.1")
        // Core libraries
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)

        // Lifecycle and ViewModel

        implementation(libs.androidx.lifecycle.livedata.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)

        // Navigation
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)

        // Dots Indicator
        implementation(libs.dotsindicator)

        // Retrofit for networking
        implementation(libs.retrofit)
        implementation(libs.converter.gson)

        // Coroutines
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.kotlinx.coroutines.core)

        // Logging
        implementation(libs.logging.interceptor)

        // Google Sign In
        implementation(libs.play.services.auth)

        // Data Store
        implementation(libs.androidx.datastore.preferences)

        // Glide for image loading
        implementation(libs.glide)
        implementation(libs.circleimageview)

        // Firebase
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.storage)
        implementation(libs.firebase.auth)
        implementation(libs.firebase.database)
        // Image picker
        implementation(libs.philjay.mpandroidchart)

        // Material Design v190
        implementation(libs.material.v190)

        // Compose
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)

        // Test libraries
        testImplementation(libs.junit)
        testImplementation(libs.mockito.core)
        testImplementation(libs.mockito.kotlin)
        testImplementation(libs.kotlinx.coroutines.test)
        testImplementation(libs.androidx.core.testing)
        testImplementation(libs.truth)
        testImplementation(libs.mockk)
        testImplementation("androidx.arch.core:core-testing:2.1.0")
        testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        implementation (libs.converter.scalars)

        implementation (libs.otpview)

        implementation("androidx.health.connect:connect-client:1.1.0-alpha10")

        implementation ("com.prolificinteractive:material-calendarview:1.4.3")

        implementation("androidx.work:work-runtime-ktx:2.9.0")

        implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
        implementation ("com.microsoft.signalr:signalr:5.0.0")
        configurations.all {
            resolutionStrategy {
                force("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
                force("org.jetbrains.kotlin:kotlin-stdlib-common:1.9.0")
                force("androidx.core:core-ktx:1.12.0")
            }
        }
    }
}
dependencies {
    implementation(libs.firebase.auth)
    implementation(libs.common)
    implementation(libs.androidx.fragment.testing)
    implementation(libs.places)
    implementation(libs.androidx.core.animation)
    implementation(libs.androidx.core)
    testImplementation(libs.junit.junit)
    testImplementation(libs.play.services.fido)
}
