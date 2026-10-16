package com.virtusize.android.data.local

import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.UserBodyProfile
import com.virtusize.android.util.sortedLikeJavaScriptObjectKeys
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
internal data class BodyProfileRecommendedSizeParams(
    private val productTypes: List<ProductType>,
    private val storeProduct: Product,
    private val userBodyProfile: UserBodyProfile,
) {
    /**
     * Returns the map that represents the API request body
     * @return the name of the event
     */
    fun paramsToMap(): Map<String, Any> {
        return emptyMap<String, Any>()
            .plus(
                mapOf("app_origin" to APP_ORIGIN_ANDROID),
            )
            .plus(
                mapOf(PARAM_BODY_DATA to createBodyDataParams()),
            )
            .plus(
                mapOf(PARAM_USER_GENDER to userBodyProfile.gender),
            )
            .plus(
                mapOf(PARAM_USER_HEIGHT to userBodyProfile.height),
            )
            .plus(
                // The web widget sends a JSON number (`56`), not the raw API string (`"56.00"`)
                numericWeight()?.let { mapOf(PARAM_USER_WEIGHT to it) }.orEmpty(),
            )
            .plus(
                mapOf(PARAM_USER_AGE to userBodyProfile.age),
            )
            .plus(
                mapOf(PARAM_ITEMS to arrayOf(createItemsParams())),
            )
    }

    fun paramsToMapShoe(): Map<String, Any> {
        return emptyMap<String, Any>()
            .plus(
                mapOf(PARAM_BODY_DATA to createBodyDataParams()),
            )
            .plus(
                mapOf(PARAM_USER_GENDER to userBodyProfile.gender),
            )
            .plus(
                mapOf(PARAM_USER_HEIGHT to userBodyProfile.height),
            )
            .plus(
                // Send the raw API string (e.g. "56.00") to match the web widget's payload
                userBodyProfile.weight.takeIf { it.toFloatOrNull() != null }
                    ?.let { mapOf(PARAM_USER_WEIGHT to it) }
                    .orEmpty(),
            )
            .plus(
                mapOf(PARAM_USER_AGE to userBodyProfile.age),
            )
            .plus(
                createItemsParams(),
            )
            .plus(
                mapOf(PARAM_FOOTWEAR_DATA to userBodyProfile.footwearData),
            )
            .plus(
                mapOf(PARAM_ADDITIONAL_INFO to createAdditionalInfoParams()),
            )
            .plus(
                mapOf(PARAM_PRODUCT_NAME to storeProduct.name),
            )
    }

    /**
     * Returns the map that represents the kids size recommendation API request body.
     * Matches the Aoyama widget payload for `/kid`.
     *
     * The `/kid` API result depends on the order of `sizeNames` and of the `size_measurements` keys,
     * so both are emitted in the order the web widget sends (see [sortedLikeJavaScriptObjectKeys]).
     * All maps are insertion-ordered, which [org.json.JSONObject] preserves on Android.
     */
    fun paramsToMapKid(): Map<String, Any> {
        val productTypeName = productTypes.find { it.id == storeProduct.productType }?.name ?: ""
        val gender =
            storeProduct.storeProductMeta?.additionalInfo?.gender
                ?: storeProduct.storeProductMeta?.gender
                ?: ""
        val brand =
            storeProduct.storeProductMeta?.additionalInfo?.brand
                ?: storeProduct.storeProductMeta?.brand
                ?: ""

        // The web widget sends `additionalInfo.sizes` as-is (e.g. only height/bust/sleeve),
        // not the full product size list, and omits it when `itemMeasurements` is false
        val additionalInfo = storeProduct.storeProductMeta?.additionalInfo
        val additionalInfoSizes =
            additionalInfo?.sizes
                ?.takeIf { additionalInfo.itemMeasurements != false }
                ?.associate { size -> size.name to size.measurements.associate { it.name to it.millimeter } }

        val productSizeNames = storeProduct.sizes.map { it.name }.filter { it.isNotEmpty() }
        val sizeNames =
            productSizeNames
                .ifEmpty { additionalInfoSizes?.keys?.sorted() ?: emptyList() }
                .sortedLikeJavaScriptObjectKeys()
        val sizeMeasurements =
            additionalInfoSizes?.let { sizes ->
                // Sizes known to the product first, in the web widget's order, then any extra keys
                val extraNames = sizes.keys.filter { it !in sizeNames }.sorted()
                (sizeNames + extraNames).mapNotNull { name -> sizes[name]?.let { name to it } }.toMap()
            }
        val weight = userBodyProfile.weight.toFloatOrNull()?.let { Math.round(it) }

        val productParams =
            linkedMapOf<String, Any>(
                PARAM_KID_BRAND to brand,
                PARAM_GENDER to gender,
                PARAM_KID_PRODUCT_TYPE to productTypeName,
                PARAM_KID_SIZE_NAMES to sizeNames,
            )
        sizeMeasurements?.let { productParams[PARAM_KID_SIZE_MEASUREMENTS] = it }

        val userParams =
            linkedMapOf<String, Any>(
                PARAM_GENDER to userBodyProfile.gender,
                PARAM_KID_USER_HEIGHT to userBodyProfile.height,
            )
        weight?.let { userParams[PARAM_KID_USER_WEIGHT] = it }
        userParams[PARAM_KID_USER_AGE] = userBodyProfile.age
        userParams[PARAM_KID_BODY_DATA] = createKidBodyDataParams()

        return linkedMapOf(
            PARAM_KID_PRODUCT to productParams,
            PARAM_KID_USER to userParams,
            PARAM_EXTERNAL_PRODUCT_ID to (storeProduct.externalId ?: ""),
        )
    }

    /**
     * Creates the map that represents the kid's body data for the `/kid` payload.
     * Unlike the `/item` payload, the web widget sends the measurement names as they come
     * from the predict API (camelCase, e.g. "hipWidth") and does not add the "chest" alias.
     */
    private fun createKidBodyDataParams(): Map<String, Any> =
        userBodyProfile.bodyData
            .sortedBy { it.name }
            .associate {
                it.name to
                    linkedMapOf(
                        PARAM_BODY_MEASUREMENT_VALUE to it.millimeter,
                        PARAM_BODY_MEASUREMENT_PREDICTED to true,
                    )
            }

    private fun createItemsParams(): Map<String, Any> {
        val clientSizeOrder =
            storeProduct.sizes
                .map { it.name }
                .filter { it.isNotEmpty() }
                .sortedLikeJavaScriptObjectKeys()
        val additionalInfo = createAdditionalInfoParams().toMutableMap()
        // The web widget puts fit_adjust on the item, and sends the size-chart order
        // inside additional_info. A tied fit score follows that order, including names
        // such as J/XS that JavaScript does not sort numerically.
        additionalInfo.remove(PARAM_FIT_ADJUST)
        additionalInfo[PARAM_CLIENT_SIZE_ORDER] = clientSizeOrder
        (additionalInfo[PARAM_SIZES] as? Map<*, *>)?.let { sizes ->
            val typed = linkedMapOf<String, Any?>()
            sizes.forEach { (key, value) ->
                if (key is String) typed[key] = value
            }
            additionalInfo[PARAM_SIZES] = typed.orderedByClientSize(clientSizeOrder)
        }
        return linkedMapOf(
            PARAM_ITEM_SIZES to createItemSizesParams().orderedByClientSize(clientSizeOrder),
            PARAM_PRODUCT_TYPE to
                toSnakeCase(productTypes.find { it.id == storeProduct.productType }?.name ?: ""),
            PARAM_ADDITIONAL_INFO to additionalInfo,
            PARAM_EXTERNAL_PRODUCT_ID to (storeProduct.externalId ?: ""),
            PARAM_FIT_ADJUST to JSONObject.NULL,
        )
    }

    /** Whole numbers stay integers (`56`), fractional weights stay decimals (`56.5`). */
    private fun numericWeight(): Number? {
        val weight = userBodyProfile.weight.toDoubleOrNull() ?: return null
        val whole = weight.toInt()
        return if (weight == whole.toDouble()) whole else weight
    }

    /**
     * Creates the map that represents the store product additional info
     */
    fun createAdditionalInfoParams(): Map<String, Any?> {
        val brand =
            storeProduct.storeProductMeta?.additionalInfo?.brand
                ?: storeProduct.storeProductMeta?.brand
        val sizeHashMap =
            storeProduct.storeProductMeta?.additionalInfo?.sizes
                ?.associate {
                    it.name to
                        it.measurements.associate { measurement ->
                            // Store meta uses camelCase (`frontRise`); the web widget sends `front_rise`
                            toSnakeCase(measurement.name) to measurement.millimeter
                        }
                }
                ?.withJavaScriptKeyOrder()
        val modelInfo =
            storeProduct.storeProductMeta?.additionalInfo?.modelInfo
                ?.mapKeys { (key, _) -> toSnakeCase(key) } ?: JSONObject.NULL

        return emptyMap<String, Any?>()
            .plus(
                mapOf(PARAM_BRAND to (brand ?: "")),
            )
            .plus(
                mapOf(
                    PARAM_FIT to (storeProduct.storeProductMeta?.additionalInfo?.fit ?: "regular"),
                ),
            )
            .plus(
                mapOf(PARAM_SIZES to (sizeHashMap ?: mutableMapOf())),
            )
            .plus(
                mapOf(PARAM_MODEL_INFO to modelInfo),
            )
            .plus(
                mapOf(PARAM_GENDER to (storeProduct.storeProductMeta?.additionalInfo?.gender ?: "male")),
            )
            .plus(
                mapOf(
                    PARAM_STYLE to (storeProduct.storeProductMeta?.additionalInfo?.style ?: "regular"),
                ),
            )
            // The web widget sends these two keys with every request
            .plus(
                mapOf(PARAM_ITEM_MEASUREMENTS to storeProduct.sizes.isNotEmpty()),
            )
            .plus(
                mapOf(PARAM_FIT_ADJUST to JSONObject.NULL),
            )
    }

    /**
     * Creates the map that represents the user body data
     */
    fun createBodyDataParams(): Map<String, Any?> {
        var chestValue: Int? = null
        val bodyData =
            emptyMap<String, Any?>()
                .plus(
                    userBodyProfile.bodyData.associate {
                        if (it.name == PARAM_BODY_BUST) {
                            chestValue = it.millimeter
                        }
                        it.name to
                            mutableMapOf(
                                PARAM_BODY_MEASUREMENT_VALUE to aoyamaMillimeters(it.name, it.millimeter),
                                PARAM_BODY_MEASUREMENT_PREDICTED to true,
                            )
                    },
                )
                .plus(
                    chestValue?.let {
                        mapOf(
                            PARAM_BODY_CHEST to
                                mutableMapOf(
                                    PARAM_BODY_MEASUREMENT_VALUE to aoyamaMillimeters(PARAM_BODY_CHEST, it),
                                    PARAM_BODY_MEASUREMENT_PREDICTED to true,
                                ),
                        )
                    }.orEmpty(),
                )
                .plus(
                    createBraSizeParams(),
                )
                // Convert all keys in the map to snake_case. Keys the profile adds later stay in the payload.
                .mapKeys { (key, _) -> toSnakeCase(key) }
                .toMutableMap()
        // Aoyama always sends these, including when the profile has no bra size
        putBraDefault(bodyData, "bra_country", "")
        putBraDefault(bodyData, "bra_cup", "")
        putBraDefault(bodyData, "bra_band", 0)
        return bodyData
    }

    /**
     * Aoyama divides every measurement by 10 in `parseBodyMeasurements`. These names are also
     * `Math.round`ed, which is nearest 10 mm. Every other measurement is sent unchanged.
     */
    private fun aoyamaMillimeters(
        name: String,
        millimeters: Int,
    ): Int {
        return if (toSnakeCase(name) in AOYAMA_ROUNDED_BODY_MEASUREMENTS) {
            (millimeters + 5) / 10 * 10
        } else {
            millimeters
        }
    }

    private fun putBraDefault(
        bodyData: MutableMap<String, Any?>,
        key: String,
        value: Any,
    ) {
        if (key !in bodyData) {
            bodyData[key] =
                mutableMapOf(
                    PARAM_BODY_MEASUREMENT_VALUE to value,
                    PARAM_BODY_MEASUREMENT_PREDICTED to false,
                )
        }
    }

    /**
     * Creates the map that represents the bra size data in body_data format
     * Matches iOS implementation: multiplies band value by 10, prefixes keys with "bra_"
     */
    private fun createBraSizeParams(): Map<String, Any> {
        val braSize = userBodyProfile.braSize ?: return emptyMap()
        val braSizeParams = mutableMapOf<String, Any>()

        for ((key, value) in braSize) {
            val snakeKey = toSnakeCase(key)
            if (snakeKey == "band") {
                // Multiply band value by 10, matching iOS implementation
                val bandValue =
                    when (value) {
                        is Int -> value * 10
                        is Number -> value.toInt() * 10
                        else -> value
                    }
                braSizeParams["bra_$snakeKey"] =
                    mutableMapOf(
                        PARAM_BODY_MEASUREMENT_VALUE to bandValue,
                        PARAM_BODY_MEASUREMENT_PREDICTED to false,
                    )
            } else {
                braSizeParams["bra_$snakeKey"] =
                    mutableMapOf(
                        PARAM_BODY_MEASUREMENT_VALUE to value,
                        PARAM_BODY_MEASUREMENT_PREDICTED to false,
                    )
            }
        }

        return braSizeParams
    }

    /**
     * Creates the map that represents the store product size info
     */
    fun createItemSizesParams(): Map<String, Any?> {
        return storeProduct.sizes
            .associate { productSize ->
                productSize.name to
                    productSize.measurements.associate { measurement ->
                        // Measurement names arrive camelCase from the store-products API
                        // (e.g. "bustRound"), but the get-size API expects the web widget's
                        // snake_case names ("bust_round")
                        toSnakeCase(measurement.name) to measurement.millimeter
                    }
            }
            .withJavaScriptKeyOrder()
    }

    /** The size API walks these objects in key order when two sizes share a fit score. */
    private fun <Value> Map<String, Value>.withJavaScriptKeyOrder(): Map<String, Value> {
        return keys.toList().sortedLikeJavaScriptObjectKeys().associateWith { getValue(it) }
    }

    /**
     * Emits size names in [clientOrder]. `J/XS` matches `J/XS` and also `XS`.
     * Names that are not in the chart stay in JavaScript object-key order.
     */
    private fun <Value> Map<String, Value>.orderedByClientSize(clientOrder: List<String>): Map<String, Value> {
        val remaining = keys.toMutableSet()
        val ordered = mutableListOf<String>()
        for (name in clientOrder) {
            val match = bestSizeKey(remaining, name) ?: continue
            ordered.add(match)
            remaining.remove(match)
        }
        ordered.addAll(remaining.toList().sortedLikeJavaScriptObjectKeys())
        return ordered.associateWith { getValue(it) }
    }

    private fun bestSizeKey(
        keys: Set<String>,
        clientName: String,
    ): String? {
        if (clientName in keys) return clientName
        return keys
            .filter { clientName.endsWith("/$it") || clientName.endsWith(it) }
            .maxByOrNull { it.length }
    }

    private companion object {
        const val PARAM_ADDITIONAL_INFO = "additional_info"
        const val PARAM_BODY_DATA = "body_data"
        const val PARAM_ITEM_SIZES = "item_sizes_orig"
        const val PARAM_ITEMS = "items"
        const val PARAM_PRODUCT_TYPE = "product_type"
        const val PARAM_USER_GENDER = "user_gender"
        const val PARAM_USER_HEIGHT = "user_height"
        const val PARAM_USER_WEIGHT = "user_weight"
        const val PARAM_USER_AGE = "user_age"
        const val PARAM_EXTERNAL_PRODUCT_ID = "ext_product_id"
        const val PARAM_FOOTWEAR_DATA = "footwear_data"
        const val PARAM_PRODUCT_NAME = "product_name"

        const val PARAM_KID_PRODUCT = "product"
        const val PARAM_KID_USER = "user"
        const val PARAM_KID_BRAND = "brand"
        const val PARAM_KID_PRODUCT_TYPE = "productType"
        const val PARAM_KID_SIZE_NAMES = "sizeNames"
        const val PARAM_KID_SIZE_MEASUREMENTS = "size_measurements"
        const val PARAM_KID_USER_HEIGHT = "height"
        const val PARAM_KID_USER_WEIGHT = "weight"
        const val PARAM_KID_USER_AGE = "age"
        const val PARAM_KID_BODY_DATA = "bodyData"

        const val PARAM_BRAND = "brand"
        const val PARAM_FIT = "fit"
        const val PARAM_ITEM_MEASUREMENTS = "item_measurements"
        const val PARAM_FIT_ADJUST = "fit_adjust"
        const val PARAM_CLIENT_SIZE_ORDER = "client_size_order"
        const val PARAM_SIZES = "sizes"
        const val PARAM_MODEL_INFO = "model_info"
        const val PARAM_GENDER = "gender"
        const val PARAM_STYLE = "style"

        const val PARAM_BODY_MEASUREMENT_VALUE = "value"
        const val PARAM_BODY_MEASUREMENT_PREDICTED = "predicted"
        const val PARAM_BODY_BUST = "bust"
        const val PARAM_BODY_CHEST = "chest"
        val AOYAMA_ROUNDED_BODY_MEASUREMENTS =
            setOf("shoulder", "bicep", "chest", "waist", "hip", "thigh", "inseam")

        const val APP_ORIGIN_ANDROID = 2
    }

    /**
     * Converts a camelCase or PascalCase string to snake_case
     */
    private fun toSnakeCase(input: String): String {
        return input.replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("([A-Z])([A-Z][a-z])"), "$1_$2")
            .lowercase()
    }
}
