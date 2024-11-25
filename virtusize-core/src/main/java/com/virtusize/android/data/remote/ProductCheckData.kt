package com.virtusize.android.data.remote

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This class represents the response for the API request ProductCheck
 */
@Parcelize
data class ProductCheckData(
    val data: Data?,
    val productId: String,
    val name: String,
    // the JSON response as a String (For the Flutter SDK)
    val jsonString: String?,
) : Parcelable
