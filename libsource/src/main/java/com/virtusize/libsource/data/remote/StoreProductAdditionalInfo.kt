package com.virtusize.libsource.data.remote

import com.virtusize.libsource.util.I18nConstants

data class StoreProductAdditionalInfo(
    val fit: String?,
    val brandSizing: BrandSizing?
) {
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