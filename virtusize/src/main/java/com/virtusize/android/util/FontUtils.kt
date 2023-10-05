package com.virtusize.android.util

import android.content.Context
import android.widget.TextView
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.ui.utils.Font

// The object that wraps Font utility functions
internal object FontUtils {

    /**
     * Sets up the TypeFace for a TextView by the display language
     */
    fun setTypeFace(
        context: Context,
        textView: TextView,
        language: VirtusizeLanguage?,
        fontType: Font.FontType
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
        fontType: Font.FontType
    ) {
        when (language) {
            VirtusizeLanguage.EN -> {
                Font.setTypeFace(context, textViews, Font.FontName.ROBOTO, fontType)
            }
            VirtusizeLanguage.JP -> {
                Font.setTypeFace(
                    context,
                    textViews,
                    Font.FontName.NOTO_SANS_CJK_JP,
                    fontType
                )
            }
            VirtusizeLanguage.KR -> {
                Font.setTypeFace(
                    context,
                    textViews,
                    Font.FontName.NOTO_SANS_CJK_KR,
                    fontType
                )
            }

            else -> {}
        }
    }
}
