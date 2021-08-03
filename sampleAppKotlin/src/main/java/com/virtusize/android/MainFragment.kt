package com.virtusize.android

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.virtusize.android.databinding.FragmentMainBinding
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.util.dpInPx
import com.virtusize.libsource.util.spToPx
import com.virtusize.ui.button.VirtusizeButtonStyle

class MainFragment : Fragment() {

    companion object {
        private const val TAG = "MainFragment"
    }

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*
         * Register message handler to listen to events from Virtusize
         */
        (activity?.application as App).Virtusize.registerMessageHandler(activityMessageHandler)

        /*
         * Set up Virtusize product for all the Virtusize views
         */
        (activity?.application as App).Virtusize.setupVirtusizeProduct(
            VirtusizeProduct(
                externalId = "vs_dress",
                imageUrl = "http://www.image.com/goods/12345.jpg"
            )
        )

        /*
         * Set up Virtusize button
         */
        // Virtusize opens automatically when button is clicked
        (activity?.application as App).Virtusize.setupVirtusizeView(binding.exampleVirtusizeButton)
        // Set up the Virtusize view style programmatically
        binding.exampleVirtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.TEAL

        /*
         * Set up Virtusize InPage Standard
         */
        (activity?.application as App).Virtusize
            .setupVirtusizeView(
                virtusizeView = binding.exampleVirtusizeInPageStandard
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

        (activity?.application as App).Virtusize.setupVirtusizeView(virtusizeView = binding.exampleVirtusizeInPageMini)
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

        /*
         * The sample function to send an order to the Virtusize server
         */
        sendOrderSample()

        // Navigate to Virtusize Design System
        binding.designSystemButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.designSystemButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToDesignSystemFragment()
            findNavController().navigate(action)
        }

        binding.snsTestButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToWebViewFragment()
            findNavController().navigate(action)
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

        (activity?.application as App)
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
        (activity?.application as App)
            .Virtusize.unregisterMessageHandler(activityMessageHandler)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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