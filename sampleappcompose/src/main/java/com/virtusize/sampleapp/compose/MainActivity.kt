package com.virtusize.sampleapp.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VirtusizeSampleApp()
        }

        Virtusize.getInstance().registerMessageHandler(activityMessageHandler)
    }

    override fun onPause() {
        super.onPause()
        Virtusize.getInstance().unregisterMessageHandler(activityMessageHandler)
    }
}
