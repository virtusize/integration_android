package com.virtusize.android.data.local

import android.os.Parcelable
import com.virtusize.android.data.remote.ProductCheck
import kotlinx.android.parcel.Parcelize

/**
 * This class represents a VirtusizeProduct object.
 * You need to pass in externalId
 * @param externalId the ID that will be used to reference this product in Virtusize API
 * @param imageUrl the image URL of this product, in order to populate the comparison view
 * @param productCheckData the product check response from Virtusize API
 */
@Parcelize
data class VirtusizeProduct @JvmOverloads constructor(
    val externalId: String,
    var imageUrl: String? = null,
    var productCheckData: ProductCheck? = null
) : Parcelable
