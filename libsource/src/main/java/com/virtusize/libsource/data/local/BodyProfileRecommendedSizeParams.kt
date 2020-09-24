package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.UserBodyProfile
import org.json.JSONObject

internal data class BodyProfileRecommendedSizeParams constructor(
    private val storeProduct: Product,
    private val userBodyProfile: UserBodyProfile
) {
    fun paramsToMap(): Map<String, Any> {
        return emptyMap<String, Any>()
            .plus(
                mapOf(PARAM_ADDITIONAL_INFO to createAdditionalInfoParams() )
            )
    }

    fun createAdditionalInfoParams(): Map<String, Any?> {
        val brand = storeProduct.storeProductMeta?.additionalInfo?.brand ?: storeProduct.storeProductMeta?.brand
        val sizeHashMap = storeProduct.storeProductMeta?.additionalInfo?.sizes?.map {
            it.name to it.measurements.map { measurement ->
                measurement.name to measurement.millimeter
            }.toMap()
        }?.toMap()
        val gender = storeProduct.storeProductMeta?.gender // storeProduct.storeProductMeta?.additionalInfo?.gender ?:
        return emptyMap<String, Any?>()
            .plus(
                mapOf(PARAM_BRAND to (brand ?: ""))
            )
            .plus(
                mapOf(PARAM_FIT to (storeProduct.storeProductMeta?.additionalInfo?.fit ?: "regular"))
            )
            .plus(
                mapOf(PARAM_SIZES to (sizeHashMap ?: "{}"))
            )
            .plus(
                mapOf(PARAM_MODEL_INFO to (storeProduct.storeProductMeta?.additionalInfo?.modelInfo ?: "{}"))
            )
            .plus(
                mapOf(PARAM_GENDER to (gender ?: JSONObject.NULL))
            )
    }

    private companion object {
        const val PARAM_ADDITIONAL_INFO = "additional_info"
        const val PARAM_BRAND = "brand"
        const val PARAM_FIT = "fit"
        const val PARAM_SIZES = "sizes"
        const val PARAM_MODEL_INFO = "model_info"
        const val PARAM_GENDER = "gender"
    }
}