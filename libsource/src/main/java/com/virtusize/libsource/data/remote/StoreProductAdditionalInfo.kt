package com.virtusize.libsource.data.remote

import com.virtusize.libsource.util.I18nConstants

/**
 * This class represents the additional info of a store product
 * @param brand the brand name
 * @param gender the gender for the product
 * @param sizes the size list of the product
 * @param modelInfo the model info
 * @param fit the general fit key
 * @param style the store product style
 * @param brandSizing the brand sizing info
 * @see ProductSize
 * @see BrandSizing
 */
data class StoreProductAdditionalInfo(
    val brand: String,
    val gender: String?,
    val sizes: Set<ProductSize>,
    val modelInfo: Map<String, Any>?,
    val fit: String?,
    val style: String?,
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
            return I18nConstants.GENERAL_FIT_LOOSE_KEY
        }
        if(mutableListOf("tight", "slim").contains(fit)) {
            return I18nConstants.GENERAL_FIT_TIGHT_KEY
        }
        return I18nConstants.GENERAL_FIT_REGULAR_KEY
    }
}