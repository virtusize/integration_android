package com.virtusize.android.data.remote

data class BodyProfileRecommendedSizeNew(
    val extProductId: String,
    val fitScore: Double,
    val fitScoreDifference: Double,
    val scenario: String,
    val secondFitScore: Double,
    val secondSize: String,
    val sizeName: String,
    val thresholdFitScore: Double,
    val virtualItem: VirtualItem?,
    val willFit: Boolean,
    val willFitForSizes: WillFitForSizes?
)
