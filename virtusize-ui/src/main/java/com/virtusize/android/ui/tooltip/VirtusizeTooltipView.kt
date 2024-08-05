package com.virtusize.android.ui.tooltip

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.virtusize.android.ui.R
import com.virtusize.android.ui.databinding.VirtusizeTooltipBinding
import com.virtusize.android.ui.utils.dp

class VirtusizeTooltipView
    @JvmOverloads
    constructor(
        context: Context,
        private val builder: VirtusizeTooltip.Builder,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        companion object {
            internal val arrowHeight = 7.dp
            private val arrowWidth = 13.dp
            private val halfTriangleWidth = arrowWidth / 2
            private val cornerRadius = 6.dp
            private var borderWidth = 1.dp
        }

        private var binding: VirtusizeTooltipBinding? = null
        internal var containerView: View

        private var borderPaint: Paint? = null
        private val tooltipPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var tooltipPath: Path = Path()

        init {
            // Set it to false to let onDraw to be called
            setWillNotDraw(false)
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            // Use a custom layout
            if (builder.layoutId != null) {
                containerView = inflater.inflate(builder.layoutId!!, this, true)
                // Use the default layout
            } else {
                binding = VirtusizeTooltipBinding.inflate(inflater, this, true)
                containerView = binding!!.root
                binding!!.tooltipTextView.text = builder.text ?: resources.getString(R.string.vs_similar_items)
                val tooltipTextViewDefaultPadding =
                    resources.getDimension(R.dimen.vs_tooltip_text_view_default_padding).toInt()
                val tooltipTextViewLeftPadding = resources.getDimension(R.dimen.vs_tooltip_text_view_start_padding).toInt()
                val tooltipTextViewRightPadding = resources.getDimension(R.dimen.vs_tooltip_text_view_end_padding).toInt()
                if (builder.hideCloseButton) {
                    binding!!.closeImageView.visibility = GONE
                    binding!!.tooltipTextView.setPadding(
                        tooltipTextViewDefaultPadding,
                        tooltipTextViewDefaultPadding,
                        tooltipTextViewDefaultPadding,
                        tooltipTextViewDefaultPadding,
                    )
                } else {
                    binding!!.tooltipTextView.setPadding(
                        tooltipTextViewLeftPadding,
                        tooltipTextViewDefaultPadding,
                        tooltipTextViewRightPadding,
                        tooltipTextViewDefaultPadding,
                    )
                }
                if (builder.inverse) {
                    binding!!.tooltipTextView.setTextColor(ContextCompat.getColor(context, R.color.vs_gray_900))
                    binding!!.closeImageView.setColorFilter(ContextCompat.getColor(context, R.color.vs_gray_900))
                } else {
                    binding!!.tooltipTextView.setTextColor(ContextCompat.getColor(context, R.color.vs_white))
                    binding!!.closeImageView.setColorFilter(ContextCompat.getColor(context, R.color.vs_white))
                }
            }

            builder.width?.let { width ->
                layoutParams.width = width.toInt()
            }
            builder.height?.let { height ->
                layoutParams.height = height.toInt()
            }
            setLayoutParams(layoutParams)

            if (!builder.noBorder) {
                borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                if (builder.inverse) {
                    borderPaint!!.color = ContextCompat.getColor(context, R.color.vs_gray_900)
                } else {
                    borderPaint!!.color = ContextCompat.getColor(context, R.color.vs_white)
                }
                borderPaint!!.style = Paint.Style.STROKE
                borderPaint!!.strokeWidth = borderWidth
                borderPaint!!.isAntiAlias = true
            } else {
                borderPaint = null
            }

            // Adjust the padding because the drawing takes up the original space of containerView
            setPadding(
                if (builder.position == VirtusizeTooltip.Position.RIGHT) arrowHeight.toInt() else 0,
                if (builder.position == VirtusizeTooltip.Position.BOTTOM) arrowHeight.toInt() else 0,
                if (builder.position == VirtusizeTooltip.Position.LEFT) arrowHeight.toInt() else 0,
                if (builder.position == VirtusizeTooltip.Position.TOP) arrowHeight.toInt() else 0,
            )
        }

        override fun onDraw(canvas: Canvas) {
            val containerRectF = RectF(0F, 0F, containerView.width.toFloat(), containerView.height.toFloat())
            if (!builder.noBorder) {
                containerRectF.left += borderWidth
                containerRectF.top += borderWidth
                containerRectF.right -= borderWidth
                containerRectF.bottom -= borderWidth
            }
            val arrowMiddlePoint = getArrowMidPoint(this@VirtusizeTooltipView, containerRectF)

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

                // Draw the triangle (arrow)
                if (!builder.hideArrow) {
                    when (builder.position) {
                        VirtusizeTooltip.Position.TOP -> {
                            drawTriangleForVerticalPosition(
                                path = this,
                                moveXTo = arrowMiddlePoint.x,
                                moveYTo = containerView.height.toFloat() + arrowHeight,
                                containerEdge = containerRectF.bottom,
                            )
                        }
                        VirtusizeTooltip.Position.BOTTOM -> {
                            drawTriangleForVerticalPosition(
                                path = this,
                                moveXTo = arrowMiddlePoint.x,
                                moveYTo = 0f,
                                containerEdge = containerRectF.top,
                            )
                        }
                        VirtusizeTooltip.Position.LEFT -> {
                            drawTriangleForHorizontalPosition(
                                path = this,
                                moveXTo = containerView.width.toFloat() + arrowHeight,
                                moveYTo = arrowMiddlePoint.y + (if (!builder.noBorder) borderWidth else 0f),
                                containerEdge = containerRectF.right,
                            )
                        }
                        VirtusizeTooltip.Position.RIGHT -> {
                            drawTriangleForHorizontalPosition(
                                path = this,
                                moveXTo = 0f,
                                moveYTo = arrowMiddlePoint.y + (if (!builder.noBorder) borderWidth else 0f),
                                containerEdge = containerRectF.left,
                            )
                        }
                    }
                }

                close()
            }

            if (builder.inverse || builder.layoutId != null) {
                tooltipPaint.color = ContextCompat.getColor(context, R.color.vs_white)
            } else {
                tooltipPaint.color = ContextCompat.getColor(context, R.color.vs_gray_900)
            }

            canvas.drawPath(tooltipPath, tooltipPaint)

            if (!builder.noBorder) {
                canvas.drawPath(tooltipPath, borderPaint!!)

                if (!builder.hideArrow) {
                    // Draw another triangle with 1dp offset to remove the line between the rectangle and the triangle
                    with(tooltipPath) {
                        reset()
                        tooltipPath.reset()
                        when (builder.position) {
                            VirtusizeTooltip.Position.TOP -> {
                                drawTriangleForVerticalPosition(
                                    path = this,
                                    moveXTo = arrowMiddlePoint.x,
                                    moveYTo = containerView.height.toFloat() + arrowHeight,
                                    containerEdge = containerRectF.bottom,
                                    yOffset = -borderWidth,
                                )
                            }
                            VirtusizeTooltip.Position.BOTTOM -> {
                                drawTriangleForVerticalPosition(
                                    path = this,
                                    moveXTo = arrowMiddlePoint.x,
                                    moveYTo = 0f,
                                    containerEdge = containerRectF.top,
                                    yOffset = borderWidth,
                                )
                            }
                            VirtusizeTooltip.Position.LEFT -> {
                                drawTriangleForHorizontalPosition(
                                    path = this,
                                    moveXTo = containerView.width.toFloat() + arrowHeight,
                                    moveYTo = arrowMiddlePoint.y + (if (!builder.noBorder) borderWidth else 0f),
                                    containerEdge = containerRectF.right,
                                    xOffset = -borderWidth,
                                )
                            }
                            VirtusizeTooltip.Position.RIGHT -> {
                                drawTriangleForHorizontalPosition(
                                    path = this,
                                    moveXTo = 0f,
                                    moveYTo = arrowMiddlePoint.y + (if (!builder.noBorder) borderWidth else 0f),
                                    containerEdge = containerRectF.left,
                                    xOffset = borderWidth,
                                )
                            }
                        }

                        if (builder.layoutId == null) {
                            if (builder.inverse) {
                                tooltipPaint.color = ContextCompat.getColor(context, R.color.vs_white)
                            } else {
                                tooltipPaint.color =
                                    ContextCompat.getColor(context, R.color.vs_gray_900)
                            }
                        }

                        canvas.drawPath(this, tooltipPaint)
                    }
                }
            }

            super.onDraw(canvas)
        }

        private fun drawTriangleForVerticalPosition(
            path: Path,
            moveXTo: Float,
            moveYTo: Float,
            containerEdge: Float,
            yOffset: Float = 0f,
        ) {
            path.moveTo(moveXTo, moveYTo + yOffset)
            path.lineTo(moveXTo - halfTriangleWidth, containerEdge + yOffset)
            path.lineTo(moveXTo + halfTriangleWidth, containerEdge + yOffset)
        }

        private fun drawTriangleForHorizontalPosition(
            path: Path,
            moveXTo: Float,
            moveYTo: Float,
            containerEdge: Float,
            xOffset: Float = 0f,
        ) {
            path.moveTo(moveXTo + xOffset, moveYTo)
            path.lineTo(containerEdge + xOffset, moveYTo - halfTriangleWidth)
            path.lineTo(containerEdge + xOffset, moveYTo + halfTriangleWidth)
        }

        private fun getArrowMidPoint(
            tooltipView: VirtusizeTooltipView,
            rectF: RectF,
        ): PointF {
            val anchoredView = builder.anchorView
            var middleX = rectF.width() / 2
            val middleY = rectF.height() / 2
            if (middleX != anchoredView.x + anchoredView.width / 2) {
                middleX += (
                    anchoredView.x +
                        (anchoredView.width / 2) -
                        (tooltipView.x) -
                        (rectF.width() / 2)
                )
            }
            return PointF(middleX, middleY)
        }
    }
