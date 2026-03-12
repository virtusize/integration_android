package com.virtusize.sampleapp.compose

import android.app.Application
import com.virtusize.android.VirtusizeBuilder
import com.virtusize.android.data.local.VirtusizeEnvironment
import com.virtusize.android.data.local.VirtusizeInfoCategory
import com.virtusize.android.data.local.VirtusizeLanguage

const val KeyJp = "fc3165757dadee62df86f7a6a260bda855efc704"
const val KeyKr = "813769e4b2e6a7479b90579fa9252d49a9cd3341"

internal class VirtusizeSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        VirtusizeBuilder().init(this)
            // Only the API key is required
            .setApiKey(KeyJp)
            // For using the Order API, a user ID is required
            .setUserId("123")
            // By default, the Virtusize environment will be set to GLOBAL
            .setEnv(VirtusizeEnvironment.JAPAN)
            // By default, the initial language will be set based on the Virtusize environment
            .setLanguage(VirtusizeLanguage.EN)
            // By default, ShowSGI is false
            .setShowSGI(true)
            // By default, Virtusize allows all the possible languages
            .setAllowedLanguages(listOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP, VirtusizeLanguage.KR))
            // By default, Virtusize displays all the possible info categories in the Product Details tab
            .setDetailsPanelCards(setOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
            // By default, Virtusize enables the SNS buttons
            .setShowSNSButtons(true)
            // By default, Virtusize shows Privacy Policy
            .setShowPrivacyPolicy(true)
            .build()
    }
}
