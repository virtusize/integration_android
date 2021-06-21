package com.virtusize.libsource

import android.content.Context
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.network.VirtusizeAPIService

class VirtusizeFlutterRepository(context: Context) {
    private val apiService: VirtusizeAPIService = VirtusizeAPIService.getInstance(context, null)

    suspend fun productDataCheck(product: VirtusizeProduct) = apiService.productDataCheck(product).successData
}