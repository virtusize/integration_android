package com.virtusize.libsource

import android.content.Context
import com.virtusize.libsource.data.local.*

/**
 * This class utilizes the builder pattern to build and return a Virtusize object
 * @param userId the user id that is the unique user id from the client system
 * @param apiKey the API key that is unique to every Virtusize Client
 * @param browserID the browser ID that is specific to the Virtusize WebView
 * @param env the Virtusize environment
 * @param context Android Application Context
 * @param region the [VirtusizeRegion] that is used to set the region of the config url domains within the Virtusize web app
 * @param language the [VirtusizeLanguage] that sets the initial language the Virtusize web app will load in
 * @param allowedLanguages the languages that the user can switch to using the Language Selector
 * @param showSGI the Boolean value to determine whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
 * @param detailsPanelCards the info categories that will display in the Product Details tab
 */
class VirtusizeBuilder {
    private var userId: String? = null
    private var apiKey: String? = null
    private var browserID: String? = null
    private var env = VirtusizeEnvironment.GLOBAL
    private var context: Context? = null
    private var region: VirtusizeRegion = VirtusizeRegion.JP
    private var language: VirtusizeLanguage? = region.defaultLanguage()
    private var allowedLanguages: MutableList<VirtusizeLanguage> = VirtusizeLanguage.values().asList().toMutableList()
    private var showSGI: Boolean = false
    private var detailsPanelCards: MutableList<VirtusizeInfoCategory> = VirtusizeInfoCategory.values().asList().toMutableList()

    /**
     * This method is used to add the application context to the Virtusize builder
     * Context is required for this Virtusize builder to function properly
     * @param ctx Application Context
     * @return VirtusizeBuilder
     */
    fun init(ctx: Context): VirtusizeBuilder {
        context = ctx
        return this
    }

    /**
     * Sets up the user ID from the client system
     * @param id the id that is an unique user ID from the client system
     * @return VirtusizeBuilder
     */
    fun setUserId(id: String): VirtusizeBuilder {
        this.userId = id
        return this
    }

    /**
     * Sets up the API key provided to Virtusize clients to the Virtusize object
     * The API key is required for the Virtusize object to function properly
     * @param key the API Key
     * @return VirtusizeBuilder
     */
    fun setApiKey(key: String): VirtusizeBuilder {
        this.apiKey = key
        return this
    }

    /**
     * Sets up the Virtusize environment to the Virtusize object
     * By default, the environment value is GLOBAL
     * @param environment VirtusizeEnvironment
     * @return VirtusizeBuilder
     */
    fun setEnv(environment: VirtusizeEnvironment): VirtusizeBuilder {
        this.env = environment
        this.region = environment.virtusizeRegion()
        return this
    }

    /**
     * Sets up the initial display language for the Virtusize web app
     * By default, the language value is based on the region value
     * @param language [VirtusizeLanguage]
     * @return VirtusizeBuilder
     */
    fun setLanguage(language: VirtusizeLanguage) : VirtusizeBuilder {
        this.language = language
        return this
    }

    /**
     * Sets up the languages for users to select for the Virtusize web app
     * By default, the Virtusize web app allows all the possible languages
     * @param allowedLanguages the list of [VirtusizeLanguage]
     * @return VirtusizeBuilder
     */
    fun setAllowedLanguages(allowedLanguages: MutableList<VirtusizeLanguage>) : VirtusizeBuilder {
        this.allowedLanguages = allowedLanguages
        return this
    }

    /**
     * Sets up whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
     * By default, showSGI is false
     * @param showSGI the Boolean value
     * @return VirtusizeBuilder
     */
    fun setShowSGI(showSGI: Boolean) : VirtusizeBuilder {
        this.showSGI = showSGI
        return this
    }

    /**
     * Sets up the info categories that will be displayed in the Product Details tab of the Virtusize web app
     * By default, the Virtusize web app display all the possible info categories
     * @param detailsPanelCards the list of [VirtusizeInfoCategory]
     * @return VirtusizeBuilder
     */
    fun setDetailsPanelCards(detailsPanelCards: MutableList<VirtusizeInfoCategory>) : VirtusizeBuilder {
        this.detailsPanelCards = detailsPanelCards
        return this
    }

    /**
     * Builds the Virtusize object from the passed data and returns the Virtusize object
     * @return Virtusize
     * @see Virtusize
     */
    fun build(): Virtusize {
        if (apiKey.isNullOrEmpty()) {
            throwError(VirtusizeErrorType.ApiKeyNullOrInvalid)
        }
        if (context == null) {
            throwError(VirtusizeErrorType.NullContext)
        }
        val params = VirtusizeParams(
            apiKey = apiKey,
            bid = browserID,
            environment = env,
            region = region,
            language = language,
            allowedLanguages = allowedLanguages,
            virtusizeProduct = null,
            externalUserId = userId,
            showSGI = showSGI,
            detailsPanelCards = detailsPanelCards
        )
        return Virtusize(context = context!!, params = params)
    }
}