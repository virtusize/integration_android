package com.virtusize.android

import com.virtusize.android.data.local.VirtusizeOrder
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.metrics.SentryMetricsParameters
import java.util.UUID

/**
 * Utility for Sentry metrics and structured logs tracking in the Virtusize SDK.
 *
 * Requires Sentry Android SDK >= 8.0.0.
 * Configured via AndroidManifest.xml meta-data (DSN, logs, traces sample rate, environment).
 */
internal object VirtusizeSentryTracker {

    // MARK: - Session Management

    /** The current active session ID, set by Virtusize.load. */
    var currentSessionId: String = ""

    /**
     * Generates a new UUID session ID, stores it as the current session, and returns it.
     * Also configures the Sentry scope so all subsequent logs are tagged with the new session ID.
     */
    fun generateSessionId(): String {
        currentSessionId = UUID.randomUUID().toString()
        Sentry.configureScope { scope ->
            scope.setTag("session_id", currentSessionId)
        }
        return currentSessionId
    }

    // MARK: - Metrics (Counters)

    fun increment(key: String, tags: Map<String, String> = emptyMap()) {
        Sentry.metrics().count(key, 1.0)
    }

    // MARK: - Logs

    fun logInfo(message: String, attributes: Map<String, String> = emptyMap()) {
        Sentry.logger().info(message, attributes)
    }

    fun logWarning(message: String, attributes: Map<String, String> = emptyMap()) {
        Sentry.logger().warn(message, attributes)
    }

    fun logError(message: String, attributes: Map<String, String> = emptyMap()) {
        Sentry.logger().error(message, attributes)
    }

    // MARK: - WebView Events

    fun trackWebViewEvent(
        eventName: String,
        storeId: String? = null,
    ) {
        val tags = mutableMapOf("event_name" to eventName)
        storeId?.let { tags["store_id"] = it }
        logInfo("webview-$eventName", tags)
    }

    fun trackUserSawProduct(
        storeId: String? = null,
        externalProductId: String? = null,
    ) {
        val tags = buildTags(storeId = storeId)
        externalProductId?.let { tags["external_product_id"] = it }
        increment("user.saw.product", tags)
        logInfo("user-saw-product", tags)
    }

    // MARK: - Product Check

    fun trackProductCheck(externalProductId: String, isValid: Boolean, storeId: String? = null) {
        val tags = buildTags(storeId = storeId) +
            mapOf("external_product_id" to externalProductId, "is_valid" to isValid.toString())
        logInfo("product-check", tags)
    }

    fun trackLoadCancelled(step: String, externalProductId: String, storeId: String? = null) {
        val tags = buildTags(storeId = storeId) +
            mapOf("external_product_id" to externalProductId, "step" to step)
        logWarning("load-cancelled", tags)
    }

    // MARK: - Order

    fun trackSendOrder(order: VirtusizeOrder, storeId: String? = null) {
        val externalProductIds = order.items.mapNotNull { it.paramsToMap()["externalProductId"] as? String }
        if (externalProductIds.isEmpty()) {
            val tags = buildTags(storeId = storeId)
            increment("order.sent", tags)
        } else {
            for (externalProductId in externalProductIds) {
                val tags = buildTags(storeId = storeId)
                tags["external_product_id"] = externalProductId
                increment("order.sent", tags)
            }
        }
        logInfo("order-sent", buildTags(storeId = storeId))
    }

    // MARK: - Error

    fun trackError(
        throwable: Throwable,
        storeId: String? = null,
    ) {
        val tags = buildTags(storeId = storeId) +
            mapOf("error_type" to throwable::class.java.simpleName)
        increment("error", tags)
        logError(throwable.localizedMessage ?: throwable.message ?: throwable::class.java.simpleName, tags)
    }

    // MARK: - Private

    private fun buildTags(storeId: String? = null): MutableMap<String, String> {
        val tags = mutableMapOf<String, String>()
        storeId?.let { tags["store_id"] = it }
        return tags
    }
}
