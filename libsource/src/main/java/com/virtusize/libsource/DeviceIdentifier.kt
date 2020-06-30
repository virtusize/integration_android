package com.virtusize.libsource

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
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
                Log.d(Constants.LOG_TAG, "Not found package: androidx.ads.identifier")
            }
            try {
                getAdvertisingIdByGps(callback)
                return
            } catch (e: NoClassDefFoundError) {
                Log.d(Constants.LOG_TAG, "Not found package: com.google.android.gms.ads.identifier")
            }
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to get AdvertisingId: '${e.message}'")
        }
        val androidID = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        Log.d(Constants.LOG_TAG, "Got Android ID (SSAID): $androidID")
        callback(androidID)
    }

    /**
     * Gets the unique identifier by AndroidX library
     * @param callback passes back the Advertising ID
     */
    private fun getAdvertisingIdByAndroidX(callback: (String) -> Unit) {
        if (androidx.ads.identifier.AdvertisingIdClient.isAdvertisingIdProviderAvailable(context)) {
            val future = androidx.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context)
            future.addListener(Runnable {
                val info = future.get()
                Log.d(Constants.LOG_TAG, "Got advertising id: ${info.id}")
                callback(info.id)
            }, Executors.newSingleThreadExecutor())
        } else {
            Log.w(Constants.LOG_TAG, "Advertising id is opt outed.")
        }
    }

    /**
     * Gets the unique identifier by Google Play Service
     * @param callback passes back the Advertising ID
     */
    private fun getAdvertisingIdByGps(callback: (String) -> Unit) {
        try {
            val info = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context)
            if (!info.isLimitAdTrackingEnabled) {
                Log.d(Constants.LOG_TAG, "Got advertising id: ${info.id}")
                callback(info.id)
            } else {
                Log.w(Constants.LOG_TAG, "Advertising id is opt outed.")
            }
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Failed to get AdvertisingId: '${e.message}'")
        }
    }
}