package com.virtusize.libsource

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.ui.VirtusizeWebViewFragment
import com.virtusize.libsource.util.Constants

class VirtusizeFlutterHelper(private val context: Context) {
    private var virtusizeDialogFragment: VirtusizeWebViewFragment = VirtusizeWebViewFragment()

    fun openVirtusizeView(virtusize: Virtusize?, product: VirtusizeProduct, productDataCheck: ProductCheck) {
        val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
        val previousFragment = context.supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
        previousFragment?.let {fragment ->
            fragmentTransaction.remove(fragment)
        }
        fragmentTransaction.addToBackStack(null)
        val args = Bundle()
        args.putString(Constants.URL_KEY, VirtusizeApi.virtusizeWebViewURL())
        virtusize?.params?.let { params ->
            params.virtusizeProduct = product
            params.virtusizeProduct?.productCheckData = productDataCheck
            args.putString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY, "javascript:vsParamsFromSDK(${params.vsParamsString()})")
        }
        virtusizeDialogFragment.arguments = args
        virtusizeDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
    }
}