package com.virtusize.android.data.remote

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This class represents the response for the data field of ProductCheckResponse
 * @see ProductCheck
 */
@Parcelize
data class Data(
    val validProduct: Boolean,
    val fetchMetaData: Boolean,
    val shouldSeePhTooltip: Boolean,
    val productDataId: Int,
    val productTypeName: String,
    val storeName: String,
    val storeId: Int,
    val productTypeId: Int
) : Parcelable
