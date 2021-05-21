package com.virtusize.ui.tooltip

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.virtusize.ui.R

class VirtusizeTooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :  LinearLayout(context, attrs, defStyleAttr) {

    private var builder: VirtusizeTooltip.Builder? = null

    constructor(context: Context, builder: VirtusizeTooltip.Builder) : this(context) {
        this.builder = builder
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(builder?.layoutId ?: R.layout.virtusize_tooltip, this, true)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        // TODO: draw arrow
    }
}