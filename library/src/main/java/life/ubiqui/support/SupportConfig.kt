package life.ubiqui.support

/**
 * Runtime configuration for the support integration.
 *
 * Construct one and hand it to [SupportClient] / [SupportFAB].
 */
data class SupportConfig(
    /** Base URL of the Support external API, no trailing slash. */
    val apiBaseUrl: String,

    /** Bearer token issued by the Support app's external API tokens page. */
    val apiKey: String,

    /** Reported as `source_app` on every ticket. */
    val appName: String,

    /** Optional default reporter name. */
    val defaultReporterName: String? = null,

    /** Optional IdentiMe user ID for cross-platform user correlation. */
    val identimeUserId: String? = null,
)
