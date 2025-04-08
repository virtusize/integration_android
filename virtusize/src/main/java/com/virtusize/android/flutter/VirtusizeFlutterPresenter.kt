package com.virtusize.android.flutter

import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.remote.Product

//  An interface to pass the data from the actions of VirtusizeRepository
interface VirtusizeFlutterPresenter {
    fun onValidProductCheck(productWithPCDData: VirtusizeProduct)

    fun gotSizeRecommendations(
        bestUserProduct: Product?,
        recommendationText: String?,
    )

    fun hasInPageError(
        externalProductId: String?,
        error: VirtusizeError?,
    )
}
