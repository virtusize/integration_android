package com.virtusize.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.virtusize.libsource.model.VirtusizeButtonStyle
import com.virtusize.libsource.model.VirtusizeProduct
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup Virtusize fit illustrator button
        (application as App)
            .Virtusize
            .setupFitButton(
                fitIllustratorButton = exampleFitButton,
                virtusizeProduct = VirtusizeProduct(externalId = "694", imageUrl = "http://simage-kr.uniqlo.com/goods/31/12/11/71/414571_COL_COL02_570.jpg"))

        // apply style to Virtusize button
        exampleFitButton.applyStyle(VirtusizeButtonStyle.DEFAULT_STYLE)

        // Fit Illustrator opens automatically when button is clicked

        /*
         * To close fit illustrator use
         * exampleFitButton.dismissFitIllustratorView()
         */

    }
}
