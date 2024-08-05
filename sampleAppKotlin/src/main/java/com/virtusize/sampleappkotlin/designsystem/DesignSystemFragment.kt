package com.virtusize.sampleappkotlin.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.virtusize.sampleappkotlin.R
import com.virtusize.sampleappkotlin.databinding.FragmentDesignSystemBinding

class DesignSystemFragment : Fragment() {
    private lateinit var binding: FragmentDesignSystemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDesignSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        val adapter =
            DesignSystemAdapter(
                requireContext(),
                arrayOf(
                    requireContext().getString(R.string.virtusize_button),
                    requireContext().getString(R.string.virtusize_tooltip),
                ),
            )
        binding.recyclerView.adapter = adapter
    }
}
