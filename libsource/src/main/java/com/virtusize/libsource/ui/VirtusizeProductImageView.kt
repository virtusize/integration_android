package com.virtusize.libsource.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.virtusize.libsource.R
import com.virtusize.libsource.util.dpInPx
import com.virtusize.libsource.util.getDrawableResourceByName
import kotlinx.android.synthetic.main.view_product_image.view.*

/**
 * A custom class for a product image view
 */
internal class VirtusizeProductImageView(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    // The product type to determine the UI style
    private var productImageType: ProductImageType = ProductImageType.STORE

    // This enum contains the possible product types
    enum class ProductImageType {
        STORE, USER
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_product_image, this, true)
        orientation = VERTICAL

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.VirtusizeProductImageView,
            0,
            0
        )

        productImageType = ProductImageType.values()[
            typedArray.getInt(
                R.styleable.VirtusizeProductImageView_productImageType,
                0
            )
        ]

        if (productImageType == ProductImageType.USER) {
            inpageBorderImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_image_border_green_dash
                )
            )
        } else {
            inpageBorderImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_image_border_gray
                )
            )
        }

        typedArray.recycle()
    }

    /**
     * Sets the product image
     * @param bitmap the bitmap of the image
     */
    fun setProductImage(bitmap: Bitmap) {
        inpageProductImageView.setImageBitmap(bitmap)
        inpageProductImageView.setPadding(0, 0, 0, 0)
    }

    /**
     * Sets the product placeholder image by the product type and style
     * @param productType the product type, which is fetched from the store product info
     * @param style the product style, which is fetched from the store product info
     */
    fun setProductPlaceHolderImage(productType: Int?, style: String?) {
        if (productImageType == ProductImageType.STORE) {
            inpageProductCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_gray_200
                )
            )
        }
        inpageProductImageView.setImageDrawable(getProductPlaceholderImage(productType, style))
        inpageProductImageView.setPadding(6.dpInPx, 6.dpInPx, 6.dpInPx, 6.dpInPx)
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
        if (productTypeImageWithStyle != null) {
            productPlaceholderImage = productTypeImageWithStyle
        }
        if (productImageType == ProductImageType.USER) {
            productPlaceholderImage?.setTint(ContextCompat.getColor(context, R.color.virtusizeTeal))
        } else {
            productPlaceholderImage?.setTint(
                ContextCompat.getColor(
                    context,
                    R.color.virtusizeBlack
                )
            )
        }
        return productPlaceholderImage
    }
}
