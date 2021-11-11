package com.virtusize.sampleappkotlin.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.virtusize.sampleappkotlin.databinding.FragmentAvatarBinding

class AvatarFragment: Fragment() {
    private lateinit var binding: FragmentAvatarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAvatarBinding.inflate(inflater, container, false)
        return binding.root
    }
}