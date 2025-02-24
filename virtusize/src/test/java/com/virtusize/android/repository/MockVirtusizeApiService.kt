package com.virtusize.android.repository

import android.content.Context
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.remote.Store
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.network.VirtusizeApiResponse
import org.json.JSONObject

internal class MockVirtusizeApiService(context: Context) : VirtusizeAPIService(context, null) {
    internal var mockI18n: ((VirtusizeLanguage?) -> JSONObject)? = null
    internal var mockStoreSpecificI18n: ((String) -> JSONObject?)? = null
    internal var mockStoreInfo: (() -> Store)? = null

    override suspend fun getStoreInfo() = mockStoreInfo?.let { VirtusizeApiResponse.Success(it()) } ?: super.getStoreInfo()

    override suspend fun getI18n(language: VirtusizeLanguage?) =
        mockI18n?.let { VirtusizeApiResponse.Success(it(language)) } ?: super.getI18n(language)

    override suspend fun getStoreSpecificI18n(storeName: String) =
        mockStoreSpecificI18n?.let {
            when (val specificI18n = it(storeName)) {
                null -> VirtusizeApiResponse.Error(VirtusizeError(message = "error"))
                else -> VirtusizeApiResponse.Success(specificI18n)
            }
        } ?: super.getStoreSpecificI18n(storeName)
}
