package com.virtusize.android.auth.data

/**
 * A class representing a Google user. It inherits from [VirtusizeUser].
 *
 * @property sub The user's unique ID.
 * @property givenName The user's given name.
 * @property familyName The user's family name.
 * @property locale The user's locale.
 * @property pictureUrl The user's profile picture URL.
 */
internal data class GoogleUser(
    val sub: String,
    val givenName: String,
    val familyName: String,
    override val name: String,
    override val email: String,
    val locale: String,
    val pictureUrl: String,
) : VirtusizeUser {
    override val id: String
        get() = sub
    override val snsType: String
        get() = SnsType.GOOGLE.value
}
