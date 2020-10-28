package com.virtusize.libsource.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.util.FontUtils
import com.virtusize.libsource.util.VirtusizeUtils
import com.virtusize.libsource.util.dpInPx
import kotlinx.android.synthetic.main.view_inpage_standard.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class VirtusizeInPageStandard(context: Context, attrs: AttributeSet) : VirtusizeInPageView(context, attrs) {

    /**
     * @see VirtusizeView.virtusizeParams
     */
    override var virtusizeParams: VirtusizeParams? = null
        private set

    /**
     * @see VirtusizeView.virtusizeMessageHandler
     */
    override lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
        private set

    /**
     * @see VirtusizeView.virtusizeDialogFragment
     */
    override var virtusizeDialogFragment = VirtusizeWebView()
        private set

    // If the width of the InPage is small than 411dp, the value is true
    private var smallInPageWidth = false

    // The duration of how long the cross fade animation for product images should be
    private val crossFadeAnimationDuration = 750
    // The cross fade animation Runnable
    private var crossFadeRunnable: Runnable? = null
    // The cross fade animation Handler
    private var crossFadeHandler: Handler = Handler(Looper.getMainLooper())

    // The VirtusizeViewStyle that clients can choose to use for this InPage Standard view
    var virtusizeViewStyle = VirtusizeViewStyle.NONE
        set(value) {
            field = value
            setStyle()
        }

    // The background color that clients can set up for the button
    var virtusizeButtonBackgroundColor = 0
        private set

    // The horizontal margin between the edges of InPage Standard view and the phone screen
    var horizontalMargin = -1
        set(value) {
            field = value
            setStyle()
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_inpage_standard, this, true)
        visibility = View.INVISIBLE

        val attrsArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.VirtusizeInPageStandard,
            0,
            0
        )
        val buttonStyle = attrsArray.getInt(
            R.styleable.VirtusizeInPageStandard_virtusizeInPageStandardStyle,
            VirtusizeViewStyle.NONE.value
        )
        virtusizeViewStyle = VirtusizeViewStyle.values().firstOrNull { it.value == buttonStyle } ?: VirtusizeViewStyle.NONE
        virtusizeButtonBackgroundColor = attrsArray.getColor(R.styleable.VirtusizeInPageStandard_inPageStandardButtonBackgroundColor, 0)
        horizontalMargin = attrsArray.getDimension(
            R.styleable.VirtusizeInPageStandard_inPageStandardHorizontalMargin,
            -1f
        ).toInt()
        attrsArray.recycle()
        setStyle()
    }

    /**
     * @see VirtusizeView.setup
     */
    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        super.setup(params, messageHandler)
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
    }

    /**
     * @see VirtusizeView.setupProductCheckResponseData
     * @throws VirtusizeErrorType.NullProduct error
     */
    override fun setupProductCheckResponseData(productCheck: ProductCheck) {
        if (virtusizeParams?.virtusizeProduct != null) {
            virtusizeParams?.virtusizeProduct!!.productCheckData = productCheck
            productCheck.data?.let { productCheckResponseData ->
                if (productCheckResponseData.validProduct) {
                    visibility = View.VISIBLE
                    setupConfiguredLocalization()
                    setLoadingScreen(true)
                    inpage_standard_card_view.setOnClickListener {
                        openVirtusizeWebView(context)
                    }
                    inpage_standard_button.setOnClickListener {
                        openVirtusizeWebView(context)
                    }
                }
            }
        } else {
            virtusizeMessageHandler.onError(VirtusizeErrorType.NullProduct.virtusizeError())
            VirtusizeErrorType.NullProduct.throwError()
        }
    }

    /**
     * Sets up the styles for the loading screen and the screen after finishing loading
     * @param loading pass true when it's loading, and pass false when finishing loading
     * @param userBestFitProduct pass the user best fit product to determine whether to display the user product image or not
     */
    private fun setLoadingScreen(loading: Boolean, userBestFitProduct: Product? = null) {
        inpage_standard_store_product_image_view.visibility = if(loading) View.INVISIBLE else View.VISIBLE
        inpage_standard_user_product_image_view.visibility = if(userBestFitProduct == null) View.GONE else View.VISIBLE
        vs_signature_image_view.visibility = if(loading) View.INVISIBLE else View.VISIBLE
        privacy_policy_text.visibility = if(loading) View.INVISIBLE else View.VISIBLE
        vs_icon_image_view.visibility = if(loading) View.VISIBLE else View.GONE
        inpage_standard_loading_text.visibility = if(loading) View.VISIBLE else View.GONE
        if(loading) {
            inpage_standard_loading_text.startAnimation()
            inpage_standard_top_text.visibility = View.GONE
            inpage_standard_bottom_text.visibility = View.GONE
        } else {
            inpage_standard_loading_text.stopAnimation()
            if(!inpage_standard_top_text.text.isNullOrBlank()) {
                inpage_standard_top_text.visibility = View.VISIBLE
            }
            if(!inpage_standard_bottom_text.text.isNullOrBlank()) {
                inpage_standard_bottom_text.visibility = View.VISIBLE
            }
        }

        // If the InPage width is small and when the loading is finished
        if(smallInPageWidth && !loading) {
            if (userBestFitProduct != null) {
                startCrossFadeProductImageViews()
            } else {
                stopCrossFadeProductImageViews()
            }
        }
    }

    /**
     * @see VirtusizeInPageView.setupRecommendationText
     */
    override fun setupRecommendationText(text: String) {
        val splitTexts = text.split("<br>")
        if(splitTexts.size == 2) {
            inpage_standard_top_text.text = splitTexts[0]
            inpage_standard_bottom_text.text = splitTexts[1]
        } else {
            inpage_standard_top_text.text = ""
            inpage_standard_top_text.visibility = View.GONE
            inpage_standard_bottom_text.text = splitTexts[0]
        }
    }

    /**
     * @see VirtusizeInPageView.showErrorScreen
     */
    override fun showErrorScreen() {
        inpage_standard_error_screen_layout.visibility = View.VISIBLE
        inpage_standard_layout.visibility = View.GONE
        inpage_standard_card_view.cardElevation = 0f
        inpage_standard_card_view.setOnClickListener {}
        inpage_standard_button.setOnClickListener {}
    }

    /**
     * Sets up the background color of the button
     * @param color a color int
     */
    fun setButtonBackgroundColor(@ColorInt color: Int) {
        virtusizeButtonBackgroundColor = color
        setStyle()
    }

    /**
     * Sets the product images with the info of the store product and the best fit user product
     * @param storeProduct the store product
     * @param userBestFitProduct the best fit user product. If it's not available, it will be null
     */
    internal fun setProductImages(storeProduct: Product, userBestFitProduct: Product?) {
        val productMap = mutableMapOf<VirtusizeProductImageView, Product>()
        productMap[inpage_standard_store_product_image_view] = storeProduct
        if(userBestFitProduct != null) {
            productMap[inpage_standard_user_product_image_view] = userBestFitProduct
            removeLeftPaddingFromStoreProductImageView()
        } else {
            addLeftPaddingToStoreProductImageView(true)
        }
        CoroutineScope(Dispatchers.IO).launch {
            for (map in productMap.entries) {
                val productImageView = map.key
                val product = map.value
                try {
                    URL(product.getProductImageURL()).openStream().use {
                        val bitmap = BitmapFactory.decodeStream(it)
                        withContext(Dispatchers.Main) {
                            productImageView.setProductImage(bitmap)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        productImageView.setProductPlaceHolderImage(product.productType, product.storeProductMeta?.additionalInfo?.style)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                setLoadingScreen(false, userBestFitProduct)
            }
        }
    }

    /**
     * Adds the left padding to the store product image view
     */
    private fun addLeftPaddingToStoreProductImageView(addExtraPadding: Boolean) {
        val productImageOverlapMargin = resources.getDimension(R.dimen.inpage_standard_product_image_overlap_margin)
        val productImageHorizontalMargin = resources.getDimension(R.dimen.inpage_standard_product_image_horizontal_margin)
        var addedPadding = productImageHorizontalMargin.toInt()
        if(addExtraPadding) {
            addedPadding -= productImageOverlapMargin.toInt()
        }
        inpage_standard_store_product_image_view.setPadding(addedPadding, 0, 0, 0)
    }

    private fun removeLeftPaddingFromStoreProductImageView() {
        inpage_standard_store_product_image_view.setPadding(0, 0, 0, 0)
    }

    /**
     * Sets the InPage Standard style corresponding to [VirtusizeViewStyle] and [horizontalMargin]
     */
    private fun setStyle() {
        // Set Virtusize default style
        when {
            virtusizeButtonBackgroundColor!= 0 -> {
                setSizeCheckButtonBackgroundTint(virtusizeButtonBackgroundColor)
            }
            virtusizeViewStyle == VirtusizeViewStyle.TEAL -> {
                setSizeCheckButtonBackgroundTint(ContextCompat.getColor(context, R.color.virtusizeTeal))
            }
            else -> {
                setSizeCheckButtonBackgroundTint(ContextCompat.getColor(context, R.color.color_gray_900))
            }
        }

        // Set horizontal margins
        val inPageStandardFooterTopMargin = if(horizontalMargin >= 2.dpInPx) 10.dpInPx - horizontalMargin else horizontalMargin + 8.dpInPx
        if (horizontalMargin < 0) {
            return
        }
        setupMargins(inpage_standard_card_view, horizontalMargin, horizontalMargin, horizontalMargin, horizontalMargin)
        setupInPageStandardFooterMargins(
            horizontalMargin + 2.dpInPx,
            inPageStandardFooterTopMargin,
            horizontalMargin + 2.dpInPx,
            0
        )

        viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (viewTreeObserver.isAlive) {
                    viewTreeObserver.removeOnPreDrawListener(this)
                }
                if (width < 411.dpInPx) {
                    smallInPageWidth = true
                }
                return true
            }
        })

    }

    /**
     * Starts the cross fade animation to display the user and store product images in an alternating way
     */
    private fun startCrossFadeProductImageViews() {
        // Remove the settings for layout_toEndOf and layout_toRightOf
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        inpage_standard_store_product_image_view.layoutParams = params
        // Add left padding to the store image to math the position of the user image
        addLeftPaddingToStoreProductImageView(false)
        // Remove any margins to the user product image
        setupMargins(inpage_standard_user_product_image_view, 0, 0, 0, 0)
        if(crossFadeRunnable == null) {
            crossFadeRunnable = Runnable {
                if(inpage_standard_user_product_image_view.visibility == View.VISIBLE) {
                    // Make sure the store product image is invisible when the animation starts
                    inpage_standard_store_product_image_view.visibility == View.INVISIBLE
                    fadeInAnimation(inpage_standard_store_product_image_view, inpage_standard_user_product_image_view)
                    fadeOutAnimation(inpage_standard_user_product_image_view)
                } else {
                    fadeInAnimation(inpage_standard_user_product_image_view, inpage_standard_store_product_image_view)
                    fadeOutAnimation(inpage_standard_store_product_image_view)
                }
            }
            crossFadeHandler.postDelayed(crossFadeRunnable!!, 2500)
        }
    }


    /**
     * Stops the cross fade animation
     */
    private fun stopCrossFadeProductImageViews() {
        // Remove the runnable from the handler
        crossFadeRunnable?.let { crossFadeHandler.removeCallbacks(it) }
        crossFadeRunnable = null
        // Make sure the alpha values for product images are back to 1f if they got changed during the animation.=
        inpage_standard_user_product_image_view.alpha = 1f
        inpage_standard_store_product_image_view.alpha = 1f
    }

    /**
     * Fades in an image
     * @param imageViewOne the image to be faded in
     * @param imageViewTwo the image to be set invisible after the animation is done
     */
    private fun fadeInAnimation(imageViewOne: VirtusizeProductImageView, imageViewTwo: VirtusizeProductImageView) {
        imageViewOne.apply {
            alpha = 0f
            visibility = VISIBLE

            animate()
                .alpha(1f)
                .setDuration(crossFadeAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        imageViewTwo.visibility = INVISIBLE
                    }
                })
        }
    }

    /**
     * Fades out an image
     * @param imageView the image to be faded out
     */
    private fun fadeOutAnimation(imageView: VirtusizeProductImageView) {
        imageView.apply {
            animate()
                .alpha(0f)
                .setDuration(crossFadeAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        crossFadeRunnable?.let { crossFadeHandler.postDelayed(it, 2500) }
                    }
                })
        }
    }

    /**
     * Sets the background color of the size check button
     */
    private fun setSizeCheckButtonBackgroundTint(color: Int) {
        var drawable = ContextCompat.getDrawable(context, R.drawable.button_background_white)
        drawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(drawable, color)
        ViewCompat.setBackground(
            inpage_standard_button,
            drawable
        )
    }

    /**
     * Sets up the text fonts, localization, and UI dimensions based on the configured context
     */
    private fun setupConfiguredLocalization() {
        FontUtils.setTypeFaces(
            context,
            mutableListOf(
                inpage_standard_top_text,
                inpage_standard_button,
                privacy_policy_text
            ),
            virtusizeParams?.language,
            FontUtils.FontType.REGULAR
        )
        FontUtils.setTypeFaces(
            context,
            mutableListOf(
                inpage_standard_loading_text,
                inpage_standard_bottom_text
            ),
            virtusizeParams?.language,
            FontUtils.FontType.BOLD
        )

        val configuredContext = VirtusizeUtils.getConfiguredContext(context, virtusizeParams?.language)
        inpage_standard_button.text = configuredContext?.getText(R.string.virtusize_button_text)
        privacy_policy_text.text = configuredContext?.getText(R.string.virtusize_privacy_policy)
        inpage_standard_loading_text.text = configuredContext?.getText(R.string.inpage_loading_text)
        inpage_standard_error_text.text = configuredContext?.getText(R.string.inpage_long_error_text)

        setConfiguredDimensions(configuredContext)

        if(virtusizeParams?.language == VirtusizeLanguage.JP) {
            inpage_standard_bottom_text.includeFontPadding = true
        }

        privacy_policy_text.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(configuredContext?.getString(R.string.virtusize_privacy_policy_link))
            )
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                virtusizeMessageHandler.onError(VirtusizeErrorType.PrivacyLinkNotOpen.virtusizeError(e.localizedMessage))
            }
        }
    }

    /**
     * Sets up the text sizes and UI dimensions based on the configured context
     */
    private fun setConfiguredDimensions(configuredContext: ContextWrapper?) {
        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_normal_textSize)?.let {
            inpage_standard_top_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_bold_textSize)?.let {
            inpage_standard_loading_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
            inpage_standard_bottom_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_default_textSize)?.let {
            inpage_standard_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
            inpage_standard_error_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_privacy_policy_textSize)?.let {
            privacy_policy_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_top_text_marginBottom)?.let {
            setupMargins(inpage_standard_top_text, 0, 0, 0, it.toInt())
            inpage_standard_top_text.setLineSpacing(it, 1f)
            inpage_standard_bottom_text.setLineSpacing(it, 1f)
        }
    }

    /**
     * Sets up the margins for a view
     */
    private fun setupMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        val layoutParams: MarginLayoutParams = view.layoutParams as MarginLayoutParams
        layoutParams.setMargins(left, top, right, bottom)
        view.requestLayout()
    }

    /**
     * Sets up the InPage Standard footer margins
     */
    private fun setupInPageStandardFooterMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(left, top, right, bottom)
        inpage_standard_footer.layoutParams = layoutParams
    }
}