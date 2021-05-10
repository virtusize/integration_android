package com.virtusize.android.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.virtusize.android.databinding.FragmentButtonBinding
import com.virtusize.ui.button.VirtusizeButtonStyle

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
    }
}