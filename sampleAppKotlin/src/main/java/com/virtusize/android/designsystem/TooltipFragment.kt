package com.virtusize.android.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.virtusize.android.R
import com.virtusize.android.databinding.FragmentTooltipBinding
import com.virtusize.ui.tooltip.VirtusizeTooltip
import com.virtusize.ui.utils.dp

class TooltipFragment: Fragment() {
    private var _binding: FragmentTooltipBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTooltipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tooltip1 = VirtusizeTooltip.Builder(requireContext())
            .anchor(binding.tooltip1Button)
            .position(VirtusizeTooltip.Position.BOTTOM)
            .text(R.string.vs_tooltip1_text)
            .size(width = 250.dp)
            .create()

        binding.tooltip1Button.setOnClickListener {
            tooltip1.show()
        }

        val tooltip2 = VirtusizeTooltip.Builder(requireContext())
            .anchor(binding.tooltip2Button)
            .position(VirtusizeTooltip.Position.TOP)
            .text(R.string.vs_tooltip2_text)
            .inverseStyle()
            .size(width = 250.dp)
            .create()

        binding.tooltip2Button.setOnClickListener {
            tooltip2.show()
        }

        val tooltip3 = VirtusizeTooltip.Builder(requireContext())
            .anchor(binding.tooltip3Button)
            .position(VirtusizeTooltip.Position.RIGHT)
            .text(R.string.vs_tooltip3_text)
            .hideArrow()
            .hideCloseButton()
            .create()

        binding.tooltip3Button.setOnClickListener {
            tooltip3.show()
        }

        val tooltip4 = VirtusizeTooltip.Builder(requireContext())
            .anchor(binding.tooltip4Button)
            .position(VirtusizeTooltip.Position.BOTTOM)
            .text("You can also set \"showOverlay\" prop to show a dark overlay, and \"noBorder\" to remove the border around the carrot")
            .inverseStyle()
            .showOverlay()
            .noBorder()
            .create()

        binding.tooltip4Button.setOnClickListener {
            tooltip4.show()
        }

        val customTooltip = VirtusizeTooltip.Builder(requireContext())
            .anchor(binding.customTooltipButton)
            .position(VirtusizeTooltip.Position.BOTTOM)
            .customView(R.layout.custom_tooltip_view)
            .size(width = 250.dp)
            .create()

        val customView = customTooltip.getCustomView()
        val customTooltipLayout = customView.findViewById<LinearLayout>(R.id.customTooltipLayout)
        customTooltipLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.vs_gray_900))
        val customTooltipTextView = customView.findViewById<TextView>(R.id.customTooltipTextView)
        customTooltipTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.vs_white))

        binding.customTooltipButton.setOnClickListener {
            customTooltip.show()
        }

        val fittingRoomTooltip = VirtusizeTooltip.Builder(requireContext())
            .anchor(binding.fittingRoomEntryButton)
            .position(VirtusizeTooltip.Position.LEFT)
            .text(R.string.vs_similar_items)
            .hideCloseButton()
            .create()

        binding.fittingRoomEntryButton.setOnClickListener {
            fittingRoomTooltip.show()
        }
    }
}