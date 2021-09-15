package com.virtusize.libsource.data.remote

/**
 * This class represents the response for the request ProductMetaDataHints
 * @see VirtusizeEndpoint.ProductMetaDataHints
 */
internal data class ProductMetaDataHints(
    val apiKey: String,
    val imageUrl: String,
    val cloudinaryPublicId: String,
    val externalProductId: String
)
