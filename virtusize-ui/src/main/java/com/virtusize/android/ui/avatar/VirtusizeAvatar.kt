package com.virtusize.android.ui.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.virtusize.android.ui.R
import com.virtusize.android.ui.databinding.VirtusizeAvatarBinding
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.virtusize.android.ui.utils.dp
import kotlin.math.min


class VirtusizeAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val avatarGapWidth = 16.dp
    }

    var vsAvatarSize = VirtusizeAvatarSize.SMALL
        set(value) {
            field = value
            setAvatarSize()
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

        vsAvatarBorderColor = attrsArray.getColor(R.styleable.VirtusizeAvatar_avatarBorderColor, ContextCompat.getColor(context, R.color.vs_gray_800))

        attrsArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        srcImageBitmap?.let { srcImageBitmap ->
            avatarImageViewCanvas.drawColor(Color.WHITE)
            
            // Draw the avatar image bitmap
            avatarImageViewCanvas.drawBitmap(
                srcImageBitmap,
                ((dstBitmapSize - srcImageBitmap.width) / 2.0).toFloat(),
                ((dstBitmapSize - srcImageBitmap.height) / 2.0).toFloat(),
                null
            )

            gapPaint.style = Paint.Style.STROKE
            gapPaint.strokeWidth = avatarGapWidth * 2
            gapPaint.color = Color.WHITE

            val dstBitmapWidthRadius = (dstBitmap?.width ?: 0) / 2
            val dstBitmapHeightRadius = (dstBitmap?.height ?: 0) / 2

            // Draw the rounded gap
            avatarImageViewCanvas.drawCircle(
                dstBitmapWidthRadius.toFloat(),
                dstBitmapHeightRadius.toFloat(),
                dstBitmapWidthRadius.toFloat(),
                gapPaint
            )

            borderPaint.style = Paint.Style.STROKE
            borderPaint.color = vsAvatarBorderColor
            val avatarBorderWidth = if (vsAvatarBorderWidth == VirtusizeAvatarBorderWidth.THICK) { 16.dp } else { 8.dp }
            borderPaint.strokeWidth = avatarBorderWidth

            if (vsAvatarBorderStyle == VirtusizeAvatarBorderStyle.DASHED) {
                borderPaint.pathEffect = vsAvatarBorderWidth.getDashPathEffect()
            } else if (vsAvatarBorderStyle == VirtusizeAvatarBorderStyle.NONE) {
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
            setAvatarImageBitmap(BitmapFactory.decodeResource(resources, resId))
            invalidate()
        }
    }

    fun setAvatarImage(imageBitmap: Bitmap) {
        setAvatarImageBitmap(imageBitmap)
        invalidate()
    }

    private fun setAvatarImageBitmap(imageBitmap: Bitmap) {
        srcImageBitmap = imageBitmap
        dstBitmapSize = min(imageBitmap.width, imageBitmap.height) + avatarGapWidth.toInt() * 2
        dstBitmap = Bitmap.createBitmap(dstBitmapSize, dstBitmapSize, Bitmap.Config.ARGB_8888)
        dstBitmap?.let { dstBitmap ->
            avatarImageViewCanvas = Canvas(dstBitmap)
        }
    }

    private fun setAvatarSize() {
        val avatarImageSize = vsAvatarSize.getAvatarSize(context).toInt()
        val avatarImageLayoutParams = LayoutParams(avatarImageSize, avatarImageSize)
        avatarImageLayoutParams.gravity = Gravity.CENTER
        binding.avatarImageCardView.layoutParams = avatarImageLayoutParams
        binding.avatarImageCardView.radius = vsAvatarSize.getAvatarRadiusSize(context)
    }
}