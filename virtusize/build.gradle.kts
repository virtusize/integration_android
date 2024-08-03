import com.virtusize.android.Constants
import com.virtusize.android.getProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
    signing
}

android {
    namespace = "com.virtusize.android"
    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        minSdk = Constants.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
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
    api(project(":virtusize-core"))
    implementation(libs.virtusize.auth)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.material)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (Constants.VERSION_NAME.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = getProperties("OSSRH_USERNAME")
                password = getProperties("OSSRH_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = Constants.GROUP_ID
            artifactId = Constants.Virtusize.ARTIFACT_ID
            version = Constants.VERSION_NAME

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name = Constants.Virtusize.ARTIFACT_NAME
                packaging = "aar"
                description = Constants.Virtusize.ARTIFACT_DESCRIPTION
                url = Constants.POM_URL

                licenses {
                    license {
                        name = "The MIT License"
                        url = "https://raw.githubusercontent.com/virtusize/integration_android/master/LICENSE"
                        distribution = "repo"
                    }
                }

                developers {
                    developer {
                        id = "virtusize"
                        name = "Virtusize"
                    }
                }

                scm {
                    connection = "scm:git@github.com/virtusize/integration_android.git"
                    developerConnection = "scm:git@github.com/virtusize/integration_android.git"
                    url = Constants.POM_URL
                }
            }
        }
    }

    val shouldSign =
        getProperties("GPG_KEY_ID") != null &&
                getProperties("GPG_KEY") != null &&
                getProperties("GPG_KEY_PASSWORD") != null

    signing {
        isRequired = shouldSign && gradle.taskGraph.hasTask("publish")
        if (isRequired) {
            useInMemoryPgpKeys(
                getProperties("GPG_KEY_ID"),
                getProperties("GPG_KEY"),
                getProperties("GPG_KEY_PASSWORD"),
            )
        }
        sign(publishing.publications["maven"])
    }
}
