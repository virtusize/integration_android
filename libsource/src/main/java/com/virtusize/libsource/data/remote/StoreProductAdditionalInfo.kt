package com.virtusize.libsource.data.remote

data class StoreProductAdditionalInfo(
    val fit: String?,
    val brandSizing: BrandSizing?
) {
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