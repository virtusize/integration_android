package com.virtusize.sampleappkotlin

import android.app.Application
import com.virtusize.android.VirtusizeBuilder
import com.virtusize.android.data.local.VirtusizeEnvironment
import com.virtusize.android.data.local.VirtusizeInfoCategory
import com.virtusize.android.data.local.VirtusizeLanguage

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Virtusize instance for your application
        VirtusizeBuilder().init(this)
            // Only the API key is required
            .setApiKey("f47e670dabb372b6a0cd5cde1719bebed427b30d")
            // For using the Order API, a user ID is required
            .setUserId("123")
            // By default, the Virtusize environment will be set to GLOBAL
            .setEnv(VirtusizeEnvironment.STAGING)
            // By default, the initial language will be set based on the Virtusize environment
            .setLanguage(VirtusizeLanguage.EN)
            // By default, ShowSGI is false
            .setShowSGI(true)
            // By default, Virtusize allows all the possible languages
            .setAllowedLanguages(listOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
            // By default, Virtusize displays all the possible info categories in the Product Details tab
            .setDetailsPanelCards(setOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
            // By default, Virtusize enables the SNS buttons
            .setShowSNSButtons(true)
            // By default, branch is empty and `production` is
            .setBranch("snkrdnk-line-quick-fix")
            // By default, Virtusize shows Privacy Policy
            .setShowPrivacyPolicy(true)
            .setServiceEnvironment(false)
            .build()
    }
}
