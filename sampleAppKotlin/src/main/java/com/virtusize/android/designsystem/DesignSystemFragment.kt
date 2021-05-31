package com.virtusize.android.designsystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.virtusize.android.R
import com.virtusize.android.databinding.FragmentDesignSystemBinding

class DesignSystemFragment : Fragment() {

    private var _binding: FragmentDesignSystemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDesignSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DesignSystemAdapter(requireContext(), arrayOf(requireContext().getString(R.string.virtusize_button)))
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}