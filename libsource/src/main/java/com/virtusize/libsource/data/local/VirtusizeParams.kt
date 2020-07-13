package com.virtusize.libsource.data.local

/**
 * The class that wraps the parameters we can pass to the Virtusize web app
 * @param apiKey the API key that is unique to every Virtusize Client
 * @param environment the [VirtusizeEnvironment] that is used in the SDK
 * @param region the [VirtusizeRegion] that is used to set the region of the config url domains within the Virtusize web app
 * @param language the [VirtusizeLanguage] that sets the initial language the Virtusize web app will load in
 * @param allowedLanguages the languages that the user can switch to using the Language Selector
 * @param virtusizeProduct [VirtusizeProduct] that is used for the API endpoint Product Data Check
 * @param externalUserId the unique user ID from the client system. It should be set during the initialization of the [Virtusize] class
 * @param showSGI the Boolean value to determine whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
 * @param detailsPanelCards the info categories that will be displayed in the Product Details tab
 */
data class VirtusizeParams(
    internal var apiKey: String?,
    internal var environment: VirtusizeEnvironment,
    private var region: VirtusizeRegion,
    private val language: VirtusizeLanguage?,
    private val allowedLanguages: MutableList<VirtusizeLanguage>,
    internal var virtusizeProduct: VirtusizeProduct?,
    internal var externalUserId: String?,
    private val showSGI: Boolean,
    private val detailsPanelCards: MutableList<VirtusizeInfoCategory>
) {

    /**
     * Returns the virtusize parameter string to be passed to the JavaScript function vsParamsFromSDK
     */
    internal fun vsParamsString(): String {
        return "{$PARAM_API_KEY: '$apiKey', " +
                "$PARAM_STORE_PRODUCT_ID: '${virtusizeProduct?.productCheckData?.productId}', " +
                (if (externalUserId != null) "$PARAM_EXTERNAL_USER_ID: '$externalUserId', " else "") +
                "$PARAM_LANGUAGE: '${language?.value}', " +
                "$PARAM_SHOW_SGI: $showSGI, " +
                "$PARAM_ALLOW_LANGUAGES: ${allowedLanguages.map { "{label: \"${it.label}\", value: \"${it.value}\"}" }}, " +
                    "$PARAM_DETAILS_PANEL_CARDS: ${detailsPanelCards.map { "\"${it.value}\"" }}, " +
                "$PARAM_REGION: '${region.value}', " +
                "$PARAM_ENVIRONMENT: 'production'}"
    }

    private companion object {
        private const val PARAM_API_KEY = "apiKey"
        private const val PARAM_REGION = "region"
        private const val PARAM_ENVIRONMENT = "env"
        private const val PARAM_LANGUAGE = "language"
        private const val PARAM_ALLOW_LANGUAGES = "allowedLanguages"
        private const val PARAM_STORE_PRODUCT_ID = "externalProductId"
        private const val PARAM_EXTERNAL_USER_ID = "externalUserId"
        private const val PARAM_SHOW_SGI = "showSGI"
        private const val PARAM_DETAILS_PANEL_CARDS = "detailsPanelCards"
    }
}