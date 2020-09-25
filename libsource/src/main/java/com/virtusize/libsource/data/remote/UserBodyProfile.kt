package com.virtusize.libsource.data.remote

// TODO: add comment
data class UserBodyProfile(
    val gender: String,
    val age: Int,
    val height: Int,
    val weight: String,
    val bodyData: Set<Measurement>
)