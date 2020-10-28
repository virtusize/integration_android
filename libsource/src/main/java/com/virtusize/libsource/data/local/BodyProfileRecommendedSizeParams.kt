package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.data.remote.UserBodyProfile
import org.json.JSONObject

/**
 * The class that wraps the parameters for the API request of getting the recommended size based on the user's body profile
 *
 * @param productTypes the list of available [ProductType]
 * @param storeProduct the store product info in the type of [Product]
 * @param userBodyProfile the user body profile
 * @see ProductType
 * @see Product
 * @see UserBodyProfile
 */
internal data class BodyProfileRecommendedSizeParams constructor(
    private val productTypes: List<ProductType>,
    private val storeProduct: Product,
    private val userBodyProfile: UserBodyProfile
) {

    /**
     * Returns the map that represents the API request body
     * @return the name of the event
     */
    fun paramsToMap(): Map<String, Any> {
        return emptyMap<String, Any>()
            .plus(
                mapOf(PARAM_ADDITIONAL_INFO to createAdditionalInfoParams() )
            )
            .plus(
                mapOf(PARAM_BODY_DATA to createBodyDataParams())
            )
            .plus(
                mapOf(PARAM_ITEM_SIZES to createItemSizesParams())
            )
            .plus(
                mapOf(PARAM_PRODUCT_TYPE to (productTypes.find { it.id == storeProduct.productType }?.name ?: ""))
            )
            .plus(
                mapOf(PARAM_USER_GENDER to (userBodyProfile.gender ?: ""))
            )
    }

    /**
     * Creates the map that represents the store product additional info
     */
    fun createAdditionalInfoParams(): Map<String, Any?> {
        val brand = storeProduct.storeProductMeta?.additionalInfo?.brand ?: storeProduct.storeProductMeta?.brand
        val sizeHashMap = storeProduct.storeProductMeta?.additionalInfo?.sizes?.map {
            it.name to it.measurements.map { measurement ->
                measurement.name to measurement.millimeter
            }.toMap()
        }?.toMap()
        val gender = storeProduct.storeProductMeta?.additionalInfo?.gender ?: storeProduct.storeProductMeta?.gender
        return emptyMap<String, Any?>()
            .plus(
                mapOf(PARAM_BRAND to (brand ?: ""))
            )
            .plus(
                mapOf(PARAM_FIT to (storeProduct.storeProductMeta?.additionalInfo?.fit ?: "regular"))
            )
            .plus(
                mapOf(PARAM_SIZES to (sizeHashMap ?: mutableMapOf()))
            )
            .plus(
                mapOf(PARAM_MODEL_INFO to (storeProduct.storeProductMeta?.additionalInfo?.modelInfo ?: mutableMapOf()))
            )
            .plus(
                mapOf(PARAM_GENDER to (gender ?: JSONObject.NULL))
            )
    }

    /**
     * Creates the map that represents the user body data
     */
    fun createBodyDataParams(): Map<String, Any?> {
        var chestValue: Int? = null
        return emptyMap<String, Any?>()
            .plus(
                userBodyProfile.bodyData.map {
                    if(it.name == PARAM_BODY_BUST) {
                        chestValue = it.millimeter
                    }
                    it.name to mutableMapOf(PARAM_BODY_MEASUREMENT_VALUE to it.millimeter, PARAM_BODY_MEASUREMENT_PREDICTED to true)
                }
            )
            .plus(
                chestValue?.let { mapOf(PARAM_BODY_CHEST to mutableMapOf(PARAM_BODY_MEASUREMENT_VALUE to it, PARAM_BODY_MEASUREMENT_PREDICTED to true))}.orEmpty()
            )
    }

    /**
     * Creates the map that represents the store product size info
     */
    fun createItemSizesParams(): Map<String, Any?> {
        return emptyMap<String, Any?>()
            .plus(
                storeProduct.sizes.map { productSize ->
                    productSize.name to productSize.measurements.map { measurement ->
                        measurement.name to measurement.millimeter
                    }.toMap()
                }
            )
    }

    private companion object {
        const val PARAM_ADDITIONAL_INFO = "additional_info"
        const val PARAM_BODY_DATA = "body_data"
        const val PARAM_ITEM_SIZES = "item_sizes_orig"
        const val PARAM_PRODUCT_TYPE = "product_type"
        const val PARAM_USER_GENDER = "user_gender"

        const val PARAM_BRAND = "brand"
        const val PARAM_FIT = "fit"
        const val PARAM_SIZES = "sizes"
        const val PARAM_MODEL_INFO = "model_info"
        const val PARAM_GENDER = "gender"

        const val PARAM_BODY_MEASUREMENT_VALUE = "value"
        const val PARAM_BODY_MEASUREMENT_PREDICTED = "predicted"
        const val PARAM_BODY_BUST = "bust"
        const val PARAM_BODY_CHEST = "chest"
    }
}