package com.virtusize.android.domain

import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.remote.ProductCheck

internal interface VirtusizeRepository {
    /**
     * Sets up and check the product for the product detail page
     *
     * @param product VirtusizeProduct that is being loaded with the Virtusize API
     * @return [ProductCheck] object that contains the product data check result
     */
    suspend fun productDataCheck(product: VirtusizeProduct): ProductCheck?
}
