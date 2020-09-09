package com.virtusize.libsource.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

internal class DotsLoadingTextView : AppCompatTextView {

    private var originalText: CharSequence? = null
    private var runnable: Runnable? = null
    private var dotsLoadingHandler: Handler? = null
    private var maxDots: Int = 3
    private var tempDots: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun startAnimation() {
        originalText = text
        dotsLoadingHandler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                dotsLoadingHandler?.postDelayed(this, 500)
                if (tempDots == maxDots) {
                    tempDots = 0
                    text = originalText
                } else {
                    text = "${originalText}${getDot(++tempDots)}"
                }
                invalidate()
            }
        }
        runnable!!.run()
    }

    private fun stopAnimation() {
        runnable?.let { dotsLoadingHandler?.removeCallbacks(it) }
    }

    private fun getDot(dotNumber: Int): String {
        val sb = StringBuilder()
        for (i in 1..dotNumber) {
            sb.append(".")
        }
        return sb.toString()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}