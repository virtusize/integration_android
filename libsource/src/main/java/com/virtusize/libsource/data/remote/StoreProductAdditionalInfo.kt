package com.virtusize.libsource.data.remote

import com.virtusize.libsource.util.I18nConstants

/**
 * This class represents the additional info of a store product
 * @param fit the general fit key
 * @param brandSizing the brand sizing info
 * @see BrandSizing
 */
data class StoreProductAdditionalInfo(
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