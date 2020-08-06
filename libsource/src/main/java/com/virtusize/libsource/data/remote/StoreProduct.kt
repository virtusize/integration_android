package com.virtusize.libsource.data.remote

import android.content.Context
import com.virtusize.libsource.R
import com.virtusize.libsource.util.getStringResourceByName

data class StoreProduct(
    val id: Int,
    val sizes: List<ProductSize>,
    val externalId: String,
    val productType: Int,
    val name: String,
    val storeId: Int,
    val storeProductMeta: StoreProductMeta?
) {
    fun getRecommendationText(context: Context): String {
        var text: String? = null
        when {
            isAccessory() -> {
                text = context.getString(R.string.inpage_default_accessory_text)
            }
            storeProductMeta?.additionalInfo?.brandSizing != null -> {
                val brandSizing = storeProductMeta.additionalInfo.brandSizing
                text = context.getStringResourceByName("inpage_sizing_${brandSizing.getBrandKey()}_${brandSizing.compare}_text")
            }
            storeProductMeta?.additionalInfo?.getGeneralFitKey() != null -> {
                val generalFitKey = storeProductMeta.additionalInfo.getGeneralFitKey()
                text = context.getStringResourceByName("inpage_fit_${generalFitKey}_text")
            }
        }
        return text ?: context.getString(R.string.inpage_default_text)
    }

    /**
     * Checks if the product is an accessory
     *
     * Note: 18 is for bags, 19 is for clutches, 25 is for wallets and 26 is for props
     */
    private fun isAccessory(): Boolean {
        return productType == 18 || productType == 19 || productType == 25 || productType == 26
    }
}