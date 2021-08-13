package com.virtusize.libsource.flutter

import android.app.Activity
import android.content.Context
import com.virtusize.libsource.R
import com.virtusize.libsource.Virtusize
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.parsers.I18nLocalizationJsonParser
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.ui.VirtusizeWebViewFragment
import com.virtusize.libsource.util.VirtusizeUtils
import com.virtusize.libsource.util.trimI18nText

object VirtusizeFlutterUtils {

    fun openVirtusizeView(
        activity: Activity,
        virtusize: Virtusize?,
        product: VirtusizeProduct,
        messageHandler: VirtusizeMessageHandler
    ) {
        VirtusizeUtils.openVirtusizeWebView(
            activity,
            virtusize?.params,
            VirtusizeWebViewFragment(),
            product,
            messageHandler
        )
    }

    fun getUserProductRecommendedSize(
        selectedRecommendedType: SizeRecommendationType? = null,
        userProducts: List<Product>?,
        storeProduct: Product,
        productTypes: List<ProductType>
    ): SizeComparisonRecommendedSize? {
        var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
        if(selectedRecommendedType != SizeRecommendationType.body) {
            userProductRecommendedSize = VirtusizeUtils.findBestFitProductSize(
                userProducts = userProducts,
                storeProduct = storeProduct,
                productTypes = productTypes
            )
        }
        return userProductRecommendedSize
    }

    fun getRecommendationText(
        selectedRecommendedType: SizeRecommendationType? = null,
        storeProduct: Product,
        userProductRecommendedSize: SizeComparisonRecommendedSize?,
        bodyProfileRecommendedSize: BodyProfileRecommendedSize?,
        i18nLocalization: I18nLocalization
    ): String {
        var userBodyRecommendedSize: String? = null

        if(selectedRecommendedType != SizeRecommendationType.compareProduct) {
            userBodyRecommendedSize = bodyProfileRecommendedSize?.sizeName
        }

        return storeProduct.getRecommendationText(
            i18nLocalization,
            userProductRecommendedSize,
            userBodyRecommendedSize
        ).trimI18nText(I18nLocalizationJsonParser.TrimType.MULTIPLELINES)
    }

    fun getPrivacyPolicyLink(context: Context, language: VirtusizeLanguage?): String? {
        val configuredContext = VirtusizeUtils.getConfiguredContext(
            context,
            language
        )
        return configuredContext?.getString(R.string.virtusize_privacy_policy_link)
    }
}