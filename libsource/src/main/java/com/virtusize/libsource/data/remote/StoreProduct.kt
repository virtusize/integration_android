package com.virtusize.libsource.data.remote

/**
 * This class represents the response for the request to getting the store product info
 * @param id the internal product ID in the Virtusize server
 * @param sizes the sizes that this product has
 * @param externalId the external product ID from the client's store
 * @param productType the ID of the product type of this product
 * @param name the product name
 * @param cloudinaryPublicId the store product image public id from Cloudinary
 * @param storeId the ID of the store that this product belongs to
 * @param storeProductMeta the additional data of this product
 * @see ProductSize
 * @see StoreProductMeta
 */
data class StoreProduct(
    val id: Int,
    val sizes: List<ProductSize>,
    val externalId: String,
    val productType: Int,
    val name: String,
    val cloudinaryPublicId: String,
    val storeId: Int,
    val storeProductMeta: StoreProductMeta?
) {
    /**
     * Gets the InPage recommendation text based on the product info
     * @param i18nLocalization [I18nLocalization]
     * @return the InPage text
     */
    fun getRecommendationText(i18nLocalization: I18nLocalization): String {
        var text: String? = null
        when {
            isAccessory() -> {
                text = i18nLocalization.defaultAccessoryText
            }
            storeProductMeta?.additionalInfo?.brandSizing != null -> {
                text = i18nLocalization.getSizingText(storeProductMeta.additionalInfo.brandSizing)
            }
            storeProductMeta?.additionalInfo?.getGeneralFitKey() != null -> {
                text = i18nLocalization.getFitText(storeProductMeta.additionalInfo.getGeneralFitKey())
            }
        }
        return text ?: i18nLocalization.defaultText
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