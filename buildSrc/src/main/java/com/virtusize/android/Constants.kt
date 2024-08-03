package com.virtusize.android

object Constants {
    const val COMPILE_SDK = 34
    const val MIN_SDK = 21
    const val TARGET_SDK = 34

    // Update versionName when publishing a new release
    const val VERSION_NAME = "2.5.5"
    const val GROUP_ID = "com.virtusize.android"
    const val POM_URL = "https://github.com/virtusize/integration_android"

    object Virtusize {
        const val ARTIFACT_ID = "virtusize"
        const val ARTIFACT_NAME = "virtusize"
        const val ARTIFACT_DESCRIPTION = "Virtusize Android SDK"
    }

    object VirtusizeCore {
        const val ARTIFACT_ID = "virtusize-core"
        const val ARTIFACT_NAME = "virtusize-core"
        const val ARTIFACT_DESCRIPTION = "The core module of Virtusize Android SDK"
    }
}
