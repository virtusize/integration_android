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
    private val builder: VirtusizeTooltip.Builder,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        internal val arrowHeight = 7.dp
        private val arrowWidth = 13.dp
        private val halfTriangleWidth = arrowWidth / 2
        private val cornerRadius = 6.dp
    }

    private var binding: VirtusizeTooltipBinding? = null
    internal var containerView: View


    private val tooltipPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var tooltipPath: Path = Path()

    init {
        // Set it to false to let onDraw to be called
        setWillNotDraw(false)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (builder.layoutId != null) {
            containerView = inflater.inflate(builder.layoutId!!, this, true)
        } else {
            binding = VirtusizeTooltipBinding.inflate(inflater, this, true)
            containerView = binding!!.root
            binding!!.tooltipTextView.text = builder.text ?: "You can also set \"showOverlay\" prop to show a dark overlay, and \"noBorder\" to remove the border around the carrot"
            if (builder.hideCloseButton) {
                binding!!.closeImageView.visibility = GONE
                binding!!.tooltipTextView.setPadding(8.dp.toInt(), 10.dp.toInt(), 8.dp.toInt(), 10.dp.toInt())
            } else {
                binding!!.tooltipTextView.setPadding(12.dp.toInt(), 10.dp.toInt(), 26.dp.toInt(), 10.dp.toInt())
            }
        }

        // Adjust the padding because the drawing takes up the original space of containerView
        if (builder.position == VirtusizeTooltip.Position.TOP) {

        } else if (builder.position == VirtusizeTooltip.Position.BOTTOM) {
            setPadding(0, arrowHeight.toInt(), 0, 0)
        } else if (builder.position == VirtusizeTooltip.Position.RIGHT) {
            setPadding(arrowHeight.toInt(), 0, 0, 0)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val containerRectF = RectF(0F, 0F, containerView.width.toFloat(), containerView.height.toFloat())
        if (builder.position == VirtusizeTooltip.Position.BOTTOM) {
            containerRectF.top += arrowHeight
            containerRectF.bottom += arrowHeight
        } else if (builder.position == VirtusizeTooltip.Position.RIGHT) {
            containerRectF.left += arrowHeight
            containerRectF.right += arrowHeight
        }

        with(tooltipPath) {
            // Draw the body
            addRoundRect(containerRectF, cornerRadius, cornerRadius, Path.Direction.CW)

            val arrowMiddlePoint = PointF(containerRectF.width() / 2, containerRectF.height() / 2)

            // Draw the triangle (arrow)
            if (!builder.hideArrow) {
                when (builder.position) {
                    VirtusizeTooltip.Position.TOP -> {
                        moveTo(arrowMiddlePoint.x, containerView.height.toFloat() + arrowHeight)
                        lineTo(arrowMiddlePoint.x - halfTriangleWidth, containerRectF.bottom)
                        lineTo(arrowMiddlePoint.x + halfTriangleWidth, containerRectF.bottom)
                    }
                    VirtusizeTooltip.Position.BOTTOM -> {
                        moveTo(arrowMiddlePoint.x, 0f)
                        lineTo(arrowMiddlePoint.x - halfTriangleWidth, containerRectF.top)
                        lineTo(arrowMiddlePoint.x + halfTriangleWidth, containerRectF.top)
                    }
                    VirtusizeTooltip.Position.LEFT -> {
                        moveTo(containerView.width.toFloat() + arrowHeight, arrowMiddlePoint.y)
                        lineTo(containerRectF.right, arrowMiddlePoint.y - halfTriangleWidth)
                        lineTo(containerRectF.right, arrowMiddlePoint.y + halfTriangleWidth)
                    }
                    VirtusizeTooltip.Position.RIGHT -> {
                        moveTo(0f, arrowMiddlePoint.y)
                        lineTo(containerRectF.left, arrowMiddlePoint.y - halfTriangleWidth)
                        lineTo(containerRectF.left, arrowMiddlePoint.y + halfTriangleWidth)
                    }
                }
            }

            close()
        }

        tooltipPaint.color = ContextCompat.getColor(context, R.color.vs_gray_900)

        canvas.drawPath(tooltipPath, tooltipPaint)
        super.onDraw(canvas)
    }
}