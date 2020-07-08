package com.virtusize.libsource.data.local.aoyama

import com.virtusize.libsource.data.local.VirtusizeProduct

// TODO
class AoyamaParams private constructor(
    internal var apiKey: String?,
    internal var region: AoyamaRegion,
    internal var env: AoyamaEnvironment,
    private val language: AoyamaLanguage,
    private val allowedLanguages: MutableList<AoyamaLanguage>,
    internal val virtusizeProduct: VirtusizeProduct?,
    internal var externalUserId: String?,
    private val showSGI: Boolean,
    private val detailsPanelCards: MutableList<AoyamaInfoCategory>
) {

    data class Builder(
        internal val apiKey: String? = null,
        internal val region: AoyamaRegion = AoyamaRegion.JP,
        internal val env: AoyamaEnvironment = AoyamaEnvironment.PRODUCTION,
        internal var language: AoyamaLanguage = AoyamaLanguage.JP,
        internal var allowedLanguages: MutableList<AoyamaLanguage> = AoyamaLanguage.values().asList().toMutableList(),
        internal var virtusizeProduct: VirtusizeProduct? = null,
        internal val externalUserId: String? = null,
        internal var showSGI: Boolean = false,
        internal var detailsPanelCards: MutableList<AoyamaInfoCategory> = AoyamaInfoCategory.values().asList().toMutableList()
    ) {

        fun language(language: AoyamaLanguage) = apply { this.language = language }
        fun allowedLanguages(allowedLanguages: MutableList<AoyamaLanguage>) = apply { this.allowedLanguages = allowedLanguages }
        fun virtusizeProduct(virtusizeProduct: VirtusizeProduct) = apply { this.virtusizeProduct = virtusizeProduct }
        fun showSGI(showSGI: Boolean) = apply { this.showSGI = showSGI }
        fun detailsPanelCards(detailsPanelCards: MutableList<AoyamaInfoCategory>) = apply { this.detailsPanelCards = detailsPanelCards }
        fun build() = AoyamaParams(apiKey, region, env, language, allowedLanguages, virtusizeProduct, externalUserId, showSGI, detailsPanelCards)
    }

    internal fun getVsParamsString(): String {
        return "{$PARAM_API_KEY: '$apiKey', " +
                "$PARAM_STORE_PRODUCT_ID: '${virtusizeProduct?.productCheckData?.productId}', " +
                (if (externalUserId != null) "$PARAM_EXTERNAL_USER_ID: '$externalUserId', " else "") +
                "$PARAM_LANGUAGE: '${language.value}', " +
                "$PARAM_SHOW_SGI: $showSGI, " +
                "$PARAM_ALLOW_LANGUAGES: ${allowedLanguages.map { "{label: \"${it.label}\", value: \"${it.value}\"}" }}, " +
//                    "defaultBrandsSGI: '${showSGI}', " +
                    "$PARAM_DETAILS_PANEL_CARDS: ${detailsPanelCards.map { "\"${it.value}\"" }}, " +
                "$PARAM_REGION: '${region.value}', " +
                "$PARAM_ENVIRONMENT: '${env.value}'}"
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