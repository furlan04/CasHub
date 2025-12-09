import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "it.unimib.CasHub"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "it.unimib.CasHub"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "stocks_api_key", gradleLocalProperties(rootDir, providers).getProperty("stocks_api_key"))
        resValue("bool", "debug", gradleLocalProperties(rootDir, providers).getProperty("debug"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.commons.validator)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.cardview)

    implementation("com.squareup.retrofit2:retrofit:2.12.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime)
    implementation(libs.webgpu)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
}