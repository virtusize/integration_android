package com.virtusize.libsource.network

/**
 * This enum represents all available Virtusize endpoints
 */
internal enum class VirtusizeEndpoint {
    ProductCheck,
    GetSize,
    Virtusize,
    ProductMetaDataHints,
    Events,
    Orders,
    StoreViewApiKey,
    StoreProducts,
    UserProducts,
    ProductType,
    UserBodyMeasurements,
    I18N
}

/**
 * This method returns a URL corresponding to the Virtusize endpoint that it is called upon
 * @return the Virtusize Endpoint URL
 */
internal fun VirtusizeEndpoint.getPath(): String {
     return when (this) {
         VirtusizeEndpoint.ProductCheck -> {
             "/product/check"
         }
         VirtusizeEndpoint.GetSize -> {
             "/ds-functions/size-rec/get-size"
         }
         VirtusizeEndpoint.Virtusize -> {
             "/a/aoyama/latest/sdk-webview.html"
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
         VirtusizeEndpoint.StoreProducts -> {
             "/a/api/v3/store-products/"
         }
         VirtusizeEndpoint.UserProducts -> {
             "/a/api/v3/user-products"
         }
         VirtusizeEndpoint.ProductType -> {
             "/a/api/v3/product-types"
         }
         VirtusizeEndpoint.UserBodyMeasurements -> {
             "/a/api/v3/user-body-measurements"
         }
         VirtusizeEndpoint.I18N -> {
             "/bundle-payloads/aoyama/"
         }
    }
}