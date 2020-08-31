package com.virtusize.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.VirtusizeOrder
import com.virtusize.libsource.ui.VirtusizeView
import com.virtusize.libsource.util.dpInPx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register message handler to listen to events from Virtusize
        (application as App)
            .Virtusize.registerMessageHandler(activityMessageHandler)

        // Set up Virtusize product
        (application as App)
            .Virtusize
            .setupVirtusizeProduct(virtusizeProduct = VirtusizeProduct(externalId = "694",
                imageUrl = "http://simage-kr.uniqlo.com/goods/31/12/11/71/414571_COL_COL02_570.jpg"
            ))

        // Set up Virtusize button
        // Virtusize opens automatically when button is clicked
        (application as App)
            .Virtusize
            .setupVirtusizeView(
                virtusizeView = exampleVirtusizeButton
            )
        // Set up the Virtusize view style programmatically
        exampleVirtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.BLACK

        // Set up Virtusize InPage Standard
        (application as App)
            .Virtusize
            .setupVirtusizeView(
                virtusizeView = exampleVirtusizeInPageStandard
            )
        exampleVirtusizeInPageStandard.virtusizeViewStyle = VirtusizeViewStyle.TEAL
        // If you like, you can set up the horizontal margins between the screen and the InPage Standard view
        // Note. Use the helper extension function `dpInPx` if you like
        exampleVirtusizeInPageStandard.horizontalMargin = 16.dpInPx.toFloat()
        /*
         * If you like, you can set up the background of the check size button in InPage Standard,
         * as long as it passes WebAIM contrast test.
         *
         * exampleVirtusizeInPageStandard.setButtonBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue))
         */

        // Set up Virtusize InPage Mini
        (application as App)
            .Virtusize
            .setupVirtusizeView(
                virtusizeView = exampleVirtusizeInPageMini
            )
        exampleVirtusizeInPageMini.virtusizeViewStyle = VirtusizeViewStyle.BLACK
        /*
         * If you like, you can set up the background of InPage Mini view as long as it passes WebAIM contrast test.
         *
         * exampleVirtusizeInPageMini.setInPageMiniBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue))
         */

        /*
         * To close the Virtusize page
         *
         * exampleVirtusizeButton.dismissVirtusizeView()
         * exampleVirtusizeInPageStandard.dismissVirtusizeView()
         * exampleVirtusizeInPageMini.dismissVirtusizeView()
         */

        // The sample function to send an order to the Virtusize server
        sendOrderSample()
    }

    /**
     * Demonstrates how to send an order to the Virtusize server
     *
     * Notes:
     * 1. The parameters sizeAlias, variantId, color, gender, and url for [VirtusizeOrderItem] are optional
     * 2. If quantity is not provided, it will be set to 1 on its own
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
        override fun virtusizeControllerShouldClose(virtusizeView: VirtusizeView) {
            Log.i(TAG, "Close Virtusize View")
            virtusizeView.dismissVirtusizeView()
        }

        override fun onEvent(virtusizeView: VirtusizeView?, event: VirtusizeEvent) {
            Log.i(TAG, event.name)
        }

        override fun onError(virtusizeView: VirtusizeView?, errorType: VirtusizeError) {
            Log.e(TAG, errorType.message)
        }
    }
}
