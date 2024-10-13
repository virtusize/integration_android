package com.virtusize.android.data.local

internal typealias StoreId = Int

internal enum class StoreName(
    val value: String,
) {
    UNITED_ARROWS("united_arrows"),
}

internal class VirtusizeStoreRepository {
    private val storeMap: Map<StoreName, StoreId> =
        mapOf(
            StoreName.UNITED_ARROWS to 99,
        )

    fun getStoreId(storeName: StoreName): StoreId? = storeMap[storeName]
}
