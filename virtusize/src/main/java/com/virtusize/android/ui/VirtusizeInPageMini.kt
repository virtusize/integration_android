package com.virtusize.android.ui

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.virtusize.android.R
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.VirtusizeViewStyle
import com.virtusize.android.databinding.ViewInpageMiniBinding
import com.virtusize.android.util.ConfigurationUtils
import com.virtusize.android.util.FontUtils
import com.virtusize.android.util.rightDrawable
import com.virtusize.android.util.spToPx

class VirtusizeInPageMini
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : VirtusizeInPageView(context, attrs, defStyleAttr) {
        /**
         * @see VirtusizeView.clientProduct
         */
        override var clientProduct: VirtusizeProduct? = null

        /**
         * @see VirtusizeView.virtusizeParams
         */
        override lateinit var virtusizeParams: VirtusizeParams

        /**
         * @see VirtusizeView.virtusizeMessageHandler
         */
        override lateinit var virtusizeMessageHandler: VirtusizeMessageHandler

        /**
         * @see VirtusizeView.virtusizeDialogFragment
         */
        override lateinit var virtusizeDialogFragment: VirtusizeWebViewFragment

        // The VirtusizeViewStyle that clients can choose to use for this InPage Mini view
        var virtusizeViewStyle: VirtusizeViewStyle = VirtusizeViewStyle.NONE
            set(value) {
                field = value
                setStyle()
            }

        // The background color for this InPage Mini view
        private var virtusizeBackgroundColor = 0

        // The configured context for localization
        private var configuredContext: ContextWrapper? = null

        private var binding: ViewInpageMiniBinding =
            ViewInpageMiniBinding.inflate(LayoutInflater.from(context), this, true)

        init {
            visibility =
                if (visibility == View.GONE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            val attrsArray =
                context.obtainStyledAttributes(attrs, R.styleable.VirtusizeInPageMini, 0, 0)
            val buttonStyle =
                attrsArray.getInt(
                    R.styleable.VirtusizeInPageMini_virtusizeInPageMiniStyle,
                    VirtusizeViewStyle.NONE.value,
                )
            virtusizeViewStyle = VirtusizeViewStyle.entries.firstOrNull { it.value == buttonStyle }
                ?: VirtusizeViewStyle.NONE
            virtusizeBackgroundColor =
                attrsArray.getColor(R.styleable.VirtusizeInPageMini_inPageMiniBackgroundColor, 0)
            messageTextSize =
                attrsArray.getDimension(
                    R.styleable.VirtusizeInPageMini_inPageMiniMessageTextSize,
                    -1f,
                )
            buttonTextSize =
                attrsArray.getDimension(
                    R.styleable.VirtusizeInPageMini_inPageMiniButtonTextSize,
                    -1f,
                )
            attrsArray.recycle()
            setStyle()
            // Coil GIF loading
            val imageLoader =
                ImageLoader.Builder(context)
                    .components {
                        add(GifDecoder.Factory())
                    }
                    .build()
            val request =
                ImageRequest.Builder(context)
                    .data(R.drawable.virtusize_loading)
                    .target(binding.gifImageView)
                    .build()
            imageLoader.enqueue(request)
        }

        /**
         * @see VirtusizeView.initialSetup
         */
        override fun initialSetup(
            product: VirtusizeProduct,
            params: VirtusizeParams,
            messageHandler: VirtusizeMessageHandler,
        ) {
            super.initialSetup(product, params, messageHandler)

            // Reset root visibility when setting up a new product
            // This ensures the view shows properly after switching from an invalid product
            binding.root.visibility = View.VISIBLE
        }

        /**
         * @see VirtusizeView.setProductWithProductCheckData
         * @throws VirtusizeErrorType.NullProduct error
         */
        override fun setProductWithProductCheckData(productWithPDC: VirtusizeProduct) {
            super.setProductWithProductCheckData(productWithPDC)
            if (clientProduct!!.externalId == productWithPDC.externalId) {
                clientProduct!!.productCheckData = productWithPDC.productCheckData
                visibility = View.VISIBLE
                binding.gifImageLayout.visibility = View.GONE
                binding.inpageMiniLayout.visibility = View.VISIBLE
                setupConfiguredLocalization()
                setLoadingScreen(true)
                setOnClickListener {
                    openVirtusizeWebView(context, clientProduct!!)
                }
                binding.inpageMiniButton.setOnClickListener {
                    openVirtusizeWebView(context, clientProduct!!)
                }
            }
        }

        /**
         * @see VirtusizeInPageView.setLanguage
         */
        override fun setLanguage(language: VirtusizeLanguage) {
            configuredContext = ConfigurationUtils.getConfiguredContext(context, language)
            binding.inpageMiniButton.text = configuredContext?.getText(R.string.virtusize_button_text)
            binding.inpageMiniLoadingText.text = configuredContext?.getText(R.string.inpage_loading_text)
            setConfiguredDimensions()
            binding.inpageMiniText.includeFontPadding = language == VirtusizeLanguage.JP
            // Optionally update error text if visible
            if (
                binding.inpageMiniText.visibility == View.VISIBLE &&
                binding.inpageMiniText.text == configuredContext?.getText(R.string.inpage_short_error_text)
            ) {
                binding.inpageMiniText.text = configuredContext?.getText(R.string.inpage_short_error_text)
            }
        }

        /**
         * @see VirtusizeInPageView.setRecommendationText
         */
        override fun setRecommendationText(
            externalProductId: String,
            text: String,
        ) {
            if (clientProduct!!.externalId != externalProductId) {
                return
            }
            binding.inpageMiniText.text = text
            setLoadingScreen(false)
        }

        /**
         * @see VirtusizeInPageView.showInPageError
         */
        override fun showInPageError(
            externalProductId: String?,
            error: VirtusizeError?,
        ) {
            if (clientProduct!!.externalId != externalProductId) {
                return
            }

            if (error?.type == VirtusizeErrorType.InvalidProduct) {
                binding.root.visibility = View.GONE
                return
            }

            binding.gifImageLayout.visibility = View.GONE
            binding.inpageMiniLayout.visibility = View.VISIBLE
            binding.inpageMiniLoadingText.visibility = View.GONE
            binding.inpageMiniText.visibility = View.VISIBLE
            binding.inpageMiniText.text =
                configuredContext?.getText(
                    R.string.inpage_short_error_text,
                )
            binding.inpageMiniText.setTextColor(
                ContextCompat.getColor(context, R.color.color_gray_700),
            )
            binding.inpageMiniImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_virtusize_error_hanger,
                ),
            )
            setOnClickListener {}
        }

        /**
         * Sets up the background color of the InPage Mini
         * @param color a color int
         */
        fun setInPageMiniBackgroundColor(
            @ColorInt color: Int,
        ) {
            virtusizeBackgroundColor = color
            setStyle()
        }

        /**
         * Sets up the styles for the loading screen and the screen after finishing loading
         * @param loading pass true when it's loading, and pass false when finishing loading
         */
        private fun setLoadingScreen(loading: Boolean) {
            if (loading) {
                binding.inpageMiniLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.virtusizeWhite,
                    ),
                )
                binding.inpageMiniLoadingText.startAnimation()
            } else {
                binding.inpageMiniLayout.setBackgroundColor(virtusizeBackgroundColor)
                binding.inpageMiniLoadingText.stopAnimation()
                setStyle()
            }
            FontUtils.setTypeFace(
                context,
                binding.inpageMiniLoadingText,
                virtusizeParams.language,
                if (loading) FontUtils.FontType.BOLD else FontUtils.FontType.REGULAR,
            )
            binding.inpageMiniImageView.visibility = if (loading) View.VISIBLE else View.GONE
            binding.inpageMiniText.visibility = if (loading) View.GONE else View.VISIBLE
            binding.inpageMiniLoadingText.visibility = if (loading) View.VISIBLE else View.GONE
            binding.inpageMiniButton.visibility = if (loading) View.GONE else View.VISIBLE
        }

        /**
         * Sets the InPage Mini style corresponding to [VirtusizeViewStyle]
         */
        override fun setStyle() {
            when {
                virtusizeBackgroundColor != 0 -> {
                    binding.inpageMiniLayout.setBackgroundColor(virtusizeBackgroundColor)
                    binding.inpageMiniButton.setTextColor(virtusizeBackgroundColor)
                    setButtonRightArrowColor(virtusizeBackgroundColor)
                }
                virtusizeViewStyle == VirtusizeViewStyle.TEAL -> {
                    binding.inpageMiniLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.virtusizeTeal,
                        ),
                    )
                    binding.inpageMiniButton.setTextColor(
                        ContextCompat.getColor(context, R.color.virtusizeTeal),
                    )
                    setButtonRightArrowColor(ContextCompat.getColor(context, R.color.virtusizeTeal))
                }
                else -> {
                    binding.inpageMiniLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.virtusizeBlack,
                        ),
                    )
                    binding.inpageMiniButton.setTextColor(
                        ContextCompat.getColor(context, R.color.virtusizeBlack),
                    )
                    setButtonRightArrowColor(ContextCompat.getColor(context, R.color.virtusizeBlack))
                }
            }
        }

        /**
         * Sets up the color of the right arrow in the button
         */
        private fun setButtonRightArrowColor(color: Int) {
            var drawable = ContextCompat.getDrawable(context, R.drawable.ic_virtusize_arrow_right_black)
            drawable = DrawableCompat.wrap(drawable!!)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            DrawableCompat.setTint(drawable, color)

            binding.inpageMiniButton.setCompoundDrawables(null, null, drawable, null)
        }

        /**
         * Sets up the text fonts, localization, and UI dimensions based on the configured context
         */
        private fun setupConfiguredLocalization() {
            FontUtils.setTypeFaces(
                context,
                mutableListOf(
                    binding.inpageMiniText,
                    binding.inpageMiniButton,
                ),
                virtusizeParams.language,
                FontUtils.FontType.REGULAR,
            )
            configuredContext =
                ConfigurationUtils.getConfiguredContext(
                    context,
                    virtusizeParams.language,
                )
            binding.inpageMiniButton.text =
                configuredContext?.getText(
                    R.string.virtusize_button_text,
                )
            binding.inpageMiniLoadingText.text =
                configuredContext?.getText(R.string.inpage_loading_text)
            setConfiguredDimensions()

            if (virtusizeParams.language == VirtusizeLanguage.JP) {
                binding.inpageMiniText.includeFontPadding = true
            }
        }

        /**
         * Sets up text sizes based on the configured context
         */
        private fun setConfiguredDimensions() {
            val additionalSize = if (virtusizeParams.language == VirtusizeLanguage.EN) 2f.spToPx else 0f
            if (messageTextSize != -1f) {
                binding.inpageMiniLoadingText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    messageTextSize + additionalSize,
                )
                binding.inpageMiniText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    messageTextSize + additionalSize,
                )
            } else {
                configuredContext?.resources?.getDimension(
                    R.dimen.virtusize_inpage_mini_message_textSize,
                )?.let {
                    binding.inpageMiniLoadingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                    binding.inpageMiniText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }
            }
            if (buttonTextSize != -1f) {
                val size = buttonTextSize + additionalSize
                binding.inpageMiniButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
                binding.inpageMiniButton.rightDrawable(
                    R.drawable.ic_virtusize_arrow_right_black,
                    0.8f * size / 2,
                    0.8f * size,
                )
            } else {
                configuredContext?.resources?.getDimension(
                    R.dimen.virtusize_inpage_default_textSize,
                )
                    ?.let {
                        binding.inpageMiniButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                    }
            }
        }
    }
