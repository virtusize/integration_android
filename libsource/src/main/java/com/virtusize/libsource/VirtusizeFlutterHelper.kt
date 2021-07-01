package com.virtusize.libsource

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.local.SizeRecommendationType
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.ui.VirtusizeWebViewFragment
import com.virtusize.libsource.util.Constants
import com.virtusize.libsource.util.VirtusizeUtils

class VirtusizeFlutterHelper(private val context: Context) {
    private var virtusizeDialogFragment: VirtusizeWebViewFragment = VirtusizeWebViewFragment()

    fun openVirtusizeView(
        virtusize: Virtusize?,
        product: VirtusizeProduct,
        productDataCheck: ProductCheck,
        messageHandler: VirtusizeMessageHandler
    ) {
        val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
        val previousFragment = context.supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
        previousFragment?.let {fragment ->
            fragmentTransaction.remove(fragment)
        }
        fragmentTransaction.addToBackStack(null)
        val args = Bundle()
        args.putString(Constants.URL_KEY, VirtusizeApi.virtusizeWebViewURL())
        virtusize?.params?.let { params ->
            params.virtusizeProduct = product
            params.virtusizeProduct?.productCheckData = productDataCheck
            args.putString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY, "javascript:vsParamsFromSDK(${params.vsParamsString()})")
        }
        virtusizeDialogFragment.arguments = args
        virtusizeDialogFragment.setupMessageHandler(messageHandler)
        virtusizeDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
    }

    fun getRecommendationText(
        selectedRecommendedType: SizeRecommendationType? = null,
        userProducts: List<Product>?,
        storeProduct: Product,
        productTypes: List<ProductType>,
        bodyProfileRecommendedSize: BodyProfileRecommendedSize?,
        i18nLocalization: I18nLocalization
    ): String {
        var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
        var userBodyRecommendedSize: String? = null

        if(selectedRecommendedType != SizeRecommendationType.body) {
            userProductRecommendedSize = VirtusizeUtils.findBestFitProductSize(
                userProducts = userProducts,
                storeProduct = storeProduct,
                productTypes = productTypes
            )
        }

        if(selectedRecommendedType != SizeRecommendationType.compareProduct) {
            userBodyRecommendedSize = bodyProfileRecommendedSize?.sizeName
        }

        return storeProduct.getRecommendationText(
            i18nLocalization,
            userProductRecommendedSize,
            userBodyRecommendedSize
        )
    }
}