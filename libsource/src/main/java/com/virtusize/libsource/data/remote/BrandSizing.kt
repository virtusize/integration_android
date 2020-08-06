package com.virtusize.libsource.data.remote

data class BrandSizing(
    val compare: String,
    val itemBrand: Boolean
) {
    fun getBrandKey(): String {
        return if(itemBrand) {
            "itemBrand"
        } else {
            "mostBrands"
        }
    }
}