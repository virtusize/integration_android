package com.virtusize.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.virtusize.libsource.data.local.VirtusizeProduct
import kotlinx.android.synthetic.main.activity_main.*

class ProductActivity : AppCompatActivity() {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        val product =  VirtusizeProduct(
            externalId = externalProductIds.random(),
            imageUrl = "http://www.image.com/goods/12345.jpg"
        )

        (application as App).Virtusize.load(product)

        (application as App).Virtusize.setupVirtusizeView(
            virtusizeView = exampleVirtusizeButton,
            product = product
        )

        (application as App).Virtusize
            .setupVirtusizeView(
                virtusizeView = exampleVirtusizeInPageStandard,
                product = product
            )

        (application as App).Virtusize.setupVirtusizeView(
            virtusizeView = exampleVirtusizeInPageMini,
            product = product
        )

        nextProductButton.setOnClickListener {
            startActivity(Intent(this, ProductActivity::class.java))
        }
    }
}