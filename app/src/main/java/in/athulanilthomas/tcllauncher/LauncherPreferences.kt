package `in`.athulanilthomas.tcllauncher

import android.content.Context
import android.net.Uri
import org.json.JSONArray

internal class LauncherPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)
    fun favorites(defaults: List<String>): List<String?> = if (prefs.contains("favorites")) {
        val saved = readList("favorites")
        List(4) { saved.getOrNull(it)?.takeIf(String::isNotEmpty) }
    } else List(4) { defaults.getOrNull(it) }

    fun saveFavorites(values: List<String?>) {
        prefs.edit().putString("favorites", JSONArray(values.map { it.orEmpty() }).toString()).apply()
    }
    fun photos(): List<Uri> = readList("photos").map(Uri::parse)
    fun savePhotos(values: List<Uri>) {
        prefs.edit().putString("photos", JSONArray(values.map(Uri::toString)).toString()).apply()
    }
    private fun readList(key: String): List<String> = runCatching {
        val array = JSONArray(prefs.getString(key, "[]"))
        List(array.length()) { array.getString(it) }
    }.getOrDefault(emptyList())
}
