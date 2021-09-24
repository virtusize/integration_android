package com.virtusize.android.ui

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtusize.android.VirtusizeRepository
import com.virtusize.android.data.remote.Product
import kotlinx.coroutines.launch

internal class VirtusizeInPageStandardViewModel(
    private val virtusizeRepository: VirtusizeRepository
) : ViewModel() {

    val productNetworkImageLiveData = MutableLiveData<Pair<VirtusizeProductImageView, Bitmap>>()

    val productPlaceholderImageLiveData =
        MutableLiveData<Pair<VirtusizeProductImageView, Product>>()

    val finishLoadingProductImages = MutableLiveData<Boolean>()

    /**
     * Loads the product images based on a map of keys [VirtusizeProductImageView] and their values [Product]
     */
    fun loadProductImages(productImageMap: MutableMap<VirtusizeProductImageView, Product>) {
        viewModelScope.launch {
            for (productImageMapEntry in productImageMap.entries) {
                val productImageView = productImageMapEntry.key
                val product = productImageMapEntry.value
                var imageBitmap: Bitmap? = null
                virtusizeRepository.loadImage(product.clientProductImageURL)?.let {
                    imageBitmap = it
                } ?: run {
                    virtusizeRepository.loadImage(product.getCloudinaryProductImageURL())?.let {
                        imageBitmap = it
                    }
                }

                if (imageBitmap != null) {
                    productNetworkImageLiveData.value = Pair(productImageView, imageBitmap!!)
                } else {
                    productPlaceholderImageLiveData.value = Pair(productImageView, product)
                }
            }
            finishLoadingProductImages.value = true
        }
    }
}
