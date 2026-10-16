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
        private const val PREFS_USER_BODY_PROFILE_KEY = "USER_BODY_PROFILE_KEY_VIRTUSIZE"
        private const val PREFS_KID_BODY_PROFILE_KEY = "KID_BODY_PROFILE_KEY_VIRTUSIZE"
        private const val PREFS_KID_PREDICTED_BODY_PROFILE_KEY = "KID_PREDICTED_BODY_PROFILE_KEY_VIRTUSIZE"

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

    // Adult/shoe `getUserBodyProfile` JSON and the kids widget profile are stored separately.

    /** Stores the encoded user body profile */
    fun storeUserBodyProfileJson(json: String) {
        preferences.edit().putString(PREFS_USER_BODY_PROFILE_KEY, json).apply()
    }

    /** Gets the encoded user body profile, or null if none is cached */
    fun getUserBodyProfileJson(): String? = preferences.getString(PREFS_USER_BODY_PROFILE_KEY, null)

    /** Deletes the cached user body profile */
    fun deleteUserBodyProfile() {
        preferences.edit().remove(PREFS_USER_BODY_PROFILE_KEY).apply()
    }

    /** Stores the encoded kids body profile */
    fun storeKidBodyProfileJson(json: String) {
        preferences.edit().putString(PREFS_KID_BODY_PROFILE_KEY, json).apply()
    }

    /** Gets the encoded kids body profile, or null if none is cached */
    fun getKidBodyProfileJson(): String? = preferences.getString(PREFS_KID_BODY_PROFILE_KEY, null)

    /** Deletes the cached kids body profile */
    fun deleteKidBodyProfile() {
        preferences.edit().remove(PREFS_KID_BODY_PROFILE_KEY).apply()
    }

    /** Stores the encoded predicted kids measurements */
    fun storeKidPredictedBodyProfileJson(json: String) {
        preferences.edit().putString(PREFS_KID_PREDICTED_BODY_PROFILE_KEY, json).apply()
    }

    /** Gets the encoded predicted kids measurements, or null if none are cached */
    fun getKidPredictedBodyProfileJson(): String? = preferences.getString(PREFS_KID_PREDICTED_BODY_PROFILE_KEY, null)

    /** Deletes the cached predicted kids measurements */
    fun deleteKidPredictedBodyProfile() {
        preferences.edit().remove(PREFS_KID_PREDICTED_BODY_PROFILE_KEY).apply()
    }

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
