package com.virtusize.libsource.data.local

/**
 * The class that wraps the parameters of the order item for the API request of sending the order
 * @param productId the provide ID provided by the client. It must be unique for a product.
 * @param size the name of the size, e.g. "S", "M", etc.
 * @param sizeAlias the alias of the size is added if the size name is not identical from the product page
 * @param variantId the variant ID that is set on the product SKU, color, or size if there are several options
 * @param imageUrl the image URL of the item
 * @param color the color of the item, e.g. "Red", etc.
 * @param gender an identifier for the gender, e.g. "W", "Women", etc.
 * @param unitPrice the product price that is a double number with a maximum of 12 digits and 2 decimals (12, 2)
 * @param currency the currency code, e.g. "JPY", etc.
 * @param quantity the number of the item purchased. If it's not passed, It will be set to 1
 * @param url the URL of the product page. Please make sure this is a URL that users can access.
 *
 */
data class VirtusizeOrderItem @JvmOverloads constructor(
    private val productId: String,
    private val size: String,
    private val sizeAlias: String? = null,
    private val variantId: String? = null,
    private val imageUrl: String,
    private val color: String? = null,
    private val gender: String? = null,
    private val unitPrice: Double,
    private val currency: String,
    private val quantity: Int = 1,
    private val url: String? = null
) {

    /**
     * Returns the map that represents the parameters for the order request
     */
    fun paramsToMap(): Map<String, Any> {
        return emptyMap<String, Any>()
            .plus(
                mapOf(PARAM_PRODUCT_ID to productId)
            )
            .plus(
                mapOf(PARAM_SIZE to size)
            )
            .plus(
                sizeAlias?.let { mapOf(PARAM_SIZE_ALIAS to it) }.orEmpty()
            )
            .plus(
                variantId?.let { mapOf(PARAM_VARIANT_ID to it) }.orEmpty()
            )
            .plus(
                mapOf(PARAM_IMAGE_URL to imageUrl)
            )
            .plus(
                color?.let { mapOf(PARAM_COLOR to it) }.orEmpty()
            )
            .plus(
                gender?.let { mapOf(PARAM_GENDER to it) }.orEmpty()
            )
            .plus(
                mapOf(PARAM_UNIT_PRICE to unitPrice)
            )
            .plus(
                mapOf(PARAM_CURRENCY to currency)
            )
            .plus(
                mapOf(PARAM_QUANTITY to quantity)
            )
            .plus(
                url?.let { mapOf(PARAM_URL to it) }.orEmpty()
            )
    }

    private companion object {
        private const val PARAM_PRODUCT_ID = "productId"
        private const val PARAM_SIZE = "size"
        private const val PARAM_SIZE_ALIAS = "sizeAlias"
        private const val PARAM_VARIANT_ID = "variantId"
        private const val PARAM_IMAGE_URL = "imageUrl"
        private const val PARAM_COLOR = "color"
        private const val PARAM_GENDER = "gender"
        private const val PARAM_UNIT_PRICE = "unitPrice"
        private const val PARAM_CURRENCY = "currency"
        private const val PARAM_QUANTITY = "quantity"
        private const val PARAM_URL = "url"
    }
}