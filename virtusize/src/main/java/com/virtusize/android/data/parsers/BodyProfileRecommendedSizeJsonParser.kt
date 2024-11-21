package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.BodyProfileRecommendedSize
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.VirtualItem
import com.virtusize.android.data.remote.WillFitForSizes
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [BodyProfileRecommendedSize] object
 * @param product the store product that is associated with this recommendation
 */
internal class BodyProfileRecommendedSizeJsonParser(private val product: Product) :
    VirtusizeJsonParser<BodyProfileRecommendedSize> {
    override fun parse(json: JSONObject): BodyProfileRecommendedSize {
        val extProductId = json.optString(EXT_PRODUCT_ID)
        val fitScore = json.optDouble(FIT_SCORE, 0.0)
        val fitScoreDifference = json.optDouble(FIT_SCORE_DIFFERENCE, 0.0)
        val scenario = json.optString(SCENARIO)
        val secondFitScore = json.optDouble(SECOND_FIT_SCORE, 0.0)
        val secondSize = json.optString(SECOND_SIZE)
        val sizeName = json.optString(SIZE_NAME)
        val thresholdFitScore = json.optDouble(THRESHOLD_FIT_SCORE, 0.0)
        val willFit = json.optBoolean(WILL_FIT)
        val virtualItem =
            json.optJSONObject(VIRTUAL_ITEM_JSON_OBJ)?.let { vitrutalItem ->
                VirtualItem(
                    bust = vitrutalItem.optDouble(BUST, 0.0),
                    hip = vitrutalItem.optDouble(HIP, 0.0),
                    inseam = vitrutalItem.optDouble(INSEAM, 0.0),
                    sleeve = vitrutalItem.optDouble(SLEEVE, 0.0),
                    waist = vitrutalItem.optDouble(WAIST, 0.0),
                )
            }
        val willFitForSizes =
            json.optJSONObject(WILL_FIT_FOR_SIZES_JSON_OBJ)?.let { willFitForSize ->
                WillFitForSizes(
                    extraLarge = willFitForSize.optBoolean(EXTRA_LARGE),
                    extraSmall = willFitForSize.optBoolean(EXTRA_SMALL),
                    large = willFitForSize.optBoolean(LARGE),
                    medium = willFitForSize.optBoolean(MEDIUM),
                    small = willFitForSize.optBoolean(SMALL),
                )
            }

        return BodyProfileRecommendedSize(
            extProductId = extProductId,
            fitScore = fitScore,
            fitScoreDifference = fitScoreDifference,
            scenario = scenario,
            secondFitScore = secondFitScore,
            secondSize = secondSize,
            sizeName = sizeName,
            thresholdFitScore = thresholdFitScore,
            virtualItem = virtualItem,
            willFit = willFit,
            willFitForSizes = willFitForSizes,
        )
    }

    private companion object {
        const val SIZE_NAME = "sizeName"
        const val EXT_PRODUCT_ID = "extProductId"
        const val FIT_SCORE = "fitScore"
        const val FIT_SCORE_DIFFERENCE = "fitScoreDifference"
        const val SCENARIO = "scenario"
        const val SECOND_FIT_SCORE = "secondFitScore"
        const val SECOND_SIZE = "secondSize"
        const val THRESHOLD_FIT_SCORE = "thresholdFitScore"
        const val VIRTUAL_ITEM_JSON_OBJ = "virtualItem"
        const val WILL_FIT_FOR_SIZES_JSON_OBJ = "willFitForSizes"
        const val WILL_FIT = "willFit"
        const val BUST = "bust"
        const val HIP = "hip"
        const val INSEAM = "inseam"
        const val SLEEVE = "sleeve"
        const val WAIST = "waist"
        const val EXTRA_LARGE = "extra_large"
        const val EXTRA_SMALL = "extra_small"
        const val LARGE = "large"
        const val MEDIUM = "medium"
        const val SMALL = "small"
    }
}
