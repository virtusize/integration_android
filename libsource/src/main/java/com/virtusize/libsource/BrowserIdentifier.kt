package com.virtusize.libsource

import android.content.SharedPreferences
import com.virtusize.libsource.Constants.BID_KEY
import java.util.*
import kotlin.random.Random

/**
 * This class is used to get BrowserIdentifier specific to this application
 * @param sharedPrefs Application specific Shared Preferences
 */
class BrowserIdentifier(private val sharedPrefs: SharedPreferences) {

    /**
     * This method is used to get Browser Identifier specific to this app
     * @return Browser Identifier as String
     */
    fun getBrowserId(): String {
        return if(sharedPrefs.contains(BID_KEY)) {
            sharedPrefs.getString(BID_KEY, null) ?: generateAndStoreBrowserId()
        }
        else generateAndStoreBrowserId()
    }

    /**
     * This method is used to generate and store browser identifier in application level shared preferences
     * @return Browser Identifier as String
     */
    private fun generateAndStoreBrowserId(): String {
        val bid = generateBrowserId()
        val editor = sharedPrefs.edit()
        editor.putString(BID_KEY, bid)
        editor.apply()
        return bid
    }

    /**
     * This method is used to generate Browser Identifier of the format cJhf5nGjDA0fgUXLAIz5Ls5.pfa1lp
     * @return Browser Identifier as String
     */
    private fun generateBrowserId(): String {
        // Format: cJhf5nGjDA0fgUXLAIz5Ls5.pfa1lp
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