package com.virtusize.android.extensions

import com.virtusize.android.constants.Constants
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

private val isSnapshot: Boolean
    get() = Constants.VERSION_NAME.endsWith("SNAPSHOT")

private fun Project.configureRepositories() {
    configure<PublishingExtension> {
        publications {
            repositories {
                maven {
                    val releasesRepoUrl =
                        "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotsRepoUrl =
                        "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    url =
                        uri(if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl)
                    credentials {
                        username = getProperties("OSSRH_USERNAME")
                        password = getProperties("OSSRH_PASSWORD")
                    }
                }
            }
        }
    }
}

private val mavenPublicationName = "maven"

private fun Project.configureMavenPublication(
    publishId: String,
    publishName: String,
    publishDescription: String,
) {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(mavenPublicationName) {
                groupId = Constants.GROUP_ID
                artifactId = publishId
                version = Constants.VERSION_NAME

                afterEvaluate {
                    from(components["release"])
                }

                pom {
                    val pomURL = "https://github.com/virtusize/integration_android"
                    name.set(publishName)
                    packaging = "aar"
                    description.set(publishDescription)
                    url.set(pomURL)

                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://raw.githubusercontent.com/virtusize/integration_android/master/LICENSE")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("virtusize")
                            name.set("Virtusize")
                        }
                    }

                    scm {
                        connection.set("scm:git@github.com/virtusize/integration_android.git")
                        developerConnection.set("scm:git@github.com/virtusize/integration_android.git")
                        url.set(pomURL)
                    }
                }
            }
        }
    }
}

private fun Project.configureSigning(publication: PublishingExtension) {
    configure<SigningExtension> {
        val areSigningCredentialsProvided =
            getProperties("GPG_KEY_ID") != null &&
                getProperties("GPG_KEY") != null &&
                getProperties("GPG_KEY_PASSWORD") != null
        isRequired = !isSnapshot && areSigningCredentialsProvided
        if (isRequired) {
            useInMemoryPgpKeys(
                getProperties("GPG_KEY_ID"),
                getProperties("GPG_KEY"),
                getProperties("GPG_KEY_PASSWORD"),
            )
        }
        sign(publication.publications[mavenPublicationName])
    }
}

fun Project.publish(
    publishId: String,
    publishName: String,
    publishDescription: String,
    publication: PublishingExtension,
) {
    configureRepositories()
    configureMavenPublication(publishId, publishName, publishDescription)
    configureSigning(publication)
}
