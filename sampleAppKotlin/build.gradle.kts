import com.virtusize.android.constants.Constants

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.virtusize.sampleappkotlin"
    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        applicationId = "com.virtusize.sampleappkotlin"
        minSdk = Constants.MIN_SDK
        targetSdk = Constants.TARGET_SDK
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

//    implementation(libs.virtusize)
    implementation(project(":virtusize"))
    // For the WebViewFragment example implementation
    implementation(libs.virtusize.auth)
}
