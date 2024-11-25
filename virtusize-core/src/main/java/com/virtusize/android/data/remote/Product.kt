package com.virtusize.android.data.remote

import android.content.Context
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.StoreName
import com.virtusize.android.data.local.VirtusizeStoreRepository
import com.virtusize.android.network.VirtusizeApi
import com.virtusize.android.util.ConfigurationUtils
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
    ): String {
        return when {
            isAccessory() -> accessoryText(i18nLocalization, sizeComparisonRecommendedSize)
            sizes.size == 1 ->
                oneSizeText(
                    i18nLocalization = i18nLocalization,
                    sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                    bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
                )
            else ->
                multiSizeText(
                    context = context,
                    i18nLocalization,
                    sizeComparisonRecommendedSize,
                    bodyProfileRecommendedSizeName,
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
    ): String {
        bodyProfileRecommendedSizeName?.let {
            return i18nLocalization.oneSizeWillFitResultText
        }
        sizeComparisonRecommendedSize?.let {
            return i18nLocalization.getOneSizeProductComparisonText(it)
        }
        return i18nLocalization.willNotFitResultText
    }

    /**
     * Gets the text for a multi-size product
     */
    private fun multiSizeText(
        context: Context,
        i18nLocalization: I18nLocalization,
        sizeComparisonRecommendedSize: SizeComparisonRecommendedSize?,
        bodyProfileRecommendedSizeName: String?,
    ): String {
        bodyProfileRecommendedSizeName?.let {
            return i18nLocalization.getMultiSizeBodyProfileText(
                context = context,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            )
        }
        sizeComparisonRecommendedSize?.bestStoreProductSize?.name?.let {
            return i18nLocalization.getMultiSizeProductComparisonText(it)
        }
        return i18nLocalization.willNotFitResultText
    }

    /**
     * Checks if the product is an accessory
     *
     * Note: 18 is for bags, 19 is for clutches, 25 is for wallets and 26 is for props
     */
    fun isAccessory(): Boolean {
        return productType == 18 || productType == 19 || productType == 25 || productType == 26
    }
}
