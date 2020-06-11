package com.virtusize.libsource.data.remote

/**
 * This class represents the response of the request that retrieves the specific store info
 */
data class Store(
    val id: Int,
    val surveyLink: String,
    val name: String,
    val shortName: String,
    val lengthUnitId: Int,
    val apiKey: String,
    val created: String,
    val updated: String,
    val disabled: String,
    val typeMapperEnabled: Boolean,
    val region: String
)