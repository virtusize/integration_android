package com.virtusize.libsource.model

import com.virtusize.libsource.data.pojo.ProductCheckResponse

/**
 * This class represents VirtusizeProduct object.
 * You need to pass in externalId
 * @param externalId Represents the id that will be used to reference this product in Virtusize API
 * @param imageUrl Image URL of product, in order to populate the comparison view
 * @param productCheckData Response from Virtusize Server for Product check request
 */
data class VirtusizeProduct (
    val externalId: String,
    var imageUrl: String? = null,
    var productCheckData: ProductCheckResponse? = null
)