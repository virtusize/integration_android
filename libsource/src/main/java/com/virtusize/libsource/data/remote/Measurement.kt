package com.virtusize.libsource.data.remote

/**
 * This class represents the measurement info
 * @param name the measurement name, e.g. "height", "bust", "sleeve", etc.
 * @param millimeter the measurement value in millimeters
 */
data class Measurement(
    val name: String,
    val millimeter: Int
)
