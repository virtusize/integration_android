package com.virtusize.libsource

import android.content.Context
import android.content.SharedPreferences
import java.util.*
import kotlin.random.Random

/**
 * This class is used to get a browser identifier and user auth data specific to this SDK
 * @param context the application context
 */
internal class SharedPreferencesHelper {

    companion object {
        private var obj: SharedPreferencesHelper? = null
        private lateinit var preferences: SharedPreferences
        fun getInstance(context: Context): SharedPreferencesHelper {
            preferences = context.getSharedPreferences(
                SHARED_PREFS_NAME,
                Context.MODE_PRIVATE
            )
            if (obj == null) {
                obj = SharedPreferencesHelper()
            }
            return obj as SharedPreferencesHelper
        }

        private const val SHARED_PREFS_NAME = "VIRTUSIZE_SHARED_PREFS"
        private const val PREFS_BID_KEY = "BID_KEY_VIRTUSIZE"
        private const val PREFS_AUTH_HEADER_KEY = "AUTH_HEADER_KEY_VIRTUSIZE"
        private const val PREFS_AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY_VIRTUSIZE"
    }

    fun setAuthHeader(authHeader: String?) {
        if(authHeader == null) {
            return
        }
        preferences.edit().putString(PREFS_AUTH_HEADER_KEY, authHeader).apply()
    }

    fun getAuthHeader(): String? {
        return preferences.getString(PREFS_AUTH_HEADER_KEY, null)
    }

    fun setAuthToken(authToken: String) {
        preferences.edit().putString(PREFS_AUTH_TOKEN_KEY, authToken).apply()
    }

    fun getAuthToken(): String? {
        return preferences.getString(PREFS_AUTH_TOKEN_KEY, null)
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
        if(bid == null) {
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