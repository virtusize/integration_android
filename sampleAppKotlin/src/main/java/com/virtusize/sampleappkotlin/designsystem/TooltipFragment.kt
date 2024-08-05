package com.virtusize.sampleappkotlin.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.virtusize.android.ui.tooltip.VirtusizeTooltip
import com.virtusize.android.ui.utils.dp
import com.virtusize.sampleappkotlin.R
import com.virtusize.sampleappkotlin.databinding.FragmentTooltipBinding

class TooltipFragment : Fragment() {
    private lateinit var binding: FragmentTooltipBinding

    private lateinit var tooltip1: VirtusizeTooltip
    private lateinit var tooltip2: VirtusizeTooltip
    private lateinit var tooltip3: VirtusizeTooltip
    private lateinit var tooltip4: VirtusizeTooltip
    private lateinit var customTooltip: VirtusizeTooltip
    private lateinit var fittingRoomTooltip: VirtusizeTooltip

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTooltipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        tooltip1 =
            VirtusizeTooltip.Builder(requireContext())
                .anchor(binding.tooltip1Button)
                .position(VirtusizeTooltip.Position.BOTTOM)
                .text(R.string.vs_tooltip1_text)
                .size(width = 250.dp)
                .create()

        binding.tooltip1Button.setOnClickListener {
            tooltip1.show()
        }

        tooltip2 =
            VirtusizeTooltip.Builder(requireContext())
                .anchor(binding.tooltip2Button)
                .position(VirtusizeTooltip.Position.TOP)
                .text(R.string.vs_tooltip2_text)
                .inverseStyle()
                .size(width = 250.dp)
                .create()

        binding.tooltip2Button.setOnClickListener {
            tooltip2.show()
        }

        tooltip3 =
            VirtusizeTooltip.Builder(requireContext())
                .anchor(binding.tooltip3Button)
                .position(VirtusizeTooltip.Position.RIGHT)
                .text(R.string.vs_tooltip3_text)
                .hideArrow()
                .hideCloseButton()
                .create()

        binding.tooltip3Button.setOnClickListener {
            tooltip3.show()
        }

        tooltip4 =
            VirtusizeTooltip.Builder(requireContext())
                .anchor(binding.tooltip4Button)
                .position(VirtusizeTooltip.Position.BOTTOM)
                .text(
                    "You can also set \"showOverlay\" prop to show a dark overlay, and \"noBorder\" to remove the border around the carrot",
                )
                .inverseStyle()
                .showOverlay()
                .noBorder()
                .create()

        binding.tooltip4Button.setOnClickListener {
            tooltip4.show()
        }

        customTooltip =
            VirtusizeTooltip.Builder(requireContext())
                .anchor(binding.customTooltipButton)
                .position(VirtusizeTooltip.Position.BOTTOM)
                .customView(R.layout.custom_tooltip_view)
                .size(width = 250.dp)
                .create()

        val customView = customTooltip.getCustomView()
        val customTooltipLayout = customView.findViewById<LinearLayout>(R.id.customTooltipLayout)
        customTooltipLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_gray_900))
        val customTooltipTextView = customView.findViewById<TextView>(R.id.customTooltipTextView)
        customTooltipTextView.setTextColor(ContextCompat.getColor(requireContext(), com.virtusize.android.ui.R.color.vs_white))

        binding.customTooltipButton.setOnClickListener {
            customTooltip.show()
        }

        fittingRoomTooltip =
            VirtusizeTooltip.Builder(requireContext())
                .anchor(binding.fittingRoomEntryButton)
                .position(VirtusizeTooltip.Position.LEFT)
                .text(com.virtusize.android.ui.R.string.vs_similar_items)
                .hideCloseButton()
                .create()

        binding.fittingRoomEntryButton.setOnClickListener {
            fittingRoomTooltip.show()
        }
    }

    override fun onDestroy() {
        if (tooltip1.isShowing) {
            tooltip1.hide()
        }
        if (tooltip2.isShowing) {
            tooltip2.hide()
        }
        if (tooltip3.isShowing) {
            tooltip3.hide()
        }
        if (tooltip4.isShowing) {
            tooltip4.hide()
        }
        if (customTooltip.isShowing) {
            customTooltip.hide()
        }
        if (fittingRoomTooltip.isShowing) {
            fittingRoomTooltip.hide()
        }
        super.onDestroy()
    }
}
