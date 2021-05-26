package com.virtusize.ui.tooltip

import android.content.Context
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.virtusize.ui.utils.dp

class VirtusizeTooltip(private val context: Context, private val builder: Builder) {

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: FrameLayout? = null
    private var tooltipView: VirtusizeTooltipView? = null

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
            val margin = 5.dp
            // Basic Position
            var tooltipViewXPosition = anchorViewLocation[0].toFloat() + builder.anchorView.width / 2
            var tooltipViewYPosition = anchorViewLocation[1].toFloat() + builder.anchorView.height / 2

            // Move based on the position setting
            when (builder.position) {
                Position.TOP -> {
                    tooltipViewXPosition -= tooltipView!!.containerView.width / 2
                    tooltipViewYPosition -= (builder.anchorView.height / 2 + tooltipView!!.containerView.height + VirtusizeTooltipView.arrowHeight + margin)
                }
                Position.BOTTOM -> {
                    tooltipViewXPosition -= tooltipView!!.containerView.width / 2
                    tooltipViewYPosition += (builder.anchorView.height / 2 + margin)
                }
                Position.LEFT -> {
                    tooltipViewXPosition -= (builder.anchorView.width / 2 + tooltipView!!.containerView.width + VirtusizeTooltipView.arrowHeight + margin)
                    tooltipViewYPosition -= tooltipView!!.containerView.height / 2
                }
                Position.RIGHT -> {
                    tooltipViewXPosition += (builder.anchorView.width / 2 + margin)
                    tooltipViewYPosition -= tooltipView!!.containerView.height / 2
                }
            }

            tooltipView?.x = tooltipViewXPosition
            tooltipView?.y = tooltipViewYPosition

            tooltipView?.visibility = View.VISIBLE
        }

        overlayView?.addView(tooltipView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

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