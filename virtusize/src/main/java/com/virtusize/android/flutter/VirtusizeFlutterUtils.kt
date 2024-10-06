package com.virtusize.android.flutter

import android.app.Activity
import android.content.Context
import com.virtusize.android.R
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.SizeRecommendationType
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.remote.BodyProfileRecommendedSize
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.util.VirtusizeUtils
import com.virtusize.android.util.trimI18nText

object VirtusizeFlutterUtils {
    fun openVirtusizeView(
        activity: Activity,
        virtusize: Virtusize?,
        product: VirtusizeProduct,
        messageHandler: VirtusizeMessageHandler,
    ) {
        VirtusizeUtils.openVirtusizeWebView(
            activity,
            virtusize?.params,
            product,
            messageHandler,
        )
    }

    fun getUserProductRecommendedSize(
        selectedRecommendedType: SizeRecommendationType? = null,
        userProducts: List<Product>?,
        storeProduct: Product,
        productTypes: List<ProductType>,
    ): SizeComparisonRecommendedSize? {
        var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
        if (selectedRecommendedType != SizeRecommendationType.Body) {
            userProductRecommendedSize =
                VirtusizeUtils.findBestFitProductSize(
                    userProducts = userProducts,
                    storeProduct = storeProduct,
                    productTypes = productTypes,
                )
        }
        return userProductRecommendedSize
    }

    fun getRecommendationText(
        selectedRecommendedType: SizeRecommendationType? = null,
        storeProduct: Product,
        userProductRecommendedSize: SizeComparisonRecommendedSize?,
        bodyProfileRecommendedSize: BodyProfileRecommendedSize?,
        i18nLocalization: I18nLocalization,
    ): String {
        var userBodyRecommendedSize: String? = null

        if (selectedRecommendedType != SizeRecommendationType.CompareProduct) {
            userBodyRecommendedSize = bodyProfileRecommendedSize?.sizeName
        }

        return storeProduct.getRecommendationText(
            i18nLocalization,
            userProductRecommendedSize,
            userBodyRecommendedSize,
        ).trimI18nText(I18nLocalization.TrimType.MULTIPLELINES)
    }

    fun getPrivacyPolicyLink(
        context: Context,
        language: VirtusizeLanguage?,
    ): String? {
        val configuredContext =
            VirtusizeUtils.getConfiguredContext(
                context,
                language,
            )
        return configuredContext?.getString(R.string.virtusize_privacy_policy_link)
    }
}
