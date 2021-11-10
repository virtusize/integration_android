package com.virtusize.android.data.local

/**
 * The class that wraps the parameters for the API request of sending the order
 *
 * @param externalOrderId the order ID provided by the client
 * @param items a list of the order items. See [VirtusizeOrderItem]
 */
data class VirtusizeOrder @JvmOverloads constructor(
    private val externalOrderId: String,
    var items: MutableList<VirtusizeOrderItem> = mutableListOf()
) {

    /**
     * A country code is set for each region i.e. ISO-3166.
     * This is set by the response of the request that retrieves the specific store info
     */
    private var region: String? = null

    /**
     * Returns the map that represents the parameters for the request that retrieves the specific store info
     * @param apiKey the API key that is unique to every Virtusize Client
     * @param externalUserId the unique user ID from the client system. It should be set during the initialization of the [Virtusize] class
     */
    fun paramsToMap(apiKey: String, externalUserId: String): Map<String, Any> {
        return emptyMap<String, Any>()
            .plus(
                mapOf(PARAM_API_KEY to apiKey)
            )
            .plus(
                mapOf(PARAM_EXTERNAL_ORDER_ID to externalOrderId)
            )
            .plus(
                mapOf(PARAM_EXTERNAL_USER_ID to externalUserId)
            )
            .plus(
                region?.let { mapOf(PARAM_REGION to it) }.orEmpty()
            )
            .plus(
                mapOf(PARAM_ITEMS to items.map { it.paramsToMap() })
            )
    }

    /**
     * Sets the string value of the region
     */
    fun setRegion(region: String?) {
        this.region = region
    }

    companion object {
        private const val PARAM_API_KEY = "apiKey"
        private const val PARAM_EXTERNAL_ORDER_ID = "externalOrderId"
        private const val PARAM_EXTERNAL_USER_ID = "externalUserId"
        private const val PARAM_REGION = "region"
        private const val PARAM_ITEMS = "items"

        /**
         * Parses a Map of the order data to a [VirtusizeOrder] object
         */
        fun parseMap(orderMap: Map<String, Any?>): VirtusizeOrder {
            return VirtusizeOrder(
                externalOrderId = orderMap[PARAM_EXTERNAL_ORDER_ID] as String,
                items = (orderMap[PARAM_ITEMS] as List<Map<String, Any?>>).map { orderItemMap ->
                    VirtusizeOrderItem.parseMap(
                        orderItemMap
                    )
                }.toMutableList()
            )
        }
    }
}
