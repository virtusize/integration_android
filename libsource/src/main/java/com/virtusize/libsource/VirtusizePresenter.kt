package com.virtusize.libsource

import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.remote.I18nLocalization
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.ProductType

//  An interface to pass the data from the actions of VirtusizeRepository
internal interface VirtusizePresenter {
    fun finishedProductCheck(productCheck: ProductCheck)
    fun onValidProductId(productId: Int)
    fun gotInitialData(storeProduct: Product, productTypes: List<ProductType>, i18nLocalization: I18nLocalization)
    fun gotSizeRecommendations(userProductRecommendedSize: SizeComparisonRecommendedSize?, userBodyRecommendedSize: String?)
    fun hasInPageError(error: VirtusizeError?)
}