package com.virtusize.libsource.data.remote

/**
 * This class represents the product info from the Virtusize API
 * @param id the internal product ID in the Virtusize server
 * @param sizes the sizes that this product has
 * @param externalId the external product ID from the client's store
 * @param productType the ID of the product type of this product
 * @param name the product name
 * @param cloudinaryPublicId the store product image public id from Cloudinary
 * @param isFavorite is true if the product is marked as a favorite
 * @param storeId the ID of the store that this product belongs to
 * @param storeProductMeta the additional data of this product
 * @see ProductSize
 * @see StoreProductMeta
 */
data class Product(
    val id: Int,
    val sizes: List<ProductSize>,
    var externalId: String? = null,
    val productType: Int,
    val name: String,
    val cloudinaryPublicId: String,
    var isFavorite: Boolean? = null,
    val storeId: Int,
    var storeProductMeta: StoreProductMeta? = null
) {
    /**
     * Gets the InPage recommendation text based on the product info
     * @param i18nLocalization [I18nLocalization]
     * @return the InPage text
     */
    fun getRecommendationText(i18nLocalization: I18nLocalization): String {
        return when {
            isAccessory() -> {
                i18nLocalization.defaultAccessoryText
            }
            else -> i18nLocalization.defaultNoDataText
        }
    }

    /**
     * Checks if the product is an accessory
     *
     * Note: 18 is for bags, 19 is for clutches, 25 is for wallets and 26 is for props
     */
    internal fun isAccessory(): Boolean {
        return productType == 18 || productType == 19 || productType == 25 || productType == 26
    }
}