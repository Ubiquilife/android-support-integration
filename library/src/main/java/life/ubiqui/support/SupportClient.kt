package life.ubiqui.support

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SupportLookupItem(val id: String, val name: String, val icon: String? = null, val color: String? = null)

data class SupportTicketDraft(
    val title: String,
    val description: String,
    val categoryId: String? = null,
    val priorityId: String? = null,
    val reporterName: String? = null,
    val sourceUrl: String? = null,
    val contextData: Map<String, Any?> = emptyMap(),
)

/**
 * HTTP client for the Ubiquilife Support external API. All methods
 * are suspending — call them from a coroutine scope.
 */
class SupportClient(private val cfg: SupportConfig) {

    suspend fun categories(): List<SupportLookupItem> = lookup("categories")
    suspend fun priorities(): List<SupportLookupItem> = lookup("priorities")
    suspend fun statuses(): List<SupportLookupItem> = lookup("statuses")

    suspend fun createTicket(draft: SupportTicketDraft): Boolean = withContext(Dispatchers.IO) {
        val url = URL("${cfg.apiBaseUrl}/tickets")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer ${cfg.apiKey}")
        conn.doOutput = true

        val ctx = JSONObject(draft.contextData.mapValues { it.value ?: JSONObject.NULL }).apply {
            put("client_app", cfg.appName)
            put("client_platform", "android")
            put("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("system_version", "Android ${Build.VERSION.RELEASE}")
            put("sdk_int", Build.VERSION.SDK_INT)
        }

        val body = JSONObject().apply {
            put("title", draft.title)
            put("description", draft.description)
            put("source_app", cfg.appName)
            draft.categoryId?.let { put("category_id", it) }
            draft.priorityId?.let { put("priority_id", it) }
            (draft.reporterName ?: cfg.defaultReporterName)?.let { put("reporter_name", it) }
            draft.sourceUrl?.let { put("source_url", it) }
            cfg.identimeUserId?.let { put("identime_user_id", it) }
            put("context_data", ctx.toString())
        }

        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val ok = conn.responseCode in 200..299
        conn.disconnect()
        ok
    }

    private suspend fun lookup(path: String): List<SupportLookupItem> = withContext(Dispatchers.IO) {
        val url = URL("${cfg.apiBaseUrl}/lookup/$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Authorization", "Bearer ${cfg.apiKey}")
        val raw = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        parseLookup(raw)
    }

    /**
     * Normalises the lookup response. The Support API returns either a
     * bare array or `{ data: [...] }`, and `name` may be either a plain
     * string or a Spatie-translatable JSON object — pull the English
     * value when it's a dict.
     */
    private fun parseLookup(raw: String): List<SupportLookupItem> {
        val root = runCatching { JSONObject(raw) }.getOrNull()
        val arr: JSONArray = when {
            root != null && root.has("data") -> root.getJSONArray("data")
            root == null -> JSONArray(raw)
            else -> JSONArray()
        }
        val out = mutableListOf<SupportLookupItem>()
        for (i in 0 until arr.length()) {
            val row = arr.getJSONObject(i)
            val name = when (val n = row.opt("name")) {
                is String -> n
                is JSONObject -> n.optString("en", "")
                else -> ""
            }
            out += SupportLookupItem(
                id = row.optString("id"),
                name = name,
                icon = row.optString("icon").takeIf { it.isNotEmpty() },
                color = row.optString("color").takeIf { it.isNotEmpty() },
            )
        }
        return out
    }
}
