package com.virtusize.libsource.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.*
import com.virtusize.libsource.R
import com.virtusize.libsource.VirtusizeRepository
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.util.*
import kotlinx.android.synthetic.main.view_inpage_standard.view.*


class VirtusizeInPageStandard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VirtusizeInPageView(context, attrs, defStyleAttr) {

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
    override var virtusizeDialogFragment = VirtusizeWebViewFragment()
        private set

    // If the width of the InPage is small than 411dp, the value is true
    private var smallInPageWidth = false

    // The duration of how long the cross fade animation for product images should be
    private val crossFadeAnimationDuration = 750
    // The cross fade animation Runnable
    private var crossFadeRunnable: Runnable? = null
    // The cross fade animation Handler
    private var crossFadeHandler: Handler = Handler(Looper.getMainLooper())

    private var userBestFitProduct: Product? = null

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

    private var viewModel: VirtusizeInPageStandardViewModel? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_inpage_standard, this, true)
        visibility = if (visibility == View.GONE) {
            View.GONE
        } else {
            View.INVISIBLE
        }

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
        virtusizeViewStyle = VirtusizeViewStyle.values().firstOrNull { it.value == buttonStyle }
            ?: VirtusizeViewStyle.NONE
        virtusizeButtonBackgroundColor = attrsArray.getColor(
            R.styleable.VirtusizeInPageStandard_inPageStandardButtonBackgroundColor,
            0
        )
        horizontalMargin = attrsArray.getDimension(
            R.styleable.VirtusizeInPageStandard_inPageStandardHorizontalMargin,
            -1f
        ).toInt()
        messageTextSize = attrsArray.getDimension(R.styleable.VirtusizeInPageStandard_inPageStandardMessageTextSize, -1f)
        buttonTextSize = attrsArray.getDimension(R.styleable.VirtusizeInPageStandard_inPageStandardButtonTextSize, -1f)

        attrsArray.recycle()

        inpageCardView.onSizeChanged { width, height ->
            if (width < 411.dpInPx) {
                smallInPageWidth = true
            }
        }

        setStyle()
    }

    /**
     * @see VirtusizeView.setup
     */
    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        super.setup(params, messageHandler)
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return VirtusizeInPageStandardViewModel(
                    VirtusizeRepository(
                        context,
                        virtusizeMessageHandler
                    )
                ) as T
            }
        }

        viewModel = viewModelFactory.create(VirtusizeInPageStandardViewModel::class.java)

        (context as? LifecycleOwner)?.apply {
            viewModel?.productImageBitmapLiveData?.observe(this, { productImageViewBitMapPair ->
                productImageViewBitMapPair.first.setProductImage(productImageViewBitMapPair.second)
            })

            viewModel?.productLiveData?.observe(this, { productImageViewDataPair ->
                productImageViewDataPair.first.setProductPlaceHolderImage(
                    productImageViewDataPair.second.productType,
                    productImageViewDataPair.second.storeProductMeta?.additionalInfo?.style
                )
            })

            viewModel?.finishLoadingProductImage?.observe(this, { finishLoading ->
                if (finishLoading) {
                    setLoadingScreen(false, userBestFitProduct)
                }
            })
        }
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
                    inpageCardView.setOnClickListener {
                        openVirtusizeWebView(context)
                    }
                    inpageButton.setOnClickListener {
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
        inpageStoreProductImageView.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        inpageUserProductImageView.visibility =
            if (userBestFitProduct == null) View.GONE else View.VISIBLE
        vsSignatureImageView.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        privacyPolicyText.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        inpageVSIconImageView.visibility = if (loading) View.VISIBLE else View.GONE
        inpageLoadingText.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            inpageLoadingText.startAnimation()
            inpageTopText.visibility = View.GONE
            inpageBottomText.visibility = View.GONE
        } else {
            inpageLoadingText.stopAnimation()
            if (!inpageTopText.text.isNullOrBlank()) {
                inpageTopText.visibility = View.VISIBLE
            }
            if (!inpageBottomText.text.isNullOrBlank()) {
                inpageBottomText.visibility = View.VISIBLE
            }
        }

        // If the InPage width is small and when the loading is finished
        if (smallInPageWidth && !loading) {
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
        if (splitTexts.size == 2) {
            inpageTopText.text = splitTexts[0]
            inpageBottomText.text = splitTexts[1]
        } else {
            inpageTopText.text = ""
            inpageTopText.visibility = View.GONE
            inpageBottomText.text = splitTexts[0]
        }
    }

    /**
     * @see VirtusizeInPageView.showErrorScreen
     */
    override fun showErrorScreen() {
        inpageErrorScreenLayout.visibility = View.VISIBLE
        inpageLayout.visibility = View.GONE
        inpageCardView.cardElevation = 0f
        inpageCardView.setOnClickListener {}
        inpageButton.setOnClickListener {}
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
        this.userBestFitProduct = userBestFitProduct
        val productMap = mutableMapOf<VirtusizeProductImageView, Product>()
        productMap[inpageStoreProductImageView] = storeProduct
        if (userBestFitProduct != null) {
            productMap[inpageUserProductImageView] = userBestFitProduct
            removeLeftPaddingFromStoreProductImageView()
        } else {
            addLeftPaddingToStoreProductImageView(true)
        }
        viewModel?.loadProductImages(productMap)
    }

    /**
     * Adds the left padding to the store product image view
     */
    private fun addLeftPaddingToStoreProductImageView(addExtraPadding: Boolean) {
        val productImageOverlapMargin =
            resources.getDimension(R.dimen.virtusize_inpage_standard_product_image_overlap_margin)
        val productImageHorizontalMargin =
            resources.getDimension(R.dimen.virtusize_inpage_standard_product_image_horizontal_margin)
        var addedPadding = productImageHorizontalMargin.toInt()
        if (addExtraPadding) {
            addedPadding -= productImageOverlapMargin.toInt()
        }
        inpageStoreProductImageView.setPadding(addedPadding, 0, 0, 0)
    }

    private fun removeLeftPaddingFromStoreProductImageView() {
        inpageStoreProductImageView.setPadding(0, 0, 0, 0)
    }

    /**
     * Sets the InPage Standard style corresponding to [VirtusizeViewStyle] and [horizontalMargin]
     */
    override fun setStyle() {
        // Set Virtusize default style
        when {
            virtusizeButtonBackgroundColor != 0 -> {
                setSizeCheckButtonBackgroundTint(virtusizeButtonBackgroundColor)
            }
            virtusizeViewStyle == VirtusizeViewStyle.TEAL -> {
                setSizeCheckButtonBackgroundTint(
                    ContextCompat.getColor(
                        context,
                        R.color.virtusizeTeal
                    )
                )
            }
            else -> {
                setSizeCheckButtonBackgroundTint(
                    ContextCompat.getColor(
                        context,
                        R.color.color_gray_900
                    )
                )
            }
        }

        // Set the background color of the inpage card view
        inpageCardView.setBackgroundColor(ContextCompat.getColor(
            context,
            R.color.virtusizeWhite
        ))

        // Set horizontal margins
        val inPageStandardFooterTopMargin =
            if (horizontalMargin >= 2.dpInPx) 10.dpInPx - horizontalMargin else horizontalMargin + 8.dpInPx
        if (horizontalMargin < 0) {
            return
        }
        setupMargins(
            inpageCardView,
            horizontalMargin,
            horizontalMargin,
            horizontalMargin,
            horizontalMargin
        )
        setupInPageStandardFooterMargins(
            horizontalMargin + 2.dpInPx,
            inPageStandardFooterTopMargin,
            horizontalMargin + 2.dpInPx,
            0
        )
    }

    /**
     * Starts the cross fade animation to display the user and store product images in an alternating way
     */
    private fun startCrossFadeProductImageViews() {
        // Remove the settings for layout_toEndOf and layout_toRightOf
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        inpageStoreProductImageView.layoutParams = params
        // Add left padding to the store image to math the position of the user image
        addLeftPaddingToStoreProductImageView(false)
        // Remove any margins to the user product image
        setupMargins(inpageUserProductImageView, 8.dpInPx, 0, 0, 0)
        if (crossFadeRunnable == null) {
            crossFadeRunnable = Runnable {
                if (inpageUserProductImageView.visibility == View.VISIBLE) {
                    // Make sure the store product image is invisible when the animation starts
                    inpageStoreProductImageView.visibility = View.INVISIBLE
                    fadeInAnimation(inpageStoreProductImageView, inpageUserProductImageView)
                    fadeOutAnimation(inpageUserProductImageView)
                } else {
                    fadeInAnimation(inpageUserProductImageView, inpageStoreProductImageView)
                    fadeOutAnimation(inpageStoreProductImageView)
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
        inpageUserProductImageView.alpha = 1f
        inpageStoreProductImageView.alpha = 1f
    }

    /**
     * Fades in an image
     * @param imageViewOne the image to be faded in
     * @param imageViewTwo the image to be set invisible after the animation is done
     */
    private fun fadeInAnimation(
        imageViewOne: VirtusizeProductImageView,
        imageViewTwo: VirtusizeProductImageView
    ) {
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
            inpageButton,
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
                inpageTopText,
                inpageButton,
                privacyPolicyText
            ),
            virtusizeParams?.language,
            FontUtils.FontType.REGULAR
        )
        FontUtils.setTypeFaces(
            context,
            mutableListOf(
                inpageLoadingText,
                inpageBottomText
            ),
            virtusizeParams?.language,
            FontUtils.FontType.BOLD
        )

        val configuredContext = VirtusizeUtils.getConfiguredContext(
            context,
            virtusizeParams?.language
        )
        inpageButton.text = configuredContext?.getText(R.string.virtusize_button_text)
        privacyPolicyText.text = configuredContext?.getText(R.string.virtusize_privacy_policy)
        inpageLoadingText.text = configuredContext?.getText(R.string.inpage_loading_text)
        inpageErrorText.text = configuredContext?.getText(R.string.inpage_long_error_text)

        setConfiguredDimensions(configuredContext)

        if (virtusizeParams?.language == VirtusizeLanguage.JP) {
            inpageBottomText.includeFontPadding = true
        }

        privacyPolicyText.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(configuredContext?.getString(R.string.virtusize_privacy_policy_link))
            )
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                virtusizeMessageHandler.onError(
                    VirtusizeErrorType.PrivacyLinkNotOpen.virtusizeError(
                        extraMessage = e.localizedMessage
                    )
                )
            }
        }
    }

    /**
     * Sets up the text sizes and UI dimensions based on the configured context
     */
    private fun setConfiguredDimensions(configuredContext: ContextWrapper?) {
        val additionalSize = if(virtusizeParams?.language == VirtusizeLanguage.EN) 2f.spToPx else 0f

        if (messageTextSize != -1f) {
            inpageTopText.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize + 2f.spToPx + additionalSize)
            inpageLoadingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize + 6f.spToPx + additionalSize)
            inpageBottomText.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize + 6f.spToPx + additionalSize)
            inpageErrorText.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize + additionalSize)
            privacyPolicyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize + additionalSize)
        } else {
            configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_normal_textSize)
                ?.let {
                    inpageTopText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }

            configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_bold_textSize)
                ?.let {
                    inpageLoadingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                    inpageBottomText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }

            configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_default_textSize)
                ?.let {
                    inpageErrorText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }

            configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_privacy_policy_textSize)
                ?.let {
                    privacyPolicyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }
        }

        if (buttonTextSize != -1f) {
            val size = buttonTextSize + additionalSize
            inpageButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            inpageButton.rightDrawable(R.drawable.ic_arrow_right_white, 0.8f * size / 2, 0.8f * size)
        } else {
            configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_default_textSize)
                ?.let {
                    inpageButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }
        }

        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_standard_top_text_marginBottom)
            ?.let {
                setupMargins(inpageTopText, 0, 0, 0, it.toInt())
                inpageTopText.setLineSpacing(it, 1f)
                inpageBottomText.setLineSpacing(it, 1f)
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
        inpageFooter.layoutParams = layoutParams
    }
}