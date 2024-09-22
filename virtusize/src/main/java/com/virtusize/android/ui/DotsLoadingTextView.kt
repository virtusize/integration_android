package com.virtusize.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * A custom TextView to display animated dots in a text
 */
internal class DotsLoadingTextView : AppCompatTextView {
    companion object {
        private const val MAX_DOTS = 3
    }

    private val dotsLoadingHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private var dotsLoadingRunnable: Runnable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    )

    /**
     * Starts the animation
     */
    fun startAnimation() {
        if (dotsLoadingRunnable != null) return
        val originalText = text
        var tempDots = 0
        dotsLoadingRunnable =
            object : Runnable {
                @SuppressLint("SetTextI18n")
                override fun run() {
                    dotsLoadingHandler.postDelayed(this, 500)
                    if (tempDots == MAX_DOTS) {
                        tempDots = 0
                        text = originalText
                    } else {
                        text = "${originalText}${getDot(++tempDots)}"
                    }
                    invalidate()
                }
            }
        dotsLoadingRunnable?.run()
    }

    /**
     * Stops the animation
     */
    fun stopAnimation() {
        dotsLoadingRunnable?.let { runnable ->
            dotsLoadingHandler.removeCallbacks(runnable)
            dotsLoadingRunnable = null
        }
    }

    /**
     * Gets the number of dots
     */
    private fun getDot(dotNumber: Int): String {
        val sb = StringBuilder()
        for (i in 1..dotNumber) {
            sb.append("·")
        }
        return sb.toString()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
