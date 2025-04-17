package com.virtusize.android.flutter

import android.app.Activity
import android.content.Context
import com.virtusize.android.ErrorResponseHandler
import com.virtusize.android.SuccessResponseHandler
import com.virtusize.android.VirtusizeRepository
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.network.VirtusizeApiTask
import kotlin.jvm.Throws

/**
 * This interface defines the methods that are required to be implemented by the [VirtusizeFlutter] class
 */
interface VirtusizeFlutter {
    companion object {
        /**
         * Singleton instance of [VirtusizeFlutter]
         */
        @Volatile
        private lateinit var instance: VirtusizeFlutter

        /**
         * Initializes the [VirtusizeFlutter] instance
         */
        internal fun init(
            context: Context,
            params: VirtusizeParams,
            virtusizeFlutterPresenter: VirtusizeFlutterPresenter?,
        ) = if (!Companion::instance.isInitialized) {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    VirtusizeFlutterImpl(
                        context = context,
                        params = params,
                        virtusizeFlutterPresenter = virtusizeFlutterPresenter,
                    ).also { instance = it }
                } else {
                    instance
                }
            }
        } else {
            instance
        }

        /**
         * Gets the [VirtusizeFlutter] instance
         * @throws IllegalStateException if the [VirtusizeFlutter] instance is not initialized
         */
        @Throws(IllegalStateException::class)
        fun getInstance(): VirtusizeFlutter =
            if (!Companion::instance.isInitialized) {
                throw IllegalStateException("VirtusizeFlutter is not initialized")
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

    fun openVirtusizeWebView(
        activity: Activity,
        externalProductId: String,
    )

    fun getPrivacyPolicyLink(context: Context): String
}
