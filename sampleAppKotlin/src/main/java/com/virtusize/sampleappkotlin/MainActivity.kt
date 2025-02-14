package com.virtusize.sampleappkotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeOrderItem
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.VirtusizeViewStyle
import com.virtusize.android.util.dpInPx
import com.virtusize.android.util.spToPx
import com.virtusize.sampleappkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
         * Register message handler to listen to events from Virtusize
         */
        Virtusize.getInstance().registerMessageHandler(activityMessageHandler)

        /*
         * Set up Virtusize product for all the Virtusize views
         */
        val product =
            VirtusizeProduct(
                externalId = "vs_dress",
                imageUrl = "http://www.image.com/goods/12345.jpg",
            )
        Virtusize.getInstance().load(product)

        /*
         * Set up Virtusize button
         */

        // Virtusize opens automatically when button is clicked
        Virtusize.getInstance().setupVirtusizeView(
            virtusizeView = binding.exampleVirtusizeButton,
            product = product,
        )
        // Set up the Virtusize view style programmatically
        binding.exampleVirtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.TEAL

        /*
         * Set up Virtusize InPage Standard
         */
        Virtusize.getInstance().setupVirtusizeView(
            virtusizeView = binding.exampleVirtusizeInPageStandard,
            product = product,
        )
        binding.exampleVirtusizeInPageStandard.virtusizeViewStyle = VirtusizeViewStyle.TEAL
        // If you like, you can set up the horizontal margins between the edges of the app screen and the InPage Standard view
        // Note: Use the helper extension function `dpInPx` if you like
        binding.exampleVirtusizeInPageStandard.horizontalMargin = 16.dpInPx
        /*
         * If you like, you can set up the background color of the check size button in InPage Standard,
         * as long as it passes WebAIM contrast test.
         *
         * exampleVirtusizeInPageStandard.setButtonBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue))
         */

        // If you like, you can change the text sizes of the InPage message and the Check Size button
        binding.exampleVirtusizeInPageStandard.messageTextSize = 10f.spToPx
        binding.exampleVirtusizeInPageStandard.buttonTextSize = 10f.spToPx

        /*
         * Set up Virtusize InPage Mini
         */
        Virtusize.getInstance().setupVirtusizeView(
            virtusizeView = binding.exampleVirtusizeInPageMini,
            product = product,
        )
        binding.exampleVirtusizeInPageMini.virtusizeViewStyle = VirtusizeViewStyle.TEAL

        /*
         * If you like, you can set up the background of InPage Mini view as long as it passes WebAIM contrast test.
         *
         * exampleVirtusizeInPageMini.setInPageMiniBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue))
         */

        // If you like, you can change the text sizes of the InPage message and the Check Size button
        binding.exampleVirtusizeInPageMini.messageTextSize = 12f.spToPx
        binding.exampleVirtusizeInPageMini.buttonTextSize = 10f.spToPx

        /*
         * To close the Virtusize page
         *
         * exampleVirtusizeButton.dismissVirtusizeView()
         * exampleVirtusizeInPageStandard.dismissVirtusizeView()
         * exampleVirtusizeInPageMini.dismissVirtusizeView()
         */

        // The sample function to send an order to the Virtusize server
        sendOrderSample()

        /*
         * Open WebApp
         */
        binding.openWebAppButton.setOnClickListener {
            startActivity(Intent(this, WebAppActivity::class.java))
        }
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
        order.items =
            mutableListOf(
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
                    "http://example.com/products/P001",
                ),
            )

        Virtusize.getInstance()
            .sendOrder(
                order,
                // this optional success callback is called when the app successfully sends the order
                onSuccess = {
                    Log.i(TAG, "Successfully sent the order")
                },
                // this optional error callback is called when an error occurs when the app is sending the order
                onError = { error ->
                    Log.e(TAG, error.message)
                },
            )
    }

    override fun onPause() {
        // Always unregister message handler in onPause() or depending on implementation onStop().
        Virtusize.getInstance().unregisterMessageHandler(activityMessageHandler)
        super.onPause()
    }

    private val activityMessageHandler =
        object : VirtusizeMessageHandler {
            override fun onEvent(
                product: VirtusizeProduct,
                event: VirtusizeEvent,
            ) {
                Log.i(TAG, event.name)
            }

            override fun onError(error: VirtusizeError) {
                Log.e(TAG, error.message)
            }
        }
}
