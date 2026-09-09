package com.virtusize.android

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import kotlin.random.Random

/**
 * This class is used to store and get the browser identifier and user auth data specific to this SDK
 */
class SharedPreferencesHelper {
    companion object {
        private const val SHARED_PREFS_NAME = "VIRTUSIZE_SHARED_PREFS"
        private const val PREFS_BID_KEY = "BID_KEY_VIRTUSIZE"
        private const val PREFS_AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY_VIRTUSIZE"
        private const val PREFS_ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY_VIRTUSIZE"
        private const val PREFS_SESSION_DATA_KEY = "SESSION_DATA_KEY_VIRTUSIZE"
        private const val PREFS_KID_GENDER_KEY = "KID_GENDER_KEY_VIRTUSIZE"
        private const val PREFS_KID_AGE_KEY = "KID_AGE_KEY_VIRTUSIZE"
        private const val PREFS_KID_HEIGHT_KEY = "KID_HEIGHT_KEY_VIRTUSIZE"
        private const val PREFS_KID_WEIGHT_KEY = "KID_WEIGHT_KEY_VIRTUSIZE"

        private var sharedPreferenceHelper: SharedPreferencesHelper? = null
        private lateinit var preferences: SharedPreferences

        // Gets the instance of [SharedPreferencesHelper]
        fun getInstance(context: Context): SharedPreferencesHelper {
            preferences =
                context.getSharedPreferences(
                    SHARED_PREFS_NAME,
                    Context.MODE_PRIVATE,
                )
            if (sharedPreferenceHelper == null) {
                sharedPreferenceHelper =
                    SharedPreferencesHelper()
            }
            return sharedPreferenceHelper as SharedPreferencesHelper
        }
    }

    /**
     * Stores the auth token for the session API
     */
    fun storeAuthToken(authToken: String?) {
        if (authToken == null) {
            return
        }

        preferences.edit().putString(PREFS_AUTH_TOKEN_KEY, authToken).apply()
    }

    /**
     * Gets the auth token for the session API
     * @return the auth token as a string
     */
    fun getAuthToken(): String? {
        return preferences.getString(PREFS_AUTH_TOKEN_KEY, null)
    }

    /**
     * Stores the access token for the session API
     */
    fun storeAccessToken(authToken: String) {
        preferences.edit().putString(PREFS_ACCESS_TOKEN_KEY, authToken).apply()
    }

    /**
     * Gets the access token for the session API
     * @return the access token as a string
     */
    fun getAccessToken(): String? {
        return preferences.getString(PREFS_ACCESS_TOKEN_KEY, null)
    }

    /**
     * Stores the session data from the session API
     */
    fun storeSessionData(sessionData: String) {
        preferences.edit().putString(PREFS_SESSION_DATA_KEY, sessionData).apply()
    }

    /**
     * Gets the session data from the session API
     * @return the session API response as a string
     */
    fun getSessionData(): String? {
        return preferences.getString(PREFS_SESSION_DATA_KEY, null)
    }

    // The kids flow keeps its inputs on the client only (the web widget uses localStorage),
    // so the SDK caches the values received from the widget events here.

    /**
     * Stores the kid's gender ("girl" or "boy") selected in the widget
     */
    fun storeKidGender(gender: String) {
        preferences.edit().putString(PREFS_KID_GENDER_KEY, gender).apply()
    }

    /**
     * Gets the kid's gender selected in the widget, or null if none was received
     */
    fun getKidGender(): String? = preferences.getString(PREFS_KID_GENDER_KEY, null)

    /**
     * Stores the kid's age in years entered in the widget
     */
    fun storeKidAge(age: Int) {
        preferences.edit().putInt(PREFS_KID_AGE_KEY, age).apply()
    }

    /**
     * Gets the kid's age in years, or null if none was received
     */
    fun getKidAge(): Int? = getIntOrNull(PREFS_KID_AGE_KEY)

    /**
     * Stores the kid's height in centimeters entered in the widget
     */
    fun storeKidHeight(heightInCm: Int) {
        preferences.edit().putInt(PREFS_KID_HEIGHT_KEY, heightInCm).apply()
    }

    /**
     * Gets the kid's height in centimeters, or null if none was received
     */
    fun getKidHeight(): Int? = getIntOrNull(PREFS_KID_HEIGHT_KEY)

    /**
     * Stores the kid's weight in kilograms entered in the widget
     */
    fun storeKidWeight(weightInKg: Int) {
        preferences.edit().putInt(PREFS_KID_WEIGHT_KEY, weightInKg).apply()
    }

    /**
     * Gets the kid's weight in kilograms, or null if none was received
     */
    fun getKidWeight(): Int? = getIntOrNull(PREFS_KID_WEIGHT_KEY)

    /**
     * Deletes the cached kid's body data
     */
    fun deleteKidBodyData() {
        preferences.edit()
            .remove(PREFS_KID_GENDER_KEY)
            .remove(PREFS_KID_AGE_KEY)
            .remove(PREFS_KID_HEIGHT_KEY)
            .remove(PREFS_KID_WEIGHT_KEY)
            .apply()
    }

    private fun getIntOrNull(key: String): Int? = if (preferences.contains(key)) preferences.getInt(key, 0) else null

    /**
     * Gets the browser identifier specific to this SDK
     * @return the browser identifier as String
     */
    fun getBrowserId(): String {
        return preferences.getString(PREFS_BID_KEY, null) ?: generateAndStoreBrowserId()
    }

    /**
     * Generates and stores a browser identifier in application level shared preferences
     * @return the browser identifier as String
     */
    private fun generateAndStoreBrowserId(): String {
        val bid = generateBrowserId()
        storeBrowserId(bid)
        return bid
    }

    /**
     * Stores a browser identifier in application level shared preferences
     */
    fun storeBrowserId(bid: String?) {
        if (bid == null) {
            return
        }
        val editor = preferences.edit()
        editor.putString(PREFS_BID_KEY, bid)
        editor.apply()
    }

    /**
     * Generates a browser identifier
     *
     * A Browser Identifier contains 23 random characters and 6 generated characters based on the current time
     * in milliseconds separated with a '.'
     * For example, cJhf5nGjDA0fgUXLAIz5Ls5.pfa1lp is a generated browser identifier
     * @sample cJhf5nGjDA0fgUXLAIz5Ls5.pfa1lp
     * @return the browser identifier as String
     */
    private fun generateBrowserId(): String {
        val browserIdentifier = StringBuilder("")
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        for (i in 0..21) {
            val randomNum = Random.nextInt(until = chars.length)
            browserIdentifier.append(chars[randomNum])
        }
        val timeUntil1970InMs = Calendar.getInstance().timeInMillis

        browserIdentifier.append('.')
        browserIdentifier.append(timeUntil1970InMs.toString(36).toLowerCase())

        return browserIdentifier.toString()
    }
}
