package com.virtusize.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.virtusize.libsource.model.*
import com.virtusize.libsource.ui.FitIllustratorButton
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

        // setup Virtusize fit illustrator button
        (application as App)
            .Virtusize
            .setupFitButton(
                fitIllustratorButton = exampleFitButton,
                virtusizeProduct = VirtusizeProduct(externalId = "694", imageUrl = "http://simage-kr.uniqlo.com/goods/31/12/11/71/414571_COL_COL02_570.jpg"))

        // Fit Illustrator opens automatically when button is clicked

        /*
         * To close fit illustrator use
         * exampleFitButton.dismissFitIllustratorView()
         */

    }

    private val activityMessageHandler = object : VirtusizeMessageHandler {
        override fun virtusizeControllerShouldClose(fitIllustratorButton: FitIllustratorButton) {
            Log.i(TAG, "Close fit illustrator")
            fitIllustratorButton.dismissFitIllustratorView()
        }

        override fun onEvent(fitIllustratorButton: FitIllustratorButton?, event: VirtusizeEvents) {
            Log.i(TAG, event.getEventName())
        }

        override fun onError(fitIllustratorButton: FitIllustratorButton?, error: VirtusizeError) {
            Log.e(TAG, error.message())
        }
    }
}
