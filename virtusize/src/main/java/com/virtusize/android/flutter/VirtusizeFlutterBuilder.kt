package com.virtusize.android.flutter

import android.content.Context
import com.virtusize.android.data.local.VirtusizeEnvironment
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeInfoCategory
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeRegion
import com.virtusize.android.data.local.throwError
import com.virtusize.android.data.local.virtusizeRegion

/**
 * This class utilizes the builder pattern to build and return a Virtusize object
 * @param userId the user id that is the unique user id from the client system
 * @param apiKey the API key that is unique to every Virtusize Client
 * @param env the Virtusize environment
 * @param context Android Application Context
 * @param region the [VirtusizeRegion] that is used to set the region of the config url domains within the Virtusize web app
 * @param language the [VirtusizeLanguage] that sets the initial language the Virtusize web app will load in
 * @param allowedLanguages the languages that the user can switch to using the Language Selector
 * @param showSGI the Boolean value to determine whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
 * @param detailsPanelCards the info categories that will display in the Product Details tab
 * @param showSNSButtons the Boolean value to determine whether the Virtusize web app will display the SNS buttons
 * @param branch the branch name of targeted Virtusize environment when specified
 */
class VirtusizeFlutterBuilder {
    private var userId: String? = null
    private var apiKey: String? = null
    private var env = VirtusizeEnvironment.GLOBAL
    private var context: Context? = null
    private var region: VirtusizeRegion = VirtusizeRegion.JP
    private var language: VirtusizeLanguage? = null
    private var allowedLanguages: MutableList<VirtusizeLanguage> =
        VirtusizeLanguage.entries.toMutableList()
    private var showSGI: Boolean = false
    private var detailsPanelCards: Set<VirtusizeInfoCategory> = VirtusizeInfoCategory.entries.toMutableSet()
    private var showSNSButtons: Boolean = true
    private var branch: String? = null
    private var virtusizeFlutterPresenter: VirtusizeFlutterPresenter? = null

    /**
     * This method is used to add the application context to the Virtusize builder
     * Context is required for this Virtusize builder to function properly
     * @param ctx Application Context
     * @return VirtusizeBuilder
     */
    fun init(ctx: Context): VirtusizeFlutterBuilder {
        context = ctx
        return this
    }

    /**
     * Sets up the user ID from the client system
     * @param id the id that is an unique user ID from the client system
     * @return VirtusizeBuilder
     */
    fun setUserId(id: String): VirtusizeFlutterBuilder {
        this.userId = id
        return this
    }

    /**
     * Sets up the API key provided to Virtusize clients to the Virtusize object
     * The API key is required for the Virtusize object to function properly
     * @param key the API Key
     * @return VirtusizeBuilder
     */
    fun setApiKey(key: String): VirtusizeFlutterBuilder {
        this.apiKey = key
        return this
    }

    /**
     * Sets up the Virtusize environment to the Virtusize object
     * By default, the environment value is GLOBAL
     * @param environment VirtusizeEnvironment
     * @return VirtusizeBuilder
     */
    fun setEnv(environment: VirtusizeEnvironment): VirtusizeFlutterBuilder {
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
    fun setLanguage(language: VirtusizeLanguage): VirtusizeFlutterBuilder {
        this.language = language
        return this
    }

    /**
     * Sets up the languages for users to select for the Virtusize web app
     * By default, the Virtusize web app allows all the possible languages
     * @param allowedLanguages the list of [VirtusizeLanguage]
     * @return VirtusizeBuilder
     */
    fun setAllowedLanguages(allowedLanguages: List<VirtusizeLanguage>): VirtusizeFlutterBuilder {
        this.allowedLanguages = allowedLanguages.toMutableList()
        return this
    }

    /**
     * Sets up whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
     * By default, showSGI is false
     * @param showSGI the Boolean value
     * @return VirtusizeBuilder
     */
    fun setShowSGI(showSGI: Boolean): VirtusizeFlutterBuilder {
        this.showSGI = showSGI
        return this
    }

    /**
     * Sets up the info categories that will be displayed in the Product Details tab of the Virtusize web app
     * By default, the Virtusize web app display all the possible info categories
     * @param detailsPanelCards the list of [VirtusizeInfoCategory]
     * @return [VirtusizeFlutterBuilder]
     */
    fun setDetailsPanelCards(detailsPanelCards: Set<VirtusizeInfoCategory>): VirtusizeFlutterBuilder {
        this.detailsPanelCards = detailsPanelCards
        return this
    }

    /**
     * Sets up whether the Virtusize web app will show the SNS buttons
     * By default, showSNSButtons is true
     * @param showSNSButtons the Boolean value
     * @return [VirtusizeFlutterBuilder]
     */
    fun setShowSNSButtons(showSNSButtons: Boolean): VirtusizeFlutterBuilder {
        this.showSNSButtons = showSNSButtons
        return this
    }

    /**
     * Sets up an environment branch name
     * By default, branch is null
     * @param branch the String value
     * @return [VirtusizeFlutterBuilder]
     */
    fun setBranch(branchName: String): VirtusizeFlutterBuilder {
        this.branch = branchName
        return this
    }

    /**
     * Sets up VirtusizeFlutterPresenter
     * By default, virtusizeFlutterPresenter is null
     * @param presenter the VirtusizeFlutterPresenter value
     * @return [VirtusizeFlutterBuilder]
     */
    fun setPresenter(presenter: VirtusizeFlutterPresenter): VirtusizeFlutterBuilder {
        this.virtusizeFlutterPresenter = presenter
        return this
    }

    /**
     * Builds the VirtusizeFlutter object from the passed data and returns the VirtusizeFlutter object
     * @return VirtusizeFlutter
     * @see VirtusizeFlutter
     */
    fun build(): VirtusizeFlutter {
        if (apiKey.isNullOrEmpty()) {
            VirtusizeErrorType.ApiKeyNullOrInvalid.throwError()
        }
        if (context == null) {
            VirtusizeErrorType.NullContext.throwError()
        }
        val params =
            VirtusizeParams(
                context = context!!,
                apiKey = apiKey,
                environment = env,
                region = region,
                language = language ?: region.defaultLanguage(),
                allowedLanguages = allowedLanguages,
                externalUserId = userId,
                showSGI = showSGI,
                detailsPanelCards = detailsPanelCards,
                showSNSButtons = showSNSButtons,
                branch = branch,
            )
        return VirtusizeFlutter.init(
            context = context!!,
            params = params,
            virtusizeFlutterPresenter = virtusizeFlutterPresenter,
        )
    }
}
