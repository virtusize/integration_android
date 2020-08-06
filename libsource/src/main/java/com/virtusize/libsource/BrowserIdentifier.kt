package com.virtusize.libsource

import android.content.SharedPreferences
import com.virtusize.libsource.util.Constants.BID_KEY
import java.util.*
import kotlin.random.Random

/**
 * This class is used to get a browser identifier specific to this SDK
 * @param sharedPrefs Application specific Shared Preferences
 */
class BrowserIdentifier(private val sharedPrefs: SharedPreferences) {

    /**
     * Gets the browser identifier specific to this SDK
     * @return the browser identifier as String
     */
    fun getBrowserId(): String {
        return if(sharedPrefs.contains(BID_KEY)) {
            sharedPrefs.getString(BID_KEY, null) ?: generateAndStoreBrowserId()
        }
        else generateAndStoreBrowserId()
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
    fun storeBrowserId(bid: String) {
        val editor = sharedPrefs.edit()
        editor.putString(BID_KEY, bid)
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