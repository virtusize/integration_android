import com.virtusize.android.constants.Constants
import com.virtusize.android.extensions.publish

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
    signing
}

android {
    namespace = "${Constants.GROUP_ID}.ui"
    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        minSdk = Constants.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res",
                "src/main/java/com/virtusize/android/ui/avatar/res",
                "src/main/java/com/virtusize/android/ui/button/res",
                "src/main/java/com/virtusize/android/ui/tooltip/res",
            )
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            // TODO: Fix error. See: https://github.com/Kotlin/dokka/issues/2956
            // withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
}

publish(
    publishId = "virtusize-ui",
    publishName = "virtusize-ui",
    publishDescription = "Virtusize UI Toolkit Android SDK",
    publication = publishing,
)
