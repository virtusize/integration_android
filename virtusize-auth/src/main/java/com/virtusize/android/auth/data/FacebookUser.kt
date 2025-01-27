package com.virtusize.android.auth.data

/**
 * A class representing a Facebook user. It inherits from [VirtusizeUser].
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property email The user's email.
 */
data class FacebookUser(
    override val id: String,
    val firstName: String,
    val lastName: String,
    override val name: String,
    override val email: String
): VirtusizeUser {
    override val snsType: String
        get() = SnsType.FACEBOOK.value
}