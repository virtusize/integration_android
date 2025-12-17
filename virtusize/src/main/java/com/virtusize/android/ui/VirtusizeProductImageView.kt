package com.virtusize.android.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.virtusize.android.R
import com.virtusize.android.databinding.ViewProductImageBinding
import com.virtusize.android.util.dpInPx
import com.virtusize.android.util.getDrawableResourceByName

/**
 * A custom class for a product image view
 */
internal class VirtusizeProductImageView(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {
    // The product type to determine the UI style
    private var productImageType: ProductImageType = ProductImageType.STORE

    // This enum contains the possible product types
    enum class ProductImageType {
        STORE,
        USER,
    }

    private val binding = ViewProductImageBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL

        val typedArray =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.VirtusizeProductImageView,
                0,
                0,
            )

        productImageType =
            ProductImageType.values()[
                typedArray.getInt(
                    R.styleable.VirtusizeProductImageView_productImageType,
                    0,
                ),
            ]

        if (productImageType == ProductImageType.USER) {
            binding.inpageBorderImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_virtusize_image_border_green_dash,
                ),
            )
        } else {
            binding.inpageBorderImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_virtusize_image_border_gray,
                ),
            )
        }

        typedArray.recycle()
    }

    /**
     * Sets the product image
     * @param bitmap the bitmap of the image
     */
    fun setProductImage(bitmap: Bitmap) {
        binding.inpageProductImageView.setImageBitmap(bitmap)
        binding.inpageProductImageView.setPadding(0, 0, 0, 0)
    }

    /**
     * Sets the product placeholder image by the product type and style
     * @param productType the product type, which is fetched from the store product info
     * @param style the product style, which is fetched from the store product info
     */
    fun setProductPlaceHolderImage(
        productType: Int?,
        style: String?,
    ) {
        if (productImageType == ProductImageType.STORE) {
            binding.inpageProductCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_gray_200,
                ),
            )
        }
        binding.inpageProductImageView.setImageDrawable(
            getProductPlaceholderImage(productType, style),
        )
        binding.inpageProductImageView.setPadding(6.dpInPx, 6.dpInPx, 6.dpInPx, 6.dpInPx)
    }

    /**
     * Gets the product placeholder image by the product type and style
     * @param productType the product type, which is fetched from the store product info
     * @param style the product style, which is fetched from the store product info
     * @return a Drawable of product placeholder image
     */
    private fun getProductPlaceholderImage(
        productType: Int?,
        style: String?,
    ): Drawable? {
        var productPlaceholderImage =
            context.getDrawableResourceByName(
                "body",
            )
        val productTypeImageWithStyle =
            context.getDrawableResourceByName(
                "ic_product_type_${productType}_$style",
            )
        if (productTypeImageWithStyle != null) {
            productPlaceholderImage = productTypeImageWithStyle
        }
        if (productImageType == ProductImageType.USER) {
            productPlaceholderImage?.setTint(ContextCompat.getColor(context, R.color.virtusizeTeal))
        } else {
            productPlaceholderImage?.setTint(
                ContextCompat.getColor(
                    context,
                    R.color.virtusizeBlack,
                ),
            )
        }
        return productPlaceholderImage
    }
}
