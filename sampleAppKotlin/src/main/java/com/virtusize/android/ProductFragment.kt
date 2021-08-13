package com.virtusize.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.virtusize.android.databinding.FragmentProductBinding
import com.virtusize.libsource.data.local.VirtusizeProduct

class ProductFragment : Fragment() {
    companion object {
        val externalProductIds = listOf(
            "vs_dress",
            "vs_top",
            "vs_shirt",
            "vs_coat",
            "vs_jacket",
            "vs_sweater",
            "vs_skirt",
            "vs_pants"
        )
    }

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val product =  VirtusizeProduct(
            externalId = externalProductIds.random(),
            imageUrl = "http://www.image.com/goods/12345.jpg"
        )

        (activity?.application as App).Virtusize.load(product)

        (activity?.application as App).Virtusize.setupVirtusizeView(
            virtusizeView = binding.exampleVirtusizeButton,
            product = product
        )

        (activity?.application as App).Virtusize
            .setupVirtusizeView(
                virtusizeView = binding.exampleVirtusizeInPageStandard,
                product = product
            )

        (activity?.application as App).Virtusize.setupVirtusizeView(
            virtusizeView = binding.exampleVirtusizeInPageMini,
            product = product
        )

        binding.nextProductButton.setOnClickListener {
            val action = ProductFragmentDirections.actionProductFragmentToSelf()
            findNavController().navigate(action)
        }
    }
}