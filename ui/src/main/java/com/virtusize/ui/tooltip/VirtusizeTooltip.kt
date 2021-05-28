package com.virtusize.ui.tooltip

import android.content.Context
import android.graphics.Insets
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.virtusize.ui.utils.dp

class VirtusizeTooltip(private val context: Context, private val builder: Builder) {

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: FrameLayout? = null
    private var tooltipView: VirtusizeTooltipView? = null
    private val anchorViewToTooltipMargin = 5.dp
    private val windowEdgeToTooltipMargin = 16.dp
    private val windowWidth = getScreenWidth()

    private fun createWindowParams(token: IBinder): WindowManager.LayoutParams {
        val p = WindowManager.LayoutParams()
        p.gravity = Gravity.TOP or Gravity.START
        p.width = WindowManager.LayoutParams.MATCH_PARENT
        p.height = WindowManager.LayoutParams.MATCH_PARENT
        p.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        p.format = PixelFormat.TRANSLUCENT
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        p.token = token
        return p
    }

    fun show() {
        overlayView = FrameLayout(context)
        overlayView?.fitsSystemWindows = true
        overlayView?.setOnClickListener {
            hide()
        }

        val anchorViewLocation = IntArray(2)
        builder.anchorView.getLocationInWindow(anchorViewLocation)

        windowManager.addView(overlayView, createWindowParams(builder.anchorView.windowToken))

        tooltipView = VirtusizeTooltipView(context, builder)
        tooltipView!!.visibility = View.INVISIBLE
        tooltipView!!.containerView.post {
            var shouldShow = true
            var heightChanged = false

            // Basic Position - the center of the anchor view
            var tooltipViewXPosition = anchorViewLocation[0].toFloat() + builder.anchorView.width / 2
            var tooltipViewYPosition = anchorViewLocation[1].toFloat() + builder.anchorView.height / 2

            val anchorViewEndX = anchorViewLocation[0] + builder.anchorView.width
            val anchorViewEndSpace = windowWidth - anchorViewEndX
            val minimumTooltipSpace = windowEdgeToTooltipMargin / 2 + tooltipView!!.containerView.width / 2
            val minTooltipWidth = 48.dp + anchorViewToTooltipMargin + VirtusizeTooltipView.arrowHeight +  windowEdgeToTooltipMargin
            when (builder.position) {
                Position.TOP, Position.BOTTOM -> {
                    val maxTooltipWidth = windowWidth - windowEdgeToTooltipMargin * 2
                    val anchorViewCenterToRightEdgeWidth = windowWidth - tooltipViewXPosition - builder.anchorView.width / 2
                    when {
                        tooltipView!!.containerView.width > maxTooltipWidth -> {
                            tooltipView?.layoutParams?.width = (windowWidth - windowEdgeToTooltipMargin * 2).toInt()
                            tooltipView?.layoutParams = tooltipView?.layoutParams
                            tooltipViewXPosition = windowEdgeToTooltipMargin
                        }
                        tooltipViewXPosition < minimumTooltipSpace -> {
                            tooltipViewXPosition = windowEdgeToTooltipMargin / 2
                        }
                        builder.anchorView.width < tooltipView!!.containerView.width && anchorViewCenterToRightEdgeWidth < minimumTooltipSpace -> {
                            tooltipViewXPosition = windowWidth - tooltipView!!.containerView.width - windowEdgeToTooltipMargin / 2
                        }
                        else -> {
                            tooltipViewXPosition -= tooltipView!!.containerView.width / 2
                        }
                    }
                    if(builder.position == Position.TOP) {
                        tooltipViewYPosition -= (builder.anchorView.height / 2 + anchorViewToTooltipMargin + tooltipView!!.containerView.height + VirtusizeTooltipView.arrowHeight)
                    } else {
                        tooltipViewYPosition += (builder.anchorView.height / 2 + anchorViewToTooltipMargin)
                    }
                }
                Position.LEFT -> {
                    when {
                        anchorViewLocation[0] < minTooltipWidth -> {
                            shouldShow = false
                        }
                        tooltipView!!.containerView.width > getMaxLeftTooltipWidth(anchorViewLocation[0]) -> {
                            tooltipView!!.layoutParams?.width = getMaxLeftTooltipWidth(anchorViewLocation[0]).toInt()
                            tooltipView!!.layoutParams = tooltipView!!.layoutParams
                            tooltipViewXPosition = windowEdgeToTooltipMargin
                            heightChanged = true
                        }
                        else -> {
                            tooltipViewXPosition -= (builder.anchorView.width / 2 + tooltipView!!.containerView.width + VirtusizeTooltipView.arrowHeight + anchorViewToTooltipMargin)
                            tooltipViewYPosition -= tooltipView!!.containerView.height / 2
                        }
                    }
                }
                Position.RIGHT -> {
                    when {
                        anchorViewEndSpace < minTooltipWidth -> {
                            shouldShow = false
                        }
                        tooltipView!!.containerView.width > getMaxRightTooltipWidth(anchorViewEndSpace) -> {
                            tooltipView!!.layoutParams?.width = getMaxRightTooltipWidth(anchorViewEndSpace).toInt()
                            tooltipView!!.layoutParams = tooltipView!!.layoutParams
                            tooltipViewXPosition = anchorViewEndX + anchorViewToTooltipMargin
                            heightChanged = true
                        }
                        else -> {
                            tooltipViewXPosition += (builder.anchorView.width / 2 + anchorViewToTooltipMargin)
                            tooltipViewYPosition -= tooltipView!!.containerView.height / 2
                        }
                    }
                }
            }

            tooltipView?.x = tooltipViewXPosition
            tooltipView?.y = tooltipViewYPosition

            tooltipView!!.viewTreeObserver
                .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        tooltipView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        if (heightChanged) {
                            tooltipViewYPosition -= tooltipView!!.containerView.height / 2
                            tooltipView?.y = tooltipViewYPosition
                        }
                    }
                })

            if (shouldShow) {
                tooltipView?.visibility = View.VISIBLE
            } else {
                hide()
            }
        }

        overlayView?.addView(tooltipView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    private fun getMaxLeftTooltipWidth(anchorViewX: Int): Float = anchorViewX - windowEdgeToTooltipMargin - VirtusizeTooltipView.arrowHeight - anchorViewToTooltipMargin

    private fun getMaxRightTooltipWidth(anchorViewXEnd: Int): Float = anchorViewXEnd - windowEdgeToTooltipMargin - VirtusizeTooltipView.arrowHeight - anchorViewToTooltipMargin

    fun hide() {
        windowManager.removeView(overlayView)
        overlayView = null
    }

    enum class Position {
        TOP, BOTTOM, LEFT, RIGHT
    }

    class Builder(private val context: Context) {
        internal lateinit var anchorView: View
        internal var text: CharSequence? = null
        internal var position: Position = Position.BOTTOM
        internal var hideCloseButton = false
        internal var inverse = false
        internal var hideArrow = false
        internal var showOverlay = false
        internal var noBorder = false
        @LayoutRes
        internal var layoutId: Int? = null

        fun anchor(view: View): Builder {
            this.anchorView = view
            return this
        }

        fun text(text: CharSequence): Builder {
            this.text = text
            return this
        }

        fun position(pos: Position): Builder {
            this.position = pos
            return this
        }

        fun hideCloseButton(): Builder {
            this.hideCloseButton = true
            return this
        }

        fun inverseStyle(): Builder {
            this.inverse = true
            return this
        }

        fun hideArrow(): Builder {
            this.hideArrow = true
            return this
        }

        fun showOverlay(): Builder {
            this.showOverlay = true
            return this
        }

        fun noBorder(): Builder {
            this.noBorder = true
            return this
        }

        fun customView(@LayoutRes layoutId: Int): Builder {
            this.layoutId = layoutId
            return this
        }

        fun create(): VirtusizeTooltip {
            return VirtusizeTooltip(context, this)
        }
    }
}