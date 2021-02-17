package com.virtusize.libsource.ui

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtusize.libsource.VirtusizeRepository
import com.virtusize.libsource.data.remote.Product
import kotlinx.coroutines.launch

internal class VirtusizeInPageStandardViewModel(
    private val virtusizeRepository: VirtusizeRepository
): ViewModel() {

    val productImageBitmapLiveData = MutableLiveData<Pair<VirtusizeProductImageView, Bitmap>>()

    val productLiveData = MutableLiveData<Pair<VirtusizeProductImageView, Product>>()

    val finishLoadingProductImage = MutableLiveData<Boolean>()

    fun loadProductImages(productMap: MutableMap<VirtusizeProductImageView, Product>) {
        viewModelScope.launch {
            for (map in productMap.entries) {
                val productImageView = map.key
                val product = map.value
                val imageBitmap = virtusizeRepository.loadImage(product.getProductImageURL())
                if (imageBitmap != null) {
                    productImageBitmapLiveData.value = Pair(productImageView, imageBitmap)
                } else {
                    productLiveData.value = Pair(productImageView, product)
                }
            }
            finishLoadingProductImage.value = true
        }
    }
}