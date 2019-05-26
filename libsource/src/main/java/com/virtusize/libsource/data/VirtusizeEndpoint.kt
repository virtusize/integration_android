package com.virtusize.libsource.data

enum class VirtusizeEndpoint {
    ProductCheck,
    FitIllustrator,
    ProductMetaDataHints,
    Events
}

fun VirtusizeEndpoint.getUrl(): String {
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
    }
}