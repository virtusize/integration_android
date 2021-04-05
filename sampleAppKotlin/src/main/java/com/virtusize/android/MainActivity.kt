package com.virtusize.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.VirtusizeOrder
import com.virtusize.libsource.util.dpInPx
import com.virtusize.libsource.util.spToPx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
         * Register message handler to listen to events from Virtusize
         */
        (application as App).Virtusize.registerMessageHandler(activityMessageHandler)

        /*
         * Set up Virtusize product for all the Virtusize views
         */
        (application as App).Virtusize.setupVirtusizeProduct(
                VirtusizeProduct(
                    externalId = "694",
                    imageUrl = "http://www.image.com/goods/12345.jpg"
                )
            )

        /*
         * Set up Virtusize button
         */
        // Virtusize opens automatically when button is clicked
        (application as App).Virtusize.setupVirtusizeView(exampleVirtusizeButton)
        // Set up the Virtusize view style programmatically
        exampleVirtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.TEAL

        /*
         * Set up Virtusize InPage Standard
         */
        (application as App).Virtusize
            .setupVirtusizeView(
                virtusizeView = exampleVirtusizeInPageStandard
            )
        exampleVirtusizeInPageStandard.virtusizeViewStyle = VirtusizeViewStyle.TEAL
        // If you like, you can set up the horizontal margins between the edges of the app screen and the InPage Standard view
        // Note: Use the helper extension function `dpInPx` if you like
        exampleVirtusizeInPageStandard.horizontalMargin = 16.dpInPx
        /*
         * If you like, you can set up the background color of the check size button in InPage Standard,
         * as long as it passes WebAIM contrast test.
         *
         * exampleVirtusizeInPageStandard.setButtonBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue))
         */

        // If you like, you can change the text sizes of the InPage message and the Check Size button
        exampleVirtusizeInPageStandard.messageTextSize = 10f.spToPx
        exampleVirtusizeInPageStandard.buttonTextSize = 10f.spToPx

        /*
         * Set up Virtusize InPage Mini
         */
        (application as App).Virtusize.setupVirtusizeView(virtusizeView = exampleVirtusizeInPageMini)
        exampleVirtusizeInPageMini.virtusizeViewStyle = VirtusizeViewStyle.TEAL

        /*
         * If you like, you can set up the background of InPage Mini view as long as it passes WebAIM contrast test.
         *
         * exampleVirtusizeInPageMini.setInPageMiniBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue))
         */

        // If you like, you can change the text sizes of the InPage message and the Check Size button
        exampleVirtusizeInPageMini.messageTextSize = 12f.spToPx
        exampleVirtusizeInPageMini.buttonTextSize = 10f.spToPx

        /*
         * To close the Virtusize page
         *
         * exampleVirtusizeButton.dismissVirtusizeView()
         * exampleVirtusizeInPageStandard.dismissVirtusizeView()
         * exampleVirtusizeInPageMini.dismissVirtusizeView()
         */

        /*
         * The sample function to send an order to the Virtusize server
         */
        sendOrderSample()
    }

    /**
     * Demonstrates how to send an order to the Virtusize server
     *
     * Notes:
     * 1. The parameters sizeAlias, variantId, color, gender, and url for [VirtusizeOrderItem] are optional
     * 2. If the item quantity is not provided, it will be set to 1 on its own
     */
    private fun sendOrderSample() {
        val order = VirtusizeOrder("888400111032")
        order.items = mutableListOf(
            VirtusizeOrderItem(
                "P001",
                "L",
                "Large",
                "P001_SIZEL_RED",
                "http://images.example.com/products/P001/red/image1xl.jpg",
                "Red",
                "W",
                51000.00,
                "JPY",
                1,
                "http://example.com/products/P001"
            )
        )

        (application as App)
            .Virtusize
            .sendOrder(order,
                // this optional success callback is called when the app successfully sends the order
                onSuccess = {
                    Log.i(TAG, "Successfully sent the order")
                },
                // this optional error callback is called when an error occurs when the app is sending the order
                onError = { error ->
                    Log.e(TAG, error.message)
                })
    }

    override fun onPause() {
        // Always un register message handler in onPause() or depending on implementation onStop().
        (application as App)
            .Virtusize.unregisterMessageHandler(activityMessageHandler)
        super.onPause()
    }

    private val activityMessageHandler = object : VirtusizeMessageHandler {
        override fun onEvent(event: VirtusizeEvent) {
            Log.i(TAG, event.name)
        }

        override fun onError(error: VirtusizeError) {
            Log.e(TAG, error.message)
        }
    }
}
