package com.virtusize.libsource.data.local.aoyama

data class AoyamaParams(
    private val region: AoyamaRegion = AoyamaRegion.COM,
    private val env: AoyamaEnvironment = AoyamaEnvironment.STAGE,
    private val language: AoyamaLanguage = AoyamaLanguage.EN,
    private val allowedLanguages: List<AoyamaLanguage> = AoyamaLanguage.values().asList(),
    private val customTexts: String? = null,
    private val storeProductId: Int,
    private val browserId: String? = null,
    private val storeId: Int,
    private val externalUserId: String? = null,
    private val showSGI: Boolean = true,
    private val detailsPanelCards: List<AoyamaInfoCategory> = AoyamaInfoCategory.values().asList()
) {

    fun paramsToMap(): Map<String, Any> {

        val languageMap = mutableMapOf<String, String>()
        allowedLanguages.forEach { languageMap[it.value] = it.label }

        return emptyMap<String, Any>()
            .plus(
                mapOf(PARAM_REGION to region.value)
            )
            .plus(
                mapOf(PARAM_ENVIRONMENT to env.value)
            )
            .plus(
                mapOf(PARAM_LANGUAGE to language.value)
            )
            .plus(
                mapOf(PARAM_ALLOW_LANGUAGES to languageMap)
            )
            .plus(
                customTexts?.let{ mapOf(PARAM_CUSTOM_TEXTS to it) }.orEmpty()
            )
            .plus(
                mapOf(PARAM_STORE_PRODUCT_ID to storeProductId)
            )
            .plus(
                browserId?.let{ mapOf(PARAM_BID to it) }.orEmpty()
            )
            .plus(
                mapOf(PARAM_STORE_ID to storeId)
            )
            .plus(
                externalUserId?.let{ mapOf(PARAM_EXTERNAL_USER_ID to it) }.orEmpty()
            )
            .plus(
                mapOf(PARAM_SHOW_SGI to showSGI)
            )
            .plus(
                mapOf(PARAM_DETAILS_PANEL_CARDS to detailsPanelCards.map { it.value })
            )
    }

    fun getI18NUrl(): String {
        return "https://i18n.virtusize." + region.value + if(env == AoyamaEnvironment.STAGE) "/stg" else "" + "/bundle-payloads/aoyama/" + language.value
    }

    private companion object {
        private const val PARAM_REGION = "region"
        private const val PARAM_ENVIRONMENT = "env"
        private const val PARAM_LANGUAGE = "language"
        private const val PARAM_ALLOW_LANGUAGES = "allowedLanguages"
        private const val PARAM_CUSTOM_TEXTS = "customTexts"
        private const val PARAM_STORE_PRODUCT_ID = "storeProductId"
        private const val PARAM_BID = "bid"
        private const val PARAM_STORE_ID = "storeId"
        private const val PARAM_EXTERNAL_USER_ID = "externalUserId"
        private const val PARAM_SHOW_SGI = "showSGI"
        private const val PARAM_DETAILS_PANEL_CARDS = "detailsPanelCards"
    }
}