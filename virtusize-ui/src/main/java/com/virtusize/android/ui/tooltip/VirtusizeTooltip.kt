package com.virtusize.android.ui.tooltip

import android.content.Context
import android.graphics.Insets
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.virtusize.android.ui.R
import com.virtusize.android.ui.utils.dp

class VirtusizeTooltip(private val context: Context, private val builder: Builder) {

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: FrameLayout? = null
    private var tooltipView: VirtusizeTooltipView? = null
    private val anchorViewToTooltipMargin = 5.dp
    private val windowEdgeToTooltipMargin = 16.dp
    private val windowWidth = getScreenWidth()

    val isShowing: Boolean
        get() = overlayView != null

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

        if (builder.showOverlay) {
            overlayView?.setBackgroundColor(ContextCompat.getColor(context, R.color.vs_black_overlay))
        }

        val anchorViewLocation = IntArray(2)
        builder.anchorView.getLocationInWindow(anchorViewLocation)

        windowManager.addView(overlayView, createWindowParams(builder.anchorView.windowToken))

        if (tooltipView == null || builder.layoutId == null) {
            tooltipView = VirtusizeTooltipView(context, builder)
        }

        tooltipView!!.visibility = View.INVISIBLE

        tooltipView!!.containerView.post {
            var shouldShow = true

            var tooltipContainerWidth = tooltipView!!.containerView.width
            builder.width?.let { width ->
                tooltipContainerWidth = width.toInt()
            }

            // Basic Position - the center of the anchor view
            var tooltipViewXPosition = anchorViewLocation[0].toFloat() + builder.anchorView.width / 2
            var tooltipViewYPosition = anchorViewLocation[1].toFloat() - builder.anchorView.height / 2

            val anchorViewEndX = anchorViewLocation[0] + builder.anchorView.width
            val anchorViewEndSpace = windowWidth - anchorViewEndX
            val minimumTooltipSpace = windowEdgeToTooltipMargin / 2 + tooltipContainerWidth / 2
            val minTooltipWidth = 48.dp + anchorViewToTooltipMargin + VirtusizeTooltipView.arrowHeight + windowEdgeToTooltipMargin

            when (builder.position) {
                Position.TOP, Position.BOTTOM -> {
                    val maxTooltipWidth = windowWidth - windowEdgeToTooltipMargin * 2
                    val anchorViewCenterToRightEdgeWidth = windowWidth - tooltipViewXPosition - builder.anchorView.width / 2
                    when {
                        tooltipContainerWidth > maxTooltipWidth -> {
                            tooltipContainerWidth = (windowWidth - windowEdgeToTooltipMargin * 2).toInt()
                            tooltipViewXPosition = windowEdgeToTooltipMargin
                        }
                        tooltipViewXPosition < minimumTooltipSpace -> {
                            tooltipViewXPosition = windowEdgeToTooltipMargin / 2
                        }
                        builder.anchorView.width < tooltipContainerWidth && anchorViewCenterToRightEdgeWidth < minimumTooltipSpace -> {
                            tooltipViewXPosition = windowWidth - tooltipContainerWidth - windowEdgeToTooltipMargin / 2
                        }
                        else -> {
                            tooltipViewXPosition -= tooltipContainerWidth / 2
                        }
                    }
                    if (builder.position == Position.TOP) {
                        tooltipViewYPosition -= (builder.anchorView.height + anchorViewToTooltipMargin + VirtusizeTooltipView.arrowHeight)
                    } else {
                        tooltipViewYPosition += anchorViewToTooltipMargin + VirtusizeTooltipView.arrowHeight
                    }
                }
                Position.LEFT -> {
                    when {
                        anchorViewLocation[0] < minTooltipWidth -> {
                            shouldShow = false
                        }
                        tooltipContainerWidth > getMaxLeftTooltipWidth(anchorViewLocation[0]) -> {
                            tooltipContainerWidth = getMaxLeftTooltipWidth(anchorViewLocation[0]).toInt()
                            tooltipViewXPosition = windowEdgeToTooltipMargin
                        }
                        else -> {
                            tooltipViewXPosition -= (builder.anchorView.width / 2 + tooltipContainerWidth + VirtusizeTooltipView.arrowHeight + anchorViewToTooltipMargin)
                        }
                    }
                }
                Position.RIGHT -> {
                    when {
                        anchorViewEndSpace < minTooltipWidth -> {
                            shouldShow = false
                        }
                        tooltipContainerWidth > getMaxRightTooltipWidth(anchorViewEndSpace) -> {
                            tooltipContainerWidth = getMaxRightTooltipWidth(anchorViewEndSpace).toInt()
                            tooltipViewXPosition = anchorViewEndX + anchorViewToTooltipMargin
                        }
                        else -> {
                            tooltipViewXPosition += (builder.anchorView.width / 2 + anchorViewToTooltipMargin)
                        }
                    }
                }
            }

            if (tooltipView!!.containerView.layoutParams == null) {
                tooltipView!!.containerView.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            }
            tooltipView!!.containerView.layoutParams.width = tooltipContainerWidth
            builder.height?.let { height ->
                tooltipView!!.containerView.layoutParams.height = height.toInt()
            }
            tooltipView!!.containerView.layoutParams = tooltipView!!.containerView.layoutParams

            tooltipView!!.viewTreeObserver
                .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        tooltipView!!.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        if (builder.position == Position.LEFT || builder.position == Position.RIGHT) {
                            tooltipViewYPosition -= builder.anchorView.height
                        } else if (builder.position == Position.TOP) {
                            tooltipViewYPosition -= tooltipView!!.containerView.height
                        }
                        tooltipView!!.x = tooltipViewXPosition
                        tooltipView!!.y = tooltipViewYPosition
                    }
                })

            if (shouldShow) {
                tooltipView!!.visibility = View.VISIBLE
            } else {
                hide()
            }
        }

        overlayView?.addView(tooltipView, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    fun hide() {
        if (overlayView != null) {
            overlayView?.removeView(tooltipView)
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }

    fun getCustomView(): View {
        if (builder.layoutId == null) {
            throw IllegalStateException("Please assign the layout ID for your custom view.")
        }
        if (tooltipView == null) {
            tooltipView = VirtusizeTooltipView(context, builder)
        }
        return tooltipView!!.containerView
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

    enum class Position {
        TOP, BOTTOM, LEFT, RIGHT
    }

    class Builder(private val context: Context) {
        internal lateinit var anchorView: View
        internal var text: CharSequence? = null
        internal var position: Position = Position.BOTTOM
        internal var width: Float? = null
        internal var height: Float? = null
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

        fun text(@StringRes id: Int): Builder {
            this.text = context.resources.getString(id)
            return this
        }

        fun position(pos: Position): Builder {
            this.position = pos
            return this
        }

        fun size(width: Float? = null, height: Float? = null): Builder {
            this.width = width
            this.height = height
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
