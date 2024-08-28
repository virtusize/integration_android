package com.virtusize.sampleappkotlin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.sampleappkotlin.databinding.ActivityProductBinding

class ProductActivity : AppCompatActivity() {
    companion object {
        val externalProductIds =
            listOf(
                "vs_dress",
                "vs_top",
                "vs_shirt",
                "vs_coat",
                "vs_jacket",
                "vs_sweater",
                "vs_skirt",
                "vs_pants",
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_product)

        val product =
            VirtusizeProduct(
                externalId = externalProductIds.random(),
                imageUrl = "http://www.image.com/goods/12345.jpg",
            )

        Virtusize.getInstance().load(product)

        Virtusize.getInstance().setupVirtusizeView(
            virtusizeView = binding.exampleVirtusizeButton,
            product = product,
        )

        Virtusize.getInstance()
            .setupVirtusizeView(
                virtusizeView = binding.exampleVirtusizeInPageStandard,
                product = product,
            )

        Virtusize.getInstance().setupVirtusizeView(
            virtusizeView = binding.exampleVirtusizeInPageMini,
            product = product,
        )

        binding.nextProductButton.setOnClickListener {
            startActivity(Intent(this, ProductActivity::class.java))
        }
    }
}
