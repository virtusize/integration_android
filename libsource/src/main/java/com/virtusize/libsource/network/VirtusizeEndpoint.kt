package com.virtusize.libsource.network

/**
 * This enum represents all available Virtusize endpoints
 */
internal enum class VirtusizeEndpoint {
    ProductCheck,
    FitIllustrator,
    ProductMetaDataHints,
    Events,
    Orders,
    StoreViewApiKey
}

/**
 * This method returns a URL corresponding to the Virtusize endpoint that it is called upon
 * @return the Virtusize Endpoint URL
 */
internal fun VirtusizeEndpoint.getUrl(): String {
     return when(this) {
        VirtusizeEndpoint.ProductCheck -> {
            "/integration/v3/product-data-check"
        }
        VirtusizeEndpoint.FitIllustrator -> {
            "/a/fit-illustrator/v1/index.html"
        }
         VirtusizeEndpoint.ProductMetaDataHints -> {
             "/rest-api/v1/product-meta-data-hints"
         }
         VirtusizeEndpoint.Events -> {
             "/a/api/v3/events"
         }
         VirtusizeEndpoint.Orders -> {
             "/a/api/v3/orders"
         }
         VirtusizeEndpoint.StoreViewApiKey -> {
             "/a/api/v3/stores/api-key/"
         }
    }
}