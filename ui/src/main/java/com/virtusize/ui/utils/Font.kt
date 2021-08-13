package com.virtusize.ui.utils

import android.content.Context
import android.widget.TextView

// The object that wraps Font utility functions
object Font {

    /**
     * This enum contains all available font names used in this SDK
     */
    enum class FontName(val value: String){
        ROBOTO("roboto"),
        NOTO_SANS_CJK_JP("noto_sans_cjk_jp"),
        NOTO_SANS_CJK_KR("noto_sans_cjk_kr")
    }

    /**
     * This enum contains all available font weights used in this SDK
     */
    enum class FontType(val value: String) {
        REGULAR("_regular"),
        MEDIUM("medium"),
        BOLD("_bold")
    }

    /**
     * Sets up the TypeFaces for a list of TextView by the font name
     */
    fun setTypeFace(context: Context, textViews: List<TextView>, fontName: FontName, fontType: FontType) {
        for (textView in textViews) {
            setTypeFace(context, textView, fontName, fontType)
        }
    }

    /**
     * Sets up the TypeFaces for a TextView by the font name
     */
    private fun setTypeFace(context: Context, textView: TextView, fontName: FontName, fontType: FontType) {
        textView.typeface = context.getTypefaceByName(fontName.value + fontType.value)
    }
}