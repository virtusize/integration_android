package com.virtusize.android.util

/**
 * Orders the strings the way JavaScript iterates the keys of an object keyed by them:
 * integer-like keys (e.g. "110", "120") first in ascending numeric order, then the
 * other keys in their original order.
 *
 * Used to send size names in the same order as the web widget, since the
 * size recommendation API result depends on it.
 */
fun List<String>.sortedLikeJavaScriptObjectKeys(): List<String> {
    val integerKeys =
        mapNotNull { key -> javaScriptIndexKey(key)?.let { key to it } }
            .sortedBy { it.second }
            .map { it.first }
    val otherKeys = filter { javaScriptIndexKey(it) == null }
    return integerKeys + otherKeys
}

/**
 * JavaScript treats only canonical non-negative integers below 2^32 - 1 as index keys
 */
private fun javaScriptIndexKey(key: String): Long? {
    val value = key.toLongOrNull() ?: return null
    if (value < 0 || value >= MAX_INDEX_KEY || value.toString() != key) {
        return null
    }
    return value
}

private const val MAX_INDEX_KEY = 4294967295L
