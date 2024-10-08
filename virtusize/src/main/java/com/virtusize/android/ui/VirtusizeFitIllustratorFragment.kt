package com.virtusize.android.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.virtusize.android.R
import com.virtusize.android.databinding.WebviewFitIllustratorBinding
import com.virtusize.android.util.Constants
import com.virtusize.android.util.getActivity

class VirtusizeFitIllustratorFragment : DialogFragment() {
    companion object {
        private val TAG = VirtusizeFitIllustratorFragment::class.simpleName

        fun launch(
            activity: Activity?,
            url: String,
        ) {
            var fragmentManager: FragmentManager? = null
            if (activity is AppCompatActivity) {
                fragmentManager = activity.supportFragmentManager
            } else if (activity is FragmentActivity) {
                fragmentManager = activity.supportFragmentManager
            }
            fragmentManager?.let {
                val fitIllustratorDialogFragment = VirtusizeFitIllustratorFragment()
                val fragmentTransaction = fragmentManager.beginTransaction()
                val previousFragment = fragmentManager.findFragmentByTag(Constants.FRAG_TAG)
                previousFragment?.let { fragment ->
                    fragmentTransaction.remove(fragment)
                }
                fragmentTransaction.addToBackStack(null)
                val args = Bundle()
                args.putString(Constants.URL_KEY_FOR_FIT_ILLUSTRATOR, url)
                fitIllustratorDialogFragment.arguments = args
                fitIllustratorDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
            } ?: run {
                Log.e(TAG, "Failed to open the VirtusizeFitIllustratorFragment, URL: $url")
            }
        }

        fun launch(
            context: Context,
            url: String,
        ) {
            launch(context.getActivity(), url)
        }
    }

    private lateinit var binding: WebviewFitIllustratorBinding
    private var url = "http://www.virtusize.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets style to dialog to show it as full screen
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = WebviewFitIllustratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("JavascriptInterface")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.fitIllustratorWebView.enableWindowsSettings()
        // Set up the web view client that adds a JavaScript script for the click listener to close the button
        binding.fitIllustratorWebView.webViewClient =
            object : WebViewClient() {
                override fun onPageFinished(
                    view: WebView?,
                    url: String?,
                ) {
                    view?.loadUrl(
                        "javascript:(function() { " +
                            "var element = document.getElementsByClassName('global-close')[0];" +
                            "element.onclick = function() { " +
                            "${Constants.JS_BRIDGE_NAME}.userClosedWidget();" +
                            " };" +
                            "})()",
                    )
                }
            }
        // Add the Javascript interface to receive events from the web view
        binding.fitIllustratorWebView.addJavascriptInterface(
            JavaScriptInterface(),
            Constants.JS_BRIDGE_NAME,
        )
        // Get the Fit Illustrator URL passed in fragment arguments
        arguments?.getString(Constants.URL_KEY_FOR_FIT_ILLUSTRATOR)?.let {
            url = it
        }
        binding.fitIllustratorWebView.loadUrl(url)
    }

    /**
     * The Javascript interface to receive events from the web view
     */
    private inner class JavaScriptInterface {
        /**
         * This method is called when a user clicks on the close button in the Fit Illustrator window
         */
        @JavascriptInterface
        fun userClosedWidget() {
            dismiss()
        }
    }
}
