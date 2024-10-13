package com.virtusize.android.data.local

import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.UserBodyProfile
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
    private val userBodyProfile: UserBodyProfile,
) {
    /**
     * Returns the map that represents the API request body
     * @return the name of the event
     */
    fun paramsToMap(): Map<String, Any> =
        emptyMap<String, Any>()
            .plus(
                mapOf(PARAM_BODY_DATA to createBodyDataParams()),
            ).plus(
                mapOf(PARAM_USER_GENDER to userBodyProfile.gender),
            ).plus(
                mapOf(PARAM_USER_HEIGHT to userBodyProfile.height),
            ).plus(
                userBodyProfile.weight
                    .toFloatOrNull()
                    ?.let { mapOf(PARAM_USER_WEIGHT to it) }
                    .orEmpty(),
            ).plus(
                mapOf(PARAM_ITEMS to arrayOf(createItemsParams())),
            )

    private fun createItemsParams(): Map<String, Any?> =
        emptyMap<String, Any?>()
            .plus(
                mapOf(PARAM_ITEM_SIZES to createItemSizesParams()),
            ).plus(
                mapOf(
                    PARAM_PRODUCT_TYPE to
                        (productTypes.find { it.id == storeProduct.productType }?.name ?: ""),
                ),
            ).plus(
                mapOf(PARAM_ADDITIONAL_INFO to createAdditionalInfoParams()),
            ).plus(
                mapOf(PARAM_EXTERNAL_PRODUCT_ID to (storeProduct.externalId ?: "")),
            )

    /**
     * Creates the map that represents the store product additional info
     */
    fun createAdditionalInfoParams(): Map<String, Any?> {
        val brand =
            storeProduct.storeProductMeta?.additionalInfo?.brand
                ?: storeProduct.storeProductMeta?.brand
        val sizeHashMap =
            storeProduct.storeProductMeta?.additionalInfo?.sizes?.associate {
                it.name to
                    it.measurements.associate { measurement ->
                        measurement.name to measurement.millimeter
                    }
            }
        /*val gender = storeProduct.storeProductMeta?.additionalInfo?.gender
            ?: storeProduct.storeProductMeta?.gender*/
        return emptyMap<String, Any?>()
            .plus(
                mapOf(PARAM_BRAND to (brand ?: "")),
            ).plus(
                mapOf(
                    PARAM_FIT to (storeProduct.storeProductMeta?.additionalInfo?.fit ?: "regular"),
                ),
            ).plus(
                mapOf(PARAM_SIZES to (sizeHashMap ?: mutableMapOf())),
            ).plus(
                mapOf(
                    PARAM_MODEL_INFO to
                        (storeProduct.storeProductMeta?.additionalInfo?.modelInfo ?: JSONObject.NULL),
                ),
            ).plus(
                mapOf(PARAM_GENDER to userBodyProfile.gender),
            )
    }

    /*private fun createModelInfoParams(): Map<String, Any?> {
        return emptyMap<String, Any?>()
            .plus("height" to userBodyProfile.height)
            .plus("size" to "small")
    }*/

    /**
     * Creates the map that represents the user body data
     */
    fun createBodyDataParams(): Map<String, Any?> {
        var chestValue: Int? = null
        return emptyMap<String, Any?>()
            .plus(
                userBodyProfile.bodyData.map {
                    if (it.name == PARAM_BODY_BUST) {
                        chestValue = it.millimeter
                    }
                    it.name to
                        mutableMapOf(
                            PARAM_BODY_MEASUREMENT_VALUE to it.millimeter,
                            PARAM_BODY_MEASUREMENT_PREDICTED to true,
                        )
                },
            ).plus(
                chestValue
                    ?.let {
                        mapOf(
                            PARAM_BODY_CHEST to
                                mutableMapOf(
                                    PARAM_BODY_MEASUREMENT_VALUE to it,
                                    PARAM_BODY_MEASUREMENT_PREDICTED to true,
                                ),
                        )
                    }.orEmpty(),
            )
    }

    /**
     * Creates the map that represents the store product size info
     */
    fun createItemSizesParams(): Map<String, Any?> =
        emptyMap<String, Any?>()
            .plus(
                storeProduct.sizes.map { productSize ->
                    productSize.name to
                        productSize.measurements.associate { measurement ->
                            measurement.name to measurement.millimeter
                        }
                },
            )

    private companion object {
        const val PARAM_ADDITIONAL_INFO = "additionalInfo"
        const val PARAM_BODY_DATA = "bodyData"
        const val PARAM_ITEM_SIZES = "itemSizesOrig"
        const val PARAM_ITEMS = "items"
        const val PARAM_PRODUCT_TYPE = "productType"
        const val PARAM_USER_GENDER = "userGender"
        const val PARAM_USER_HEIGHT = "userHeight"
        const val PARAM_USER_WEIGHT = "userWeight"
        const val PARAM_EXTERNAL_PRODUCT_ID = "extProductId"

        const val PARAM_BRAND = "brand"
        const val PARAM_FIT = "fit"
        const val PARAM_SIZES = "sizes"
        const val PARAM_MODEL_INFO = "modelInfo"
        const val PARAM_GENDER = "gender"

        const val PARAM_BODY_MEASUREMENT_VALUE = "value"
        const val PARAM_BODY_MEASUREMENT_PREDICTED = "predicted"
        const val PARAM_BODY_BUST = "bust"
        const val PARAM_BODY_CHEST = "chest"
    }
}
