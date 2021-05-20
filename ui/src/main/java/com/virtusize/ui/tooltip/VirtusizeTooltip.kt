package com.virtusize.ui.tooltip

import android.content.Context
import android.view.View

class VirtusizeTooltip(private val context: Context, builder: Builder) {

    enum class Position {
        TOP, BOTTOM, LEFT, RIGHT
    }

    class Builder(private val context: Context) {
        private var anchorView: View? = null
        private var position: Position = Position.BOTTOM
        private var hideCloseButton = false
        private var inverse = false
        private var hideArrow = false
        private var showOverlay = false
        private var noBorder = false

        fun anchor(view: View): Builder {
            this.anchorView = view
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

        fun create(): VirtusizeTooltip {
            return VirtusizeTooltip(context, this)
        }
    }
}