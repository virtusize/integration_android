package com.virtusize.android.extensions

import org.gradle.api.Project
import java.util.Properties

/**
 * Get the value of the key from the local.properties file.
 * If the key is not found in the local.properties file,
 * it will try to get the value from the environment variables.
 */
fun Project.getProperties(key: String): String? {
    val localPropertiesFile = file("local.properties")
    return if (localPropertiesFile.exists()) {
        Properties().apply {
            load(localPropertiesFile.inputStream())
        }.getProperty(key)
    } else {
        System.getenv(key)
    }
}
