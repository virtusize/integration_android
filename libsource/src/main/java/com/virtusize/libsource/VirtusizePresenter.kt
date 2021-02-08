package com.virtusize.libsource

import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.remote.ProductCheck

//  An interface to pass the data from the actions of VirtusizeRepository
internal interface VirtusizePresenter {
    fun finishedProductCheck(productCheck: ProductCheck)
    fun onValidProductId(productId: Int)
    fun gotSizeRecommendations(userProductRecommendedSize: SizeComparisonRecommendedSize?, userBodyRecommendedSize: String?)
    fun hasInPageError(error: VirtusizeError?)
}