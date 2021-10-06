package com.virtusize.android.ui.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.virtusize.android.ui.R
import com.virtusize.android.ui.databinding.VirtusizeAvatarBinding
import com.virtusize.android.ui.utils.dp
import kotlin.math.min

open class VirtusizeAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val avatarGapWidth = 6.dp
    }

    var vsAvatarSize = VirtusizeAvatarSize.SMALL
        set(value) {
            field = value
            invalidate()
        }

    var vsAvatarBorderStyle = VirtusizeAvatarBorderStyle.SOLID
        set(value) {
            field = value
            invalidate()
        }

    var vsAvatarBorderWidth = VirtusizeAvatarBorderWidth.THIN
        set(value) {
            field = value
            invalidate()
        }

    var vsAvatarBorderColor: Int = R.color.vs_gray_800
        set(value) {
            field = value
            invalidate()
        }

    var vsAvatarGapEnabled: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private val binding = VirtusizeAvatarBinding.inflate(LayoutInflater.from(context), this, true)

    private var gapPaint = Paint()
    private var borderPaint = Paint()
    private var avatarImageViewCanvas = Canvas()
    private var srcImageBitmap: Bitmap? = null
    private var dstBitmap: Bitmap? = null
    private var dstBitmapSize: Int = 0

    init {
        setWillNotDraw(false)

        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeAvatar, 0, 0)

        val avatarImageResId = attrsArray.getResourceId(
            R.styleable.AppCompatImageView_android_src,
            -1
        )
        setAvatarImage(avatarImageResId)

        val avatarSizeOrdinal = attrsArray.getInt(
            R.styleable.VirtusizeAvatar_avatarSize,
            VirtusizeAvatarSize.SMALL.ordinal
        )
        val avatarSize =
            VirtusizeAvatarSize.values().firstOrNull { it.ordinal == avatarSizeOrdinal }
        avatarSize?.let { size ->
            vsAvatarSize = size
        }

        val avatarBorderStyleOrdinal = attrsArray.getInt(
            R.styleable.VirtusizeAvatar_avatarBorderStyle,
            VirtusizeAvatarBorderWidth.THIN.ordinal
        )
        val avatarBorderStyle = VirtusizeAvatarBorderStyle.values()
            .firstOrNull { it.ordinal == avatarBorderStyleOrdinal }
        avatarBorderStyle?.let { borderStyle ->
            vsAvatarBorderStyle = borderStyle
        }

        val avatarBorderWidthOrdinal = attrsArray.getInt(
            R.styleable.VirtusizeAvatar_avatarBorderWidth,
            VirtusizeAvatarBorderWidth.THIN.ordinal
        )
        val avatarBorderWidth = VirtusizeAvatarBorderWidth.values()
            .firstOrNull { it.ordinal == avatarBorderWidthOrdinal }
        avatarBorderWidth?.let { borderWidth ->
            vsAvatarBorderWidth = borderWidth
        }

        vsAvatarBorderColor = attrsArray.getColor(
            R.styleable.VirtusizeAvatar_avatarBorderColor,
            ContextCompat.getColor(context, R.color.vs_gray_800)
        )
        vsAvatarGapEnabled =
            attrsArray.getBoolean(R.styleable.VirtusizeAvatar_avatarGapEnabled, true)

        attrsArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        srcImageBitmap?.let { srcImageBitmap ->
            setAvatarBitmaps()

            // Draw the avatar image bitmap
            avatarImageViewCanvas.drawBitmap(
                srcImageBitmap,
                ((dstBitmapSize - srcImageBitmap.width) / 2.0).toFloat(),
                ((dstBitmapSize - srcImageBitmap.height) / 2.0).toFloat(),
                null
            )

            val dstBitmapWidthRadius = (dstBitmap?.width ?: 0) / 2
            val dstBitmapHeightRadius = (dstBitmap?.height ?: 0) / 2

            if (vsAvatarGapEnabled) {
                gapPaint.style = Paint.Style.STROKE
                gapPaint.strokeWidth = avatarGapWidth * 2
                gapPaint.color = Color.WHITE

                // Draw the rounded gap
                avatarImageViewCanvas.drawCircle(
                    dstBitmapWidthRadius.toFloat(),
                    dstBitmapHeightRadius.toFloat(),
                    dstBitmapWidthRadius.toFloat(),
                    gapPaint
                )
            }

            borderPaint.style = Paint.Style.STROKE
            borderPaint.color = vsAvatarBorderColor
            val avatarBorderWidth = if (vsAvatarBorderWidth == VirtusizeAvatarBorderWidth.THICK) {
                4.dp
            } else {
                2.dp
            }
            borderPaint.strokeWidth = avatarBorderWidth

            if (vsAvatarBorderStyle == VirtusizeAvatarBorderStyle.DASHED) {
                borderPaint.pathEffect =
                    DashPathEffect(floatArrayOf(avatarBorderWidth, avatarBorderWidth), 0f)
            } else if (vsAvatarBorderStyle == VirtusizeAvatarBorderStyle.NONE) {
                borderPaint.color = Color.WHITE
                borderPaint.strokeWidth = 0f
            }

            // Draw the rounded border
            avatarImageViewCanvas.drawCircle(
                dstBitmapWidthRadius.toFloat(),
                dstBitmapHeightRadius.toFloat(),
                dstBitmapWidthRadius.toFloat(),
                borderPaint
            )

            val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, dstBitmap)
            roundedBitmapDrawable.isCircular = true
            roundedBitmapDrawable.setAntiAlias(true)

            binding.avatarImageView.setImageDrawable(roundedBitmapDrawable)
        }
    }

    fun setAvatarImage(@DrawableRes resId: Int) {
        if (resId != -1) {
            srcImageBitmap = BitmapFactory.decodeResource(resources, resId)
            invalidate()
        }
    }

    fun setAvatarImage(imageBitmap: Bitmap) {
        srcImageBitmap = imageBitmap
        invalidate()
    }

    private fun setAvatarBitmaps() {
        srcImageBitmap?.let { imageBitmap ->
            val avatarImageSize = vsAvatarSize.getAvatarSize(context).toInt()
            val ratio = avatarImageSize.toFloat() / min(imageBitmap.width, imageBitmap.height)
            dstBitmapSize = avatarImageSize + avatarGapWidth.toInt() * 2
            srcImageBitmap = if (vsAvatarGapEnabled) {
                val resizedBitmap = Bitmap.createScaledBitmap(
                    imageBitmap,
                    (imageBitmap.width * ratio).toInt(),
                    (imageBitmap.height * ratio).toInt(),
                    false
                )
                Bitmap.createBitmap(resizedBitmap, 0, 0, avatarImageSize, avatarImageSize)
            } else {
                val resizedBitmap = Bitmap.createScaledBitmap(
                    imageBitmap,
                    (imageBitmap.width * ratio).toInt() + avatarGapWidth.toInt() * 2,
                    (imageBitmap.height * ratio).toInt() + avatarGapWidth.toInt() * 2,
                    false
                )
                Bitmap.createBitmap(resizedBitmap, 0, 0, dstBitmapSize, dstBitmapSize)
            }
            dstBitmap = Bitmap.createBitmap(dstBitmapSize, dstBitmapSize, Bitmap.Config.ARGB_8888)
            dstBitmap?.let { dstBitmap ->
                avatarImageViewCanvas = Canvas(dstBitmap)
            }
        }
    }
}
