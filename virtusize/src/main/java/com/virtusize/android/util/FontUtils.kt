package com.virtusize.android.util

import android.content.Context
import android.widget.TextView
import com.virtusize.android.data.local.VirtusizeLanguage

// The object that wraps Font utility functions
internal object FontUtils {
    /**
     * This enum contains all available font names used in this SDK
     */
    enum class FontName(
        val value: String,
    ) {
        ROBOTO("roboto"),
        NOTO_SANS_CJK_JP("noto_sans_cjk_jp"),
        NOTO_SANS_CJK_KR("noto_sans_cjk_kr"),
    }

    /**
     * This enum contains all available font weights used in this SDK
     */
    enum class FontType(
        val value: String,
    ) {
        REGULAR("_regular"),
        BOLD("_bold"),
    }

    /**
     * Sets up the TypeFace for a TextView by the display language
     */
    fun setTypeFace(
        context: Context,
        textView: TextView,
        language: VirtusizeLanguage?,
        fontType: FontType,
    ) {
        setTypeFaces(context, mutableListOf(textView), language, fontType)
    }

    /**
     * Sets up the TypeFaces for a list of TextView by the display language
     */
    fun setTypeFaces(
        context: Context,
        textViews: List<TextView>,
        language: VirtusizeLanguage?,
        fontType: FontType,
    ) {
        when (language) {
            VirtusizeLanguage.EN -> {
                setTypeFace(context, textViews, FontName.ROBOTO, fontType)
            }
            VirtusizeLanguage.JP -> {
                setTypeFace(context, textViews, FontName.NOTO_SANS_CJK_JP, fontType)
            }
            VirtusizeLanguage.KR -> {
                setTypeFace(context, textViews, FontName.NOTO_SANS_CJK_KR, fontType)
            }

            else -> {}
        }
    }

    /**
     * Sets up the TypeFaces for a list of TextView by the font name
     */
    private fun setTypeFace(
        context: Context,
        textViews: List<TextView>,
        fontName: FontName,
        fontType: FontType,
    ) {
        for (textView in textViews) {
            setTypeFace(context, textView, fontName, fontType)
        }
    }

    /**
     * Sets up the TypeFaces for a TextView by the font name
     */
    private fun setTypeFace(
        context: Context,
        textView: TextView,
        fontName: FontName,
        fontType: FontType,
    ) {
        textView.typeface = context.getTypefaceByName(fontName.value + fontType.value)
    }
}
