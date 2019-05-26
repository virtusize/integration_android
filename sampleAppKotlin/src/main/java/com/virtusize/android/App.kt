package com.virtusize.android

import android.app.Application
import com.virtusize.libsource.Virtusize
import com.virtusize.libsource.VirtusizeBuilder
import com.virtusize.libsource.model.VirtusizeEnvironment

class App: Application() {
    lateinit var Virtusize: Virtusize

    override fun onCreate() {
        super.onCreate()

        // Initialize Virtusize instance for your application
        Virtusize = VirtusizeBuilder().init(this)
            .setApiKey("15cc36e1d7dad62b8e11722ce1a245cb6c5e6692")
            .setAppId(123)
            .setEnv(VirtusizeEnvironment.STAGING)
            .build()

    }
}