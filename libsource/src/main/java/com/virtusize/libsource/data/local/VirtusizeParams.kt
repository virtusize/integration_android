package com.virtusize.libsource.data.local

// TODO: Comment
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

    internal fun getVsParamsString(): String {
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