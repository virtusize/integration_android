package com.virtusize.libsource.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.fragment.app.FragmentActivity
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.ProductComparisonFitInfo
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.ui.VirtusizeWebViewFragment
import java.util.*
import kotlin.math.abs

// The object that wraps Virtusize utility functions
internal object VirtusizeUtils {

    // The context wrapper that is configured to a designated locale
    class ConfiguredContext(base: Context?) : ContextWrapper(base)

    /**
     * Gets the ContextWrapper that is switched to a designated locale
     * @param context the base application Context
     * @param locale the locale to switch to
     */
    private fun configureLocale(context: Context, locale: Locale?): ContextWrapper? {
        var updatedContext = context
        val resources = context.resources
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            configuration.locale = locale
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            updatedContext = context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return ConfiguredContext(updatedContext)
    }

    /**
     * Gets configured context base on the language that clients set up with the Virtusize Builder in the application
     */
    fun getConfiguredContext(context: Context, language: VirtusizeLanguage?): ContextWrapper? {
        return when (language) {
            VirtusizeLanguage.EN -> configureLocale(context, Locale.ENGLISH)
            VirtusizeLanguage.JP -> configureLocale(context, Locale.JAPAN)
            VirtusizeLanguage.KR -> configureLocale(context, Locale.KOREA)
            else -> configureLocale(context, Locale.getDefault())
        }
    }

    /**
     * Finds the size of the best fit product by comparing user products with the store product
     * @param userProducts the list of user products
     * @param storeProduct the store product
     * @param productTypes the list of available product types
     * @return [SizeComparisonRecommendedSize]
     */
    fun findBestFitProductSize(userProducts: List<Product>?, storeProduct: Product?, productTypes: List<ProductType>?): SizeComparisonRecommendedSize? {
        if (userProducts == null || storeProduct == null || productTypes == null) {
            return null
        }
        val storeProductType = productTypes.find { it.id == storeProduct.productType } ?: return null
        val compatibleUserProducts = userProducts.filter { it.productType in storeProductType.compatibleTypes }
        val sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()

        compatibleUserProducts.forEach { userProduct ->
            val userProductSize = userProduct.sizes[0]
            storeProduct.sizes.forEach { storeProductSize ->
                val productComparisonFitInfo = getProductComparisonFitInfo(
                    userProductSize,
                    storeProductSize,
                    storeProductType.weights
                )
                if (productComparisonFitInfo.fitScore > sizeComparisonRecommendedSize.bestFitScore) {
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
        storeProductTypeScoreWeights: Set<Weight>
    ): ProductComparisonFitInfo {
        var rawScore = 0f
        var isSmaller: Boolean? = null

        val sortedStoreProductTypeScoreWeights = storeProductTypeScoreWeights.sortedByDescending { it.value }
        sortedStoreProductTypeScoreWeights.forEach { weight ->
            val userProductSizeMeasurement = userProductSize.measurements.find { it.name == weight.factor }?.millimeter
            val storeProductSizeMeasurement = storeProductSize.measurements.find { it.name == weight.factor }?.millimeter
            if (userProductSizeMeasurement != null && storeProductSizeMeasurement != null) {
                rawScore += abs(weight.value * (userProductSizeMeasurement - storeProductSizeMeasurement))
                isSmaller = isSmaller ?: (userProductSizeMeasurement - storeProductSizeMeasurement > 0)
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
        virtusizeDialogFragment: VirtusizeWebViewFragment,
        product: VirtusizeProduct,
        messageHandler: VirtusizeMessageHandler
    ) {
        val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
        val previousFragment = context.supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
        previousFragment?.let { fragment ->
            fragmentTransaction.remove(fragment)
        }
        fragmentTransaction.addToBackStack(null)
        val args = Bundle()
        args.putString(Constants.URL_KEY, VirtusizeApi.virtusizeWebViewURL())
        virtusizeParams?.let { params ->
            args.putString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY, "javascript:vsParamsFromSDK(${params.vsParamsString(product)})")
        }
        args.putParcelable(Constants.VIRTUSIZE_PRODUCT_KEY, product)
        virtusizeDialogFragment.arguments = args
        virtusizeDialogFragment.setupMessageHandler(messageHandler)
        virtusizeDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
    }
}
