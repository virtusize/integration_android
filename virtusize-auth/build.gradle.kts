import com.virtusize.android.constants.Constants
import com.virtusize.android.extensions.publish

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    `maven-publish`
    signing
}

android {
    namespace = "${Constants.GROUP_ID}.auth"
    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        minSdk = Constants.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            buildConfigField("String", "VERSION_NANE", "\"${Constants.VERSION_NAME}\"")
        }
        release {
            buildConfigField("String", "VERSION_NANE", "\"${Constants.VERSION_NAME}\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
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
    implementation(project(":virtusize-core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    implementation(libs.androidx.browser)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
}

publish(
    publishId = "virtusize-auth",
    publishName = "virtusize-auth",
    publishDescription = "Virtusize Authentication Android SDK",
    publication = publishing,
)
