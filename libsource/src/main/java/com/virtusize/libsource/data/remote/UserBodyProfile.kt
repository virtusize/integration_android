package com.virtusize.libsource.data.remote

data class UserBodyProfile(
    val gender: String?,
    val age: Int,
    val height: Int,
    val weight: String,
    val bodyData: Set<Measurement>
)