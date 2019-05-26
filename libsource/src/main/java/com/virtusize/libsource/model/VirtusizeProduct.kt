package com.virtusize.libsource.model

import com.virtusize.libsource.data.pojo.ProductCheckResponse

data class VirtusizeProduct (
    val externalId: String,
    var imageUrl: String? = null,
    var productCheckData: ProductCheckResponse? = null
)