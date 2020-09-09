package com.virtusize.android

import com.virtusize.libsource.Virtusize
import com.virtusize.libsource.VirtusizeBuilder
import com.virtusize.libsource.data.local.VirtusizeEnvironment
import com.virtusize.libsource.data.local.VirtusizeLanguage
import com.virtusize.libsource.data.local.VirtusizeProduct

class TestApplication : App() {
    lateinit var Virtuzise: Virtusize

    private var storeProduct = StoreProduct.virtusizeProduct

    override fun provideProduct(): VirtusizeProduct = storeProduct

    fun setVirtusizeProduct(virtusizeProduct: VirtusizeProduct) {
        storeProduct = virtusizeProduct
    }

    override fun onCreate() {
       Virtusize = VirtusizeBuilder().init(this)
            .setApiKey("15cc36e1d7dad62b8e11722ce1a245cb6c5e6692")
            .setUserId("123")
            .setEnv(VirtusizeEnvironment.STAGING)
            .setLanguage(VirtusizeLanguage.EN)
            .build()
    }
}