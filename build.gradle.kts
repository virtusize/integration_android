import com.virtusize.android.constants.Constants
import com.virtusize.android.extensions.getProperties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.nexus.publish)
}

apply(from = "${project.rootDir}/gradle/githooks.gradle.kts")
apply(from = "${project.rootDir}/gradle/ktlint.gradle.kts")

allprojects {
    group = Constants.GROUP_ID
}

nexusPublishing {
    repositories {
        sonatype {
            // only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username = getProperties("OSSRH_USERNAME")
            password = getProperties("OSSRH_PASSWORD")
        }
    }
}
