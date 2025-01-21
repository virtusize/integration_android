package com.virtusize.android.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.virtusize.android.data.local.ProductComparisonFitInfo
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.VirtusizeWebViewInMemoryCache
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductSize
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.Weight
import com.virtusize.android.ui.VirtusizeWebViewActivity
import kotlin.math.abs

// The object that wraps Virtusize utility functions
internal object VirtusizeUtils {
    /**
     * Finds the size of the best fit product by comparing user products with the store product
     * @param userProducts the list of user products
     * @param storeProduct the store product
     * @param productTypes the list of available product types
     * @return [SizeComparisonRecommendedSize]
     */
    fun findBestFitProductSize(
        userProducts: List<Product>?,
        storeProduct: Product?,
        productTypes: List<ProductType>?,
    ): SizeComparisonRecommendedSize? {
        if (userProducts == null || storeProduct == null || productTypes == null) {
            return null
        }
        val storeProductType =
            productTypes.find { it.id == storeProduct.productType } ?: return null
        val compatibleUserProducts =
            userProducts.filter { it.productType in storeProductType.compatibleTypes }
        val sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()

        compatibleUserProducts.iterator().forEach { userProduct ->
            val userProductSize = userProduct.sizes[0]
            storeProduct.sizes.iterator().forEach { storeProductSize ->
                val productComparisonFitInfo =
                    getProductComparisonFitInfo(
                        userProductSize,
                        storeProductSize,
                        storeProductType.weights,
                    )
                if (
                    productComparisonFitInfo.fitScore > sizeComparisonRecommendedSize.bestFitScore
                ) {
                    sizeComparisonRecommendedSize.apply {
                        productComparisonFitInfo.apply {
                            bestFitScore = fitScore
                            bestStoreProductSize = storeProductSize
                            bestUserProduct = userProduct
                            isStoreProductSmaller = isSmaller
                        }
                    }
                }
            }
        }
        return sizeComparisonRecommendedSize
    }

    /**
     * Gets the product comparison fit info
     * @param userProductSize the size of a user product
     * @param storeProductSize the size of a store product
     * @param storeProductTypeScoreWeights the weights of the store product for calculation
     * @return [ProductComparisonFitInfo]
     */
    fun getProductComparisonFitInfo(
        userProductSize: ProductSize,
        storeProductSize: ProductSize,
        storeProductTypeScoreWeights: Set<Weight>,
    ): ProductComparisonFitInfo {
        var rawScore = 0f
        var isSmaller: Boolean? = null

        val sortedStoreProductTypeScoreWeights =
            storeProductTypeScoreWeights.sortedByDescending { it.value }
        sortedStoreProductTypeScoreWeights.forEach { weight ->
            val userProductSizeMeasurement =
                userProductSize.measurements.find { it.name == weight.factor }?.millimeter
            val storeProductSizeMeasurement =
                storeProductSize.measurements.find { it.name == weight.factor }?.millimeter
            if (userProductSizeMeasurement != null && storeProductSizeMeasurement != null) {
                rawScore +=
                    abs(
                        weight.value * (userProductSizeMeasurement - storeProductSizeMeasurement),
                    )
                isSmaller =
                    isSmaller ?: (userProductSizeMeasurement - storeProductSizeMeasurement > 0)
            }
        }

        val adjustScore = rawScore / 10
        val fitScore = (100 - adjustScore).coerceAtLeast(20f)

        return ProductComparisonFitInfo(fitScore, isSmaller)
    }

    /**
     * A function to open the Virtusize WebView
     */
    fun openVirtusizeWebView(
        context: Context,
        virtusizeParams: VirtusizeParams?,
        product: VirtusizeProduct,
        messageHandler: VirtusizeMessageHandler,
    ) {
        VirtusizeWebViewInMemoryCache.setupMessageHandler(messageHandler)
        val intent =
            Intent(context, VirtusizeWebViewActivity::class.java).apply {
                putExtras(createBundle(virtusizeParams, product))
            }
        context.startActivity(intent)
    }

    private fun createBundle(
        virtusizeParams: VirtusizeParams?,
        product: VirtusizeProduct,
    ): Bundle =
        Bundle().apply {
            virtusizeParams?.let { params ->
                putString(
                    Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY,
                    "javascript:vsParamsFromSDK(${params.vsParamsString(product)})",
                )
                putBoolean(Constants.VIRTUSIZE_SHOW_SNS_BUTTONS, virtusizeParams.showSNSButtons)
            }
            putParcelable(Constants.VIRTUSIZE_PRODUCT_KEY, product)
        }
}
