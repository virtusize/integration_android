package com.virtusize.libsource.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

/**
 * An abstract class representing the VirtusizeView that is an AppCompatButton
 */
abstract class VirtusizeButtonView(context: Context, attrs: AttributeSet): VirtusizeView, AppCompatButton(context, attrs)