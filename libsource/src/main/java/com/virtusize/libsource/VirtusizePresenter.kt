package com.virtusize.libsource

import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.local.VirtusizeProduct

//  An interface to pass the data from the actions of VirtusizeRepository
internal interface VirtusizePresenter {
    fun onValidProductDataCheck(productWithPDCData: VirtusizeProduct)
    fun gotSizeRecommendations(
        externalProductId: String,
        userProductRecommendedSize: SizeComparisonRecommendedSize?,
        userBodyRecommendedSize: String?
    )
    fun hasInPageError(externalProductId: String?, error: VirtusizeError?)
}
