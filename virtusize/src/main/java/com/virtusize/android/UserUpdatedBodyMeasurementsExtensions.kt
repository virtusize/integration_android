package com.virtusize.android

import com.virtusize.android.data.local.VirtusizeEvent

/**
 * Returns the recommended size carried by a body-measurements event.
 *
 * JSONObject.optString returns an empty string for a missing key, but the SDK treats
 * missing and empty recommendations as absent.
 */
internal fun VirtusizeEvent.UserUpdatedBodyMeasurements.recommendedSizeName(): String? =
    data
        ?.optString("sizeRecName")
        ?.takeIf { it.isNotEmpty() }
