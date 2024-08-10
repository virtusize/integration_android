package com.virtusize.android.compose.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.ui.VirtusizeWebViewFragment
import com.virtusize.android.util.VirtusizeUtils

internal class VirtusizeButtonViewModel : ViewModel() {
    private val virtusize: Virtusize by lazy { Virtusize.getInstance() }

    private val virtusizeDialogFragment: VirtusizeWebViewFragment by lazy { VirtusizeWebViewFragment() }

    private val messageHandler =
        object : VirtusizeMessageHandler {
            override fun onEvent(
                product: VirtusizeProduct,
                event: VirtusizeEvent,
            ) {
                onEvent(product, event)
            }

            override fun onError(error: VirtusizeError) {
                onError(error)
            }
        }

    fun loadProduct(product: VirtusizeProduct) {
        virtusize.load(product)
    }

    fun onButtonClick(
        context: Context,
        product: VirtusizeProduct,
    ) {
        VirtusizeUtils.openVirtusizeWebView(
            context,
            virtusize.params,
            virtusizeDialogFragment,
            product,
            messageHandler,
        )
    }
}
