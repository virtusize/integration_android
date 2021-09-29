package com.virtusize.android.ui.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.virtusize.android.ui.R
import com.virtusize.android.ui.databinding.VirtusizeAvatarBinding

class VirtusizeAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var virtusizeAvatarSize = VirtusizeAvatarSize.SMALL
        set(value) {
            field = value
            setAvatarStyle()
        }

    private val binding = VirtusizeAvatarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeAvatar, 0, 0)

        val avatarSize = attrsArray.getInt(
            R.styleable.VirtusizeAvatar_avatarSize,
            VirtusizeAvatarSize.SMALL.ordinal
        )
        VirtusizeAvatarSize.values().firstOrNull { it.ordinal == avatarSize }
            ?.let { avatarCircleSize ->
                virtusizeAvatarSize = avatarCircleSize
            }

        val avatarImageResId =
            attrsArray.getResourceId(R.styleable.AppCompatImageView_android_src, -1)
        setAvatarImage(avatarImageResId)

        attrsArray.recycle()
    }

    fun setAvatarImage(@DrawableRes resId: Int) {
        if (resId != -1) {
            binding.avatarImageView.setImageResource(resId)
        }
    }

    fun setAvatarImage(drawable: Drawable?) {
        binding.avatarImageView.setImageDrawable(drawable)
    }

    fun setAvatarImage(imageBitmap: Bitmap) {
        binding.avatarImageView.setImageBitmap(imageBitmap)
    }

    private fun setAvatarStyle() {
        val avatarCircleSize = virtusizeAvatarSize.getAvatarCircleSize(context).toInt()
        binding.avatarBorderCardView.layoutParams = LayoutParams(avatarCircleSize, avatarCircleSize)
        binding.avatarBorderCardView.radius = virtusizeAvatarSize.getAvatarCircleRadiusSize(context)

        val avatarImageSize = virtusizeAvatarSize.getAvatarImageSize(context).toInt()
        val avatarImageLayoutParams = LayoutParams(avatarImageSize, avatarImageSize)
        avatarImageLayoutParams.gravity = Gravity.CENTER
        binding.avatarImageCardView.layoutParams = avatarImageLayoutParams
        binding.avatarImageCardView.radius = virtusizeAvatarSize.getAvatarImageRadiusSize(context)
    }
}
