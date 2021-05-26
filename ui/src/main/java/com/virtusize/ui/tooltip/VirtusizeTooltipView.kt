package com.virtusize.ui.tooltip

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.virtusize.ui.R
import com.virtusize.ui.databinding.VirtusizeTooltipBinding
import com.virtusize.ui.utils.dp

class VirtusizeTooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :  FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val arrowHeight = 7.dp
        private val arrowWidth = 13.dp
        private val halfTriangleWidth = arrowWidth / 2
        private val cornerRadius = 6.dp
    }

    private var builder: VirtusizeTooltip.Builder? = null
    private var binding: VirtusizeTooltipBinding? = null
    internal var containerView: View
    private var containerRectF: RectF


    private val tooltipPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var tooltipPath: Path = Path()

    constructor(context: Context, builder: VirtusizeTooltip.Builder, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(context, attrs, defStyleAttr) {
        this.builder = builder
    }

    init {
        // Set it to false to let onDraw to be called
        setWillNotDraw(false)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (builder?.layoutId != null) {
            containerView = inflater.inflate(builder?.layoutId!!, this, true)
        } else {
            binding = VirtusizeTooltipBinding.inflate(inflater, this, true)
            containerView = binding!!.root
            binding!!.tooltipTextView.text = builder?.text ?: resources.getString(R.string.vs_similar_items)
        }
        containerRectF = RectF(0F, 0F, containerView.width.toFloat(), containerView.height.toFloat())
        if (builder?.position == VirtusizeTooltip.Position.BOTTOM) {
            containerRectF.top += arrowHeight
            containerRectF.bottom -= arrowHeight
        }
    }

    override fun onDraw(canvas: Canvas) {
        with(tooltipPath) {
            // Draw the body
            addRoundRect(containerRectF, cornerRadius, cornerRadius, Path.Direction.CW)

            val middleX = calculateArrowXMidPoint(rootView, containerRectF)

            // Draw the triangle (arrow)
            if (builder?.position == VirtusizeTooltip.Position.TOP) {
                moveTo(middleX, containerView.height.toFloat() + arrowHeight)
            } else if (builder?.position == VirtusizeTooltip.Position.BOTTOM) {
                moveTo(middleX, 0f)
            }

            if (builder?.position == VirtusizeTooltip.Position.TOP) {
                lineTo(middleX - halfTriangleWidth, containerRectF.bottom)
                lineTo(middleX + halfTriangleWidth, containerRectF.bottom)
            } else if (builder?.position == VirtusizeTooltip.Position.BOTTOM) {
                lineTo(middleX - halfTriangleWidth, containerRectF.top)
                lineTo(middleX + halfTriangleWidth, containerRectF.top)
            }

            close()
        }

        tooltipPaint.color = ContextCompat.getColor(context, R.color.vs_gray_900)

        canvas.drawPath(tooltipPath, tooltipPaint)
        super.onDraw(canvas)
    }

    private fun calculateArrowXMidPoint(view: View, rectF: RectF): Float {
        var middle = rectF.width() / 2
        builder?.anchorView?.apply {
            middle += (this.x + this.width / 2 - view.x - view.width / 2)
        }
        return middle
    }
}