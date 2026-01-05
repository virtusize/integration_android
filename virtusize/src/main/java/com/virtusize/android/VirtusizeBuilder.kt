package com.virtusize.android

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
 * @param showPrivacyPolicy the Boolean value to determine whether to show or hide the privacy policy
 * @param serviceEnvironment the Boolean value to determine whether to use or not services.virtusize.com url
 */
class VirtusizeBuilder {
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
    private var showPrivacyPolicy: Boolean = true
    private var serviceEnvironment: Boolean = true

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
    fun setLanguage(language: VirtusizeLanguage): VirtusizeBuilder {
        this.language = language
        return this
    }

    /**
     * Sets up the languages for users to select for the Virtusize web app
     * By default, the Virtusize web app allows all the possible languages
     * @param allowedLanguages the list of [VirtusizeLanguage]
     * @return VirtusizeBuilder
     */
    fun setAllowedLanguages(allowedLanguages: List<VirtusizeLanguage>): VirtusizeBuilder {
        this.allowedLanguages = allowedLanguages.toMutableList()
        return this
    }

    /**
     * Sets up whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
     * By default, showSGI is false
     * @param showSGI the Boolean value
     * @return VirtusizeBuilder
     */
    fun setShowSGI(showSGI: Boolean): VirtusizeBuilder {
        this.showSGI = showSGI
        return this
    }

    /**
     * Sets up the info categories that will be displayed in the Product Details tab of the Virtusize web app
     * By default, the Virtusize web app display all the possible info categories
     * @param detailsPanelCards the list of [VirtusizeInfoCategory]
     * @return [VirtusizeBuilder]
     */
    fun setDetailsPanelCards(detailsPanelCards: Set<VirtusizeInfoCategory>): VirtusizeBuilder {
        this.detailsPanelCards = detailsPanelCards
        return this
    }

    /**
     * Sets up whether the Virtusize web app will show the SNS buttons
     * By default, showSNSButtons is true
     * @param showSNSButtons the Boolean value
     * @return [VirtusizeBuilder]
     */
    fun setShowSNSButtons(showSNSButtons: Boolean): VirtusizeBuilder {
        this.showSNSButtons = showSNSButtons
        return this
    }

    /**
     * Sets up an environment branch name
     * By default, branch is null
     * @param branch the String value
     * @return [VirtusizeBuilder]
     */
    fun setBranch(branchName: String): VirtusizeBuilder {
        this.branch = branchName
        return this
    }

    /**
     * Sets up whether the Virtusize will show the Privacy Policy
     * By default value set to TRUE
     * @param showPrivacyPolicy the Boolean value
     * @return [VirtusizeBuilder]
     */
    fun setShowPrivacyPolicy(showPrivacyPolicy: Boolean): VirtusizeBuilder {
        this.showPrivacyPolicy = showPrivacyPolicy
        return this
    }

    /**
     * Sets up whether to use services.virtusize.com url
     * By default value set to TRUE
     * @param serviceEnvironment the Boolean value
     * @return [VirtusizeBuilder]
     */
    fun setServiceEnvironment(serviceEnvironment: Boolean): VirtusizeBuilder {
        this.serviceEnvironment = serviceEnvironment
        return this
    }

    /**
     * Builds the Virtusize object from the passed data and returns the Virtusize object
     * @return Virtusize
     * @see Virtusize
     */
    fun build(): Virtusize {
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
                branch = branch ?: "snkrdnk-line-quick-fix",
                showPrivacyPolicy = showPrivacyPolicy,
                serviceEnvironment = serviceEnvironment,
            )
        return Virtusize.init(context = context!!, params = params)
    }
}
