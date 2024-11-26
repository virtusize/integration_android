package com.virtusize.android.data.local

import java.lang.UnsupportedOperationException

@JvmInline
value class StoreId(val value: Int)

enum class StoreName(val value: String) {
    UNITED_ARROWS("united_arrows"),
}

object VirtusizeStoreRepository {
    private val storeMap: Map<StoreName, StoreId> =
        mapOf(
            StoreName.UNITED_ARROWS to StoreId(99),
        )

    fun getStoreId(storeName: StoreName): StoreId = storeMap[storeName] ?: throw UnsupportedOperationException("Store name not found")
}

val StoreId?.isUnitedArrows: Boolean
    get() = this?.value == VirtusizeStoreRepository.getStoreId(StoreName.UNITED_ARROWS).value
