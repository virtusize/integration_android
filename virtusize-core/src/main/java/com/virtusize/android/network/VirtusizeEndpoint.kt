package com.virtusize.android.network

/**
 * This sealed interface represents all available Virtusize endpoints
 */
sealed interface VirtusizeEndpoint {
    val path: String

    data object ProductCheck : VirtusizeEndpoint {
        override val path: String = "/product/check"
    }

    data object GetSize : VirtusizeEndpoint {
        override val path: String = "/item"
    }

    data object LatestAoyamaVersion : VirtusizeEndpoint {
        override val path: String = "/a/aoyama/latest.txt"
    }

    data class VirtusizeWebView(val version: String) : VirtusizeEndpoint {
        override val path: String = "/a/aoyama/$version/sdk-webview.html"
    }

    data object VirtusizeWebViewForSpecificClients : VirtusizeEndpoint {
        override val path: String = "/a/aoyama/testing/privacy-policy-phase2-vue/sdk-webview.html"
    }

    data object ProductMetaDataHints : VirtusizeEndpoint {
        override val path: String = "/rest-api/v1/product-meta-data-hints"
    }

    data object Orders : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/orders"
    }

    data object StoreViewApiKey : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/stores/api-key/"
    }

    data object StoreProducts : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/store-products/"
    }

    data object ProductType : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/product-types"
    }

    data object Sessions : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/sessions"
    }

    data object User : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/users/me"
    }

    data object UserProducts : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/user-products"
    }

    data object UserBodyMeasurements : VirtusizeEndpoint {
        override val path: String = "/a/api/v3/user-body-measurements"
    }

    data object I18N : VirtusizeEndpoint {
        override val path: String = "/bundle-payloads/aoyama/"
    }
}
