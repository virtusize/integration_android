package com.virtusize.android.auth.data

/**
 * Represents a Virtusize user.
 *
 * @property id The user's ID.
 * @property snsType The user's social network type.
 * @property name The user's name.
 */
interface VirtusizeUser {
    val id: String
    val snsType: String
    val name: String
    val email: String
}
