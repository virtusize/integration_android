package com.virtusize.libsource.data.remote

/**
 * This class represents the additional info of a store product
 * @param fit the general fit key
 * @param brandSizing the brand sizing info
 * @see BrandSizing
 */
data class StoreProductAdditionalInfo(
    val fit: String?,
    val brandSizing: BrandSizing?
) {
    /**
     * Gets the general fit key for the fitting related InPage texts
     */
    fun getGeneralFitKey(): String? {
        if(fit == null) {
            return null
        }
        if(mutableListOf("loose", "wide", "flared").contains(fit)) {
            return "loose"
        }
        if(mutableListOf("tight", "slim").contains(fit)) {
            return "tight"
        }
        return "regular"
    }
}