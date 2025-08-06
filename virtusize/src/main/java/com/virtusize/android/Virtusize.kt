package com.virtusize.android

import android.content.Context
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.network.VirtusizeApiTask
import com.virtusize.android.ui.VirtusizeView
import kotlin.jvm.Throws

/**
 * This interface defines the methods that are required to be implemented by the [Virtusize] class
 */
interface Virtusize {
    companion object {
        /**
         * Singleton instance of [Virtusize]
         */
        @Volatile
        private lateinit var instance: Virtusize

        /**
         * Initializes the [Virtusize] instance
         */
        internal fun init(
            context: Context,
            params: VirtusizeParams,
        ) = if (!Companion::instance.isInitialized) {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    VirtusizeImpl(context = context, params = params).also { instance = it }
                } else {
                    instance
                }
            }
        } else {
            instance
        }

        /**
         * Gets the [Virtusize] instance
         * @throws IllegalStateException if the [Virtusize] instance is not initialized
         */
        @Throws(IllegalStateException::class)
        fun getInstance(): Virtusize = getInstanceOrNull() ?: throw IllegalStateException("Virtusize is not initialized")

        /**
         * Get the [Virtusize] instance or return `null`, if [Virtusize] has not been yet initialized
         */
        fun getInstanceOrNull(): Virtusize? =
            if (!Companion::instance.isInitialized) {
                null
            } else {
                instance
            }
    }

    /**
     * The [VirtusizeParams] object that contains userId, apiKey, env and other parameters to be passed to the Virtusize web app
     */
    val params: VirtusizeParams?

    /**
     * The language that the Virtusize web app will display in
     */
    val displayLanguage: VirtusizeLanguage

    /**
     * The [VirtusizeRepository] instance
     */
    val virtusizeRepository: VirtusizeRepository

    /**
     * Use this function to set up the user ID in the app when the user is logged in/out
     * @param userId the user ID that is unique from the client system
     */
    fun setUserId(userId: String)

    /**
     * Registers a message handler.
     * The registered message handlers will receive Virtusize errors, events, and the close action for Fit Illustrator.
     * @param messageHandler an instance of VirtusizeMessageHandler
     * @see VirtusizeMessageHandler
     */
    fun registerMessageHandler(messageHandler: VirtusizeMessageHandler)

    /**
     * Unregisters a message handler.
     * If a message handler is not unregistered when the associated activity or fragment dies,
     * then when the activity or fragment opens again,
     * it will keep listening to the events along with newly registered message handlers.
     * @param messageHandler an instance of {@link VirtusizeMessageHandler}
     * @see VirtusizeMessageHandler
     */
    fun unregisterMessageHandler(messageHandler: VirtusizeMessageHandler)

    /**
     * Sets up the product for the product detail page
     *
     * @param virtusizeProduct VirtusizeProduct that is being loaded with the Virtusize API
     * @return true if the product is valid, false otherwise
     */
    fun load(virtusizeProduct: VirtusizeProduct)

    /**
     * Sets up and check the product for the product detail page
     *
     * @param virtusizeProduct VirtusizeProduct that is being loaded with the Virtusize API
     * @return true if the product is valid, false otherwise
     */
    suspend fun productCheck(virtusizeProduct: VirtusizeProduct): Boolean

    /**
     * Sets up the Virtusize view by passing the VirtusizeView along with the bound VirtusizeProduct
     * @param virtusizeView VirtusizeView that is being set up
     * @param product the [VirtusizeProduct] set by a client
     * @throws IllegalArgumentException throws an error if VirtusizeButton is null or the image URL of VirtusizeProduct is invalid
     */
    fun setupVirtusizeView(
        virtusizeView: VirtusizeView?,
        product: VirtusizeProduct,
    )

    /**
     * Cleanup [virtusizeView] once it's no longer needed (e.g. leaves the composition)
     */
    fun cleanupVirtusizeView(virtusizeView: VirtusizeView)

    /**
     * Sends an order to the Virtusize server for Kotlin apps
     * @param order [VirtusizeOrder]
     * @param onSuccess the optional success callback to notify [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: (() -> Unit)? = null,
        onError: ((VirtusizeError) -> Unit)? = null,
    )

    /**
     * Sends an order to the Virtusize server for Java apps
     * @param order [VirtusizeOrder]
     * @param onSuccess the optional success callback to pass the [Store] from the response when [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null,
    )

    /**
     * Sets the display language for the Virtusize widgets
     * @param language the [VirtusizeLanguage] to be set
     */
    fun setVsWidgetLanguage(language: VirtusizeLanguage)
}
