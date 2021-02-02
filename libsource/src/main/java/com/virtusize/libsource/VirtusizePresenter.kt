package com.virtusize.libsource

import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.remote.I18nLocalization
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.ProductType

internal interface VirtusizePresenter{
    fun onProductCheck(productCheck: ProductCheck)

    fun onProductId(productId: Int)

    fun onStoreProduct(storeProduct: Product?)
    fun onProductTypes(productTypes: List<ProductType>?)
    fun onI18nLocalization(i18nLocalization: I18nLocalization?)

    /**
     * Updates the recommendation for InPage
     * @param userProductRecommendedSize
     * @param userBodyRecommendedSize
     */
    fun updateInPageRecommendation(userProductRecommendedSize: SizeComparisonRecommendedSize?, userBodyRecommendedSize: String?)
    /**
     * Shows the error for InPage
     * @param error pass a [VirtusizeError] for the messageHandler
     */
    fun showErrorForInPage(error: VirtusizeError?)

}