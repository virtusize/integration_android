package com.virtusize.android.data.remote

import android.content.Context
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.util.dpInPx

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
 * @param clientProductImageURL the product image URL that the client provides
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
    var storeProductMeta: StoreProductMeta? = null,
    var clientProductImageURL: String? = null,
) {
    /**
     * Gets the InPage recommendation text based on the product info
     * @param i18nLocalization [I18nLocalization]
     * @return the InPage text
     */
    fun getRecommendationText(
        context: Context,
        i18nLocalization: I18nLocalization,
        sizeComparisonRecommendedSize: SizeComparisonRecommendedSize?,
        bodyProfileRecommendedSizeName: String?,
        bodyProfileWillFit: Boolean? = null,
    ): String {
        return when {
            isAccessory() -> accessoryText(i18nLocalization, sizeComparisonRecommendedSize)
            sizes.size == 1 ->
                oneSizeText(
                    i18nLocalization = i18nLocalization,
                    sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                    bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
                    bodyProfileWillFit = bodyProfileWillFit,
                )
            else ->
                multiSizeText(
                    i18nLocalization,
                    sizeComparisonRecommendedSize,
                    bodyProfileRecommendedSizeName,
                    bodyProfileWillFit,
                )
        }
    }

    /**
     * Gets the Cloudinary image URL for the product
     */
    fun getCloudinaryProductImageURL(): String {
        return "https://res.cloudinary.com/virtusize/image/upload/w_${36.dpInPx},h_${36.dpInPx}/" +
            "q_auto,f_auto,dpr_auto/$cloudinaryPublicId.jpg"
    }

    /**
     * Gets the text for an accessory
     */
    private fun accessoryText(
        i18nLocalization: I18nLocalization,
        sizeComparisonRecommendedSize: SizeComparisonRecommendedSize?,
    ): String {
        return if (sizeComparisonRecommendedSize?.bestStoreProductSize?.name != null) {
            i18nLocalization.getHasProductAccessoryText()
        } else {
            i18nLocalization.defaultAccessoryText
        }
    }

    /**
     * Gets the text for an one-size product
     */
    private fun oneSizeText(
        i18nLocalization: I18nLocalization,
        sizeComparisonRecommendedSize: SizeComparisonRecommendedSize?,
        bodyProfileRecommendedSizeName: String?,
        bodyProfileWillFit: Boolean?,
    ): String {
        // Check if body data is provided (bodyProfileRecommendedSizeName is not null means body data was provided)
        val hasBodyData = bodyProfileRecommendedSizeName != null

        // For one-size products with body data provided
        if (hasBodyData) {
            // If willFit is not explicitly false and we have a recommended size, show the will fit message
            if (bodyProfileWillFit != false) {
                return i18nLocalization.oneSizeWillFitResultText
            }
            // If willFit is false or no recommended size, show "Your size not found"
            return i18nLocalization.willNotFitResultDefaultText
        }

        // No body data provided, check for product comparison
        sizeComparisonRecommendedSize?.let {
            return i18nLocalization.getOneSizeProductComparisonText(it)
        }

        // No data at all, show body data empty message
        return i18nLocalization.bodyDataEmptyText
    }

    /**
     * Gets the text for a multi-size product
     */
    private fun multiSizeText(
        i18nLocalization: I18nLocalization,
        sizeComparisonRecommendedSize: SizeComparisonRecommendedSize?,
        bodyProfileRecommendedSizeName: String?,
        bodyProfileWillFit: Boolean?,
    ): String {
        // Check if body data is provided
        val hasBodyData = bodyProfileRecommendedSizeName != null

        // For multi-size products with body data provided
        if (hasBodyData) {
            // If willFit is not explicitly false and we have a recommended size, show it
            if (bodyProfileWillFit != false && bodyProfileRecommendedSizeName.isNotEmpty()) {
                return i18nLocalization.getMultiSizeBodyProfileText(
                    bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
                )
            }
            // If willFit is false or no recommended size, show "Your size not found"
            return i18nLocalization.willNotFitResultDefaultText
        }

        // No body data provided, check for product comparison
        sizeComparisonRecommendedSize?.bestStoreProductSize?.name?.let {
            return i18nLocalization.getMultiSizeProductComparisonText(it)
        }

        // No data at all, show body data empty message
        return i18nLocalization.bodyDataEmptyText
    }

    /**
     * Checks if the product is an accessory
     *
     * Note: 18 is for bags, 19 is for clutches, 25 is for wallets and 26 is for props
     */
    fun isAccessory(): Boolean {
        return productType == 18 || productType == 19 || productType == 25 || productType == 26
    }

    /**
     * Checks if the product is a shoe
     */
    fun isShoe(): Boolean {
        return productType == 17
    }
}
