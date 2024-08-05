package com.virtusize.sampleappkotlin.designsystem

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.virtusize.android.ui.button.VirtusizeButtonSize
import com.virtusize.android.ui.button.VirtusizeButtonStyle
import com.virtusize.android.ui.button.VirtusizeButtonTextSize
import com.virtusize.android.ui.button.VirtusizeRoundImageButtonStyle
import com.virtusize.sampleappkotlin.R
import com.virtusize.sampleappkotlin.databinding.FragmentButtonBinding

class ButtonFragment : Fragment() {
    private lateinit var binding: FragmentButtonBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentButtonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.defaultButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT

        binding.flatButton.virtusizeButtonStyle = VirtusizeButtonStyle.FLAT

        binding.invertedButton.virtusizeButtonStyle = VirtusizeButtonStyle.INVERTED

        binding.disabledButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.disabledButton.isEnabled = false

        binding.color1Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color1Button.setTextColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_white))
        binding.color1Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_teal))

        binding.color1Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color1Button.setTextColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_white))
        binding.color1Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_teal))

        binding.color2Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color2Button.setTextColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_white))
        binding.color2Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ocean_blue))

        binding.color3Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color3Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_red))

        binding.color4Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color4Button.setTextColor(Color.parseColor("#07689F"))
        binding.color4Button.setVirtusizeBackgroundColor(Color.parseColor("#FFC93C"))

        binding.defaultSizeButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.defaultSizeButton.virtusizeButtonSize = VirtusizeButtonSize.STANDARD

        binding.smallSizeButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.smallSizeButton.virtusizeButtonSize = VirtusizeButtonSize.SMALL

        binding.defaultTextSizeButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.defaultTextSizeButton.virtusizeButtonTextSize = VirtusizeButtonTextSize.DEFAULT

        binding.smallerTextSizeButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.smallerTextSizeButton.virtusizeButtonTextSize = VirtusizeButtonTextSize.SMALLER

        binding.normalTextSizeButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.normalTextSizeButton.virtusizeButtonTextSize = VirtusizeButtonTextSize.NORMAL

        binding.largeTextSizeButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.largeTextSizeButton.virtusizeButtonTextSize = VirtusizeButtonTextSize.LARGE

        binding.largerTextSizeButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.largerTextSizeButton.virtusizeButtonTextSize = VirtusizeButtonTextSize.LARGER

        binding.icon1Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.icon1Button.setText(R.string.like)
        binding.icon1Button.virtusizeButtonTextSize = VirtusizeButtonTextSize.DEFAULT
        val icon1ButtonDrawable = ContextCompat.getDrawable(requireContext(), com.virtusize.android.ui.R.drawable.ic_heart_solid)
        binding.icon1Button.setLeftIcon(icon1ButtonDrawable)

        binding.icon2Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.icon2Button.setText(R.string.get_started)
        binding.icon2Button.virtusizeButtonTextSize = VirtusizeButtonTextSize.DEFAULT
        val icon2ButtonDrawable = ContextCompat.getDrawable(requireContext(), com.virtusize.android.ui.R.drawable.ic_angle_right)
        binding.icon2Button.setRightIcon(icon2ButtonDrawable)

        binding.icon3Button.virtusizeButtonStyle = VirtusizeButtonStyle.INVERTED
        binding.icon3Button.setText(R.string.redirect_fb)
        binding.icon3Button.virtusizeButtonSize = VirtusizeButtonSize.SMALL
        binding.icon3Button.virtusizeButtonTextSize = VirtusizeButtonTextSize.DEFAULT
        val icon3ButtonRightDrawable = ContextCompat.getDrawable(requireContext(), com.virtusize.android.ui.R.drawable.ic_angle_right)
        binding.icon3Button.setRightIcon(icon3ButtonRightDrawable)
        val icon3ButtonLeftDrawable = ContextCompat.getDrawable(requireContext(), com.virtusize.android.ui.R.drawable.ic_fb)
        binding.icon3Button.setLeftIcon(icon3ButtonLeftDrawable)

        binding.round1Button.roundImageButtonStyle = VirtusizeRoundImageButtonStyle.COLOR
        binding.round1Button.setImageResource(com.virtusize.android.ui.R.drawable.ic_heart_solid)
        binding.round1Button.setColorFilter(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_red))

        binding.round2Button.roundImageButtonStyle = VirtusizeRoundImageButtonStyle.COLOR
        binding.round2Button.virtusizeButtonSize = VirtusizeButtonSize.SMALL
        binding.round2Button.setImageResource(com.virtusize.android.ui.R.drawable.ic_help)
        binding.round2Button.setColorFilter(ContextCompat.getColor(requireContext(), R.color.ocean_blue))

        binding.round3Button.virtusizeButtonStyle = VirtusizeButtonStyle.ROUND

        binding.round4Button.roundImageButtonStyle = VirtusizeRoundImageButtonStyle.INVERTED
        binding.round4Button.setImageResource(com.virtusize.android.ui.R.drawable.ic_lock)
    }
}
