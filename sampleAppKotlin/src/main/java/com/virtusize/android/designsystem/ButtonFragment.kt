package com.virtusize.android.designsystem

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.virtusize.android.R
import com.virtusize.android.databinding.FragmentButtonBinding
import com.virtusize.ui.button.VirtusizeButtonSize
import com.virtusize.ui.button.VirtusizeButtonStyle
import com.virtusize.ui.button.VirtusizeButtonTextSize

class ButtonFragment: Fragment() {

    private var _binding: FragmentButtonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentButtonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.defaultButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT

        binding.flatButton.virtusizeButtonStyle = VirtusizeButtonStyle.FLAT

        binding.invertedButton.virtusizeButtonStyle = VirtusizeButtonStyle.INVERTED

        binding.disabledButton.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.disabledButton.isEnabled = false

        binding.color1Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color1Button.setTextColor(ContextCompat.getColor(requireContext(), R.color.vs_white))
        binding.color1Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), R.color.vs_teal))

        binding.color1Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color1Button.setTextColor(ContextCompat.getColor(requireContext(), R.color.vs_white))
        binding.color1Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), R.color.vs_teal))

        binding.color2Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color2Button.setTextColor(ContextCompat.getColor(requireContext(), R.color.vs_white))
        binding.color2Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ocean_blue))

        binding.color3Button.virtusizeButtonStyle = VirtusizeButtonStyle.DEFAULT
        binding.color3Button.setVirtusizeBackgroundColor(ContextCompat.getColor(requireContext(), R.color.vs_red))

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
    }
}