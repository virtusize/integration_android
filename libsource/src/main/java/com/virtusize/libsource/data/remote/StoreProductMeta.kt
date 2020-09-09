package com.virtusize.libsource.data.remote

/**
 * This class represents the meta data of a store product
 * @param id the ID of the store product meta
 * @param additionalInfo the additional info of a store product
 * @see StoreProductAdditionalInfo
 */
data class StoreProductMeta(
    val id: Int,
    val additionalInfo: StoreProductAdditionalInfo?
)