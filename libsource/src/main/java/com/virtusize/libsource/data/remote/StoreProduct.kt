package com.virtusize.libsource.data.remote

data class StoreProduct(
    val id: Int,
    val sizes: List<ProductSize>,
    val externalId: String,
    val productType: Int,
    val name: String,
    val storeId: Int,
    val storeProductMeta: StoreProductMeta?
) {
    fun getRecommendationText(I18nLocalization: I18nLocalization): String {
        var text: String? = null
        when {
            isAccessory() -> {
                text = I18nLocalization.defaultAccessoryText
            }
            storeProductMeta?.additionalInfo?.brandSizing != null -> {
                text = I18nLocalization.getSizingText(storeProductMeta.additionalInfo.brandSizing)
            }
            storeProductMeta?.additionalInfo?.getGeneralFitKey() != null -> {
                text = I18nLocalization.getFitText(storeProductMeta.additionalInfo.getGeneralFitKey())
            }
        }
        return text ?: I18nLocalization.defaultText
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