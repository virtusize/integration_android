package com.virtusize.android

import android.app.Application
import com.virtusize.libsource.Virtusize
import com.virtusize.libsource.VirtusizeBuilder
import com.virtusize.libsource.data.local.*

class App: Application() {
    lateinit var Virtusize: Virtusize

    override fun onCreate() {
        super.onCreate()

        // Initialize Virtusize instance for your application
        Virtusize = VirtusizeBuilder().init(this)
            .setApiKey("15cc36e1d7dad62b8e11722ce1a245cb6c5e6692")
            .setUserId("123")
            .setEnv(VirtusizeEnvironment.STAGING)
            .setLanguage(VirtusizeLanguage.EN)
            .setShowSGI(false)
            .setAllowedLanguages(mutableListOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
            .setDetailsPanelCards(mutableListOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
            .build()
    }
}