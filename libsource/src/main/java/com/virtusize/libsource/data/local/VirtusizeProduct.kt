package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.remote.ProductCheck

/**
 * This class represents a VirtusizeProduct object.
 * You need to pass in externalId
 * @param externalId the ID that will be used to reference this product in Virtusize API
 * @param imageUrl the image URL of this product, in order to populate the comparison view
 * @param productCheckData the product check response from Virtusize API
 */
data class VirtusizeProduct @JvmOverloads constructor(
    val externalId: String,
    var imageUrl: String? = null,
    var productCheckData: ProductCheck? = null
)