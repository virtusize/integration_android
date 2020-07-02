package com.virtusize.libsource

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.ads.identifier.AdvertisingIdClient.isAdvertisingIdProviderAvailable
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import java.util.concurrent.Executors

/**
 * This class is used to get a unique identifier to each device
 * @param context Android Application Context
 */
class DeviceIdentifier(private val context: Context) {

    /**
     * Gets a unique device identifier
     *
     * By default, the SDK collects the Advertising ID to identify devices
     * If the Advertising ID is not available, the device's hardware identifier, Android ID (SSAID), is collected instead.
     *
     * @param callback passes back the unique ID string
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(callback: (String) -> Unit) {
        try {
            try {
                getAdvertisingIdByAndroidX(callback)
                return
            } catch (e: NoClassDefFoundError) {
                Log.d(Constants.LOG_TAG, "Package androidx.ads.identifier is not found")
            }
            try {
                getAdvertisingIdByGps(callback)
                return
            } catch (e: NoClassDefFoundError) {
                Log.d(Constants.LOG_TAG, "Package com.google.android.gms.ads.identifier is not found")
            }
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to get Advertising ID: '${e.message}'")
        }
        val androidID = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        callback(androidID)
    }

    /**
     * Gets the unique identifier by AndroidX library
     * @param callback passes back the Advertising ID
     */
    private fun getAdvertisingIdByAndroidX(callback: (String) -> Unit) {
        if (isAdvertisingIdProviderAvailable(context)) {
            val future = androidx.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context)
            future.addListener(Runnable {
                val info = future.get()
                callback(info.id)
            }, Executors.newSingleThreadExecutor())
        } else {
            Log.w(Constants.LOG_TAG, "Advertising ID provider is not available.")
        }
    }

    /**
     * Gets the unique identifier by Google Play Service
     * @param callback passes back the Advertising ID
     */
    private fun getAdvertisingIdByGps(callback: (String) -> Unit) {
        try {
            val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
            if (!info.isLimitAdTrackingEnabled) {
                callback(info.id)
            } else {
                Log.w(Constants.LOG_TAG, "Limit Ad Tracking is enabled.")
            }
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to get Advertising ID: '${e.message}'")
        }
    }
}