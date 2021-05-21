package com.virtusize.ui.tooltip

import android.content.Context
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.annotation.LayoutRes

class VirtusizeTooltip(private val context: Context, private val builder: Builder) {

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentDisplayView: View? = null

    private fun createWindowParams(token: IBinder): WindowManager.LayoutParams {
        val p = WindowManager.LayoutParams()
        p.width = WindowManager.LayoutParams.MATCH_PARENT
        p.height = WindowManager.LayoutParams.MATCH_PARENT
        p.format = PixelFormat.TRANSLUCENT
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        p.token = token
        return p
    }

    fun show() {
        currentDisplayView = VirtusizeTooltipView(context, builder)
        currentDisplayView?.setOnClickListener {
            hide()
        }
        val params = createWindowParams(builder.anchorView!!.windowToken)
        windowManager.addView(currentDisplayView, params)
    }

    fun hide() {
        windowManager.removeView(currentDisplayView)
        currentDisplayView = null
    }

    enum class Position {
        TOP, BOTTOM, LEFT, RIGHT
    }

    class Builder(private val context: Context) {
        internal var anchorView: View? = null
        private var text: CharSequence? = null
        private var position: Position = Position.BOTTOM
        private var hideCloseButton = false
        private var inverse = false
        private var hideArrow = false
        private var showOverlay = false
        private var noBorder = false
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