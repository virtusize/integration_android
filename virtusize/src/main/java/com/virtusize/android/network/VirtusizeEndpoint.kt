package com.virtusize.android.network

import com.virtusize.android.data.local.VirtusizeEnvironment

/**
 * This enum represents all available Virtusize endpoints
 */
internal enum class VirtusizeEndpoint {
    ProductCheck,
    GetSize,
    VirtusizeWebView,
    ProductMetaDataHints,
    Orders,
    StoreViewApiKey,
    StoreProducts,
    ProductType,
    Sessions,
    User,
    UserProducts,
    UserBodyMeasurements,
    I18N
}

/**
 * This method returns a URL corresponding to the Virtusize endpoint that it is called upon
 * @return the Virtusize Endpoint URL
 */
internal fun VirtusizeEndpoint.getPath(env: VirtusizeEnvironment? = null): String {
    return when (this) {
        VirtusizeEndpoint.ProductCheck -> {
            "/product/check"
        }
        VirtusizeEndpoint.GetSize -> {
            "/ds-functions/size-rec/get-size"
        }
        VirtusizeEndpoint.VirtusizeWebView -> {
            val stgPath = when (env) {
                VirtusizeEnvironment.TESTING, VirtusizeEnvironment.STAGING -> "staging"
                else -> "latest"
            }
            "/a/aoyama/$stgPath/sdk-webview.html"
        }
        VirtusizeEndpoint.ProductMetaDataHints -> {
            "/rest-api/v1/product-meta-data-hints"
        }
        VirtusizeEndpoint.Orders -> {
            "/a/api/v3/orders"
        }
        VirtusizeEndpoint.StoreViewApiKey -> {
            "/a/api/v3/stores/api-key/"
        }
        VirtusizeEndpoint.StoreProducts -> {
            "/a/api/v3/store-products/"
        }
        VirtusizeEndpoint.ProductType -> {
            "/a/api/v3/product-types"
        }
        VirtusizeEndpoint.Sessions -> {
            "/a/api/v3/sessions"
        }
        VirtusizeEndpoint.User -> {
            "/a/api/v3/users/me"
        }
        VirtusizeEndpoint.UserProducts -> {
            "/a/api/v3/user-products"
        }
        VirtusizeEndpoint.UserBodyMeasurements -> {
            "/a/api/v3/user-body-measurements"
        }
        VirtusizeEndpoint.I18N -> {
            "/bundle-payloads/aoyama/"
        }
    }
}
