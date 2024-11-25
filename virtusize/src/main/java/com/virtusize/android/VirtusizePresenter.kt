package com.virtusize.android

import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeProduct

//  An interface to pass the data from the actions of VirtusizeRepository
internal interface VirtusizePresenter {
    fun onValidProductCheck(productWithPCDData: VirtusizeProduct)

    fun gotSizeRecommendations(
        externalProductId: String,
        userProductRecommendedSize: SizeComparisonRecommendedSize?,
        userBodyRecommendedSize: String?,
    )

    fun hasInPageError(
        externalProductId: String?,
        error: VirtusizeError?,
    )
}
