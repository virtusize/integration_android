package com.virtusize.libsource

import android.content.SharedPreferences
import com.virtusize.libsource.Constants.BID_KEY
import java.util.*
import kotlin.random.Random

class BrowserIdentifier(private val sharedPrefs: SharedPreferences) {

    fun getBrowserId(): String {
        return if(sharedPrefs.contains(BID_KEY)) {
            sharedPrefs.getString(BID_KEY, null) ?: generateAndStoreBrowserId()
        }
        else generateAndStoreBrowserId()
    }

    private fun generateAndStoreBrowserId(): String {
        val bid = generateBrowserId()
        val editor = sharedPrefs.edit()
        editor.putString(BID_KEY, bid)
        editor.apply()
        return bid
    }

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