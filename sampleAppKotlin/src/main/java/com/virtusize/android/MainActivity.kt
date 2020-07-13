package com.virtusize.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.VirtusizeOrder
import com.virtusize.libsource.ui.VirtusizeButton
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

        // setup Virtusize button
        // Virtusize opens automatically when button is clicked
        (application as App)
            .Virtusize
            .setupVirtusizeButton(
                virtusizeButton = exampleVirtusizeButton,
                virtusizeProduct = VirtusizeProduct(externalId = "694", imageUrl = "http://simage-kr.uniqlo.com/goods/31/12/11/71/414571_COL_COL02_570.jpg")
            )
        /*
         * To set up the button style programmatically
         * exampleVirtusizeButton.buttonStyle = VirtusizeButtonStyle.DEFAULT_STYLE
         */


        /*
         * To close the Aoyama page
         * exampleAoyamaButton.dismissAoyamaView()
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
                5100.00,
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
                    Log.e(TAG, error.message())
                })
    }

    override fun onPause() {
        // Always un register message handler in onPause() or depending on implementation onStop().
        (application as App)
            .Virtusize.unregisterMessageHandler(activityMessageHandler)
        super.onPause()
    }

    private val activityMessageHandler = object : VirtusizeMessageHandler {
        override fun virtusizeControllerShouldClose(virtusizeButton: VirtusizeButton) {
            Log.i(TAG, "Close Virtusize View")
            virtusizeButton.dismissVirtusizeView()
        }

        override fun onEvent(virtusizeButton: VirtusizeButton?, event: VirtusizeEvent) {
            Log.i(TAG, event.name)
        }

        override fun onError(virtusizeButton: VirtusizeButton?, error: VirtusizeError) {
            Log.e(TAG, error.message())
        }
    }
}
