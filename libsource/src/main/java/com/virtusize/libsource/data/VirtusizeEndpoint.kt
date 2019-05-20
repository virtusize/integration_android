package com.virtusize.libsource.data

enum class VirtusizeEndpoint {
    productCheck,
    fitIllustrator,
    productMetaDataHints,
    events
}

fun VirtusizeEndpoint.getUrl(): String {
     return when(this) {
        VirtusizeEndpoint.productCheck -> {
            "/integration/v3/product-data-check"
        }
        VirtusizeEndpoint.fitIllustrator -> {
            "/a/fit-illustrator/v1/index.html"
        }
         VirtusizeEndpoint.productMetaDataHints -> {
             "/rest-api/v1/product-meta-data-hints"
         }
         VirtusizeEndpoint.events -> {
             "/a/api/v3/events"
         }
    }
}