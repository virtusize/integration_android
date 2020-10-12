package com.virtusize.libsource.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.util.*
import com.virtusize.libsource.util.FontUtils
import com.virtusize.libsource.util.VirtusizeUtils
import com.virtusize.libsource.util.getDrawableResourceByName
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
    var horizontalMargin = -1f
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
        )
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
            throwError(VirtusizeErrorType.NullProduct)
        }
    }

    /**
     * Sets up the styles for the loading screen and the screen after finishing loading
     * @param loading pass true when it's loading, and pass false when finishing loading
     */
    private fun setLoadingScreen(loading: Boolean) {
        inpage_standard_product_border_card_view.visibility = if(loading) View.INVISIBLE else View.VISIBLE
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
     * Sets up the store product image
     * @param imageUrl the image URL that clients provide when setting up the product info
     * @param cloudinaryPublicId the Cloudinary image public ID
     * @param productType the product type, which is fetched from the store product info
     * @param style the product style, which is fetched from the store product info
     */
    internal fun setupProductImage(imageUrl: String?, cloudinaryPublicId: String, productType: Int, style: String?) {
        setProductImageFromURL(imageUrl ?: getCloudinaryImageUrl(cloudinaryPublicId), productType, style)
    }

    /**
     * Sets the InPage Standard style corresponding to [VirtusizeViewStyle] and [horizontalMargin]
     */
    private fun setStyle() {
        // Set Virtusize default style
        if(virtusizeButtonBackgroundColor!= 0) {
            setSizeCheckButtonBackgroundTint(virtusizeButtonBackgroundColor)
        } else if(virtusizeViewStyle == VirtusizeViewStyle.TEAL) {
            setSizeCheckButtonBackgroundTint(ContextCompat.getColor(context, R.color.virtusizeTeal))
        } else {
            setSizeCheckButtonBackgroundTint(ContextCompat.getColor(context, R.color.color_gray_900))
        }

        // Set horizontal margins
        val inPageStandardFooterTopMargin = if(horizontalMargin.toInt() >= 2.dpInPx) 10.dpInPx - horizontalMargin.toInt() else horizontalMargin.toInt() + 8.dpInPx
        if (horizontalMargin < 0f) {
            return
        }
        setupMargins(inpage_standard_card_view, horizontalMargin.toInt(), horizontalMargin.toInt(), horizontalMargin.toInt(), horizontalMargin.toInt())
        setupInPageStandardFooterMargins(
            horizontalMargin.toInt() + 2.dpInPx,
            inPageStandardFooterTopMargin,
            horizontalMargin.toInt() + 2.dpInPx,
            0
        )
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
        inpage_standard_loading_text.text = configuredContext?.getText(R.string.inpage_standard_loading_text)
        inpage_standard_error_text.text = configuredContext?.getText(R.string.inpage_long_error_text)

        setConfiguredDimensions(configuredContext)

        privacy_policy_text.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(configuredContext?.getString(R.string.virtusize_privacy_policy_link))
            )
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.d(Constants.INPAGE_LOG_TAG, e.localizedMessage)
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

    /**
     * Sets up the InPage Standard footer margins
     */
    private fun getCloudinaryImageUrl(cloudinaryPublicId: String): String {
        return "https://res.cloudinary.com/virtusize/image/upload/t_product-large-retina-v1/$cloudinaryPublicId.jpg"
    }

    /**
     * Sets up the store product image from URL
     * @param imageUrl the image URL
     * @param productType the product type, which is fetched from the store product info
     * @param style the product style, which is fetched from the store product info
     */
    private fun setProductImageFromURL(imageUrl: String?, productType: Int?, style: String?) {
        if(imageUrl.isNullOrBlank()) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                URL(imageUrl).openStream().use {
                    val bitmap = BitmapFactory.decodeStream(it)
                    withContext(Dispatchers.Main) {
                        inpage_standard_product_image_view.setImageBitmap(bitmap)
                        inpage_standard_product_image_view.setPadding(0, 0, 0, 0)
                    }
                }
            } catch (e: Exception) {
                Log.e(Constants.INPAGE_LOG_TAG, e.localizedMessage)
                withContext(Dispatchers.Main) {
                    inpage_standard_product_card_view.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.color_gray_200
                        )
                    )
                    inpage_standard_product_image_view.setImageDrawable(
                        getProductPlaceholderImage(productType, style)
                    )
                }
            }
            withContext(Dispatchers.Main) {
                setLoadingScreen(false)
            }
        }
    }

    /**
     * Gets the product placeholder image by the product type and style
     * @param productType the product type, which is fetched from the store product info
     * @param style the product style, which is fetched from the store product info
     * @return a Drawable of product placeholder image
     */
    private fun getProductPlaceholderImage(productType: Int?, style: String?): Drawable? {
        var productPlaceholderImage = context.getDrawableResourceByName(
            "ic_product_type_$productType"
        )
        val productTypeImageWithStyle = context.getDrawableResourceByName(
            "ic_product_type_${productType}_$style"
        )
        if(productTypeImageWithStyle != null) {
            productPlaceholderImage = productTypeImageWithStyle
        }
        return productPlaceholderImage
    }
}