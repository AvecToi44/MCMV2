package org.atrsx.wizardscena

import androidx.compose.runtime.mutableStateListOf
import java.io.File
import java.util.prefs.Preferences

object RecentScenariosStore {
    private const val PREF_NODE = "org.atrsx.wizardscena"
    private const val PREF_COUNT = "recent.count"
    private const val PREF_ITEM_PREFIX = "recent.item."
    private const val MAX_ITEMS = 5
    private val prefs: Preferences = Preferences.userRoot().node(PREF_NODE)

    val recentPaths = mutableStateListOf<String>()

    init {
        load()
    }

    private fun normalize(path: String): String {
        return runCatching { File(path).canonicalPath }.getOrElse { path }
    }

    fun add(path: String) {
        val normalized = normalize(path)
        val next = mutableListOf(normalized)
        next += recentPaths.filterNot { it == normalized }
        recentPaths.clear()
        recentPaths.addAll(next.take(MAX_ITEMS))
        save()
    }

    fun remove(path: String) {
        val normalized = normalize(path)
        recentPaths.removeAll { it == normalized }
        save()
    }

    private fun load() {
        recentPaths.clear()
        val count = prefs.getInt(PREF_COUNT, 0).coerceIn(0, MAX_ITEMS)
        repeat(count) { idx ->
            val item = prefs.get("$PREF_ITEM_PREFIX$idx", "").trim()
            if (item.isNotEmpty()) {
                recentPaths.add(item)
            }
        }
    }

    private fun save() {
        prefs.putInt(PREF_COUNT, recentPaths.size)
        repeat(MAX_ITEMS) { idx ->
            val value = recentPaths.getOrNull(idx)
            if (value == null) {
                prefs.remove("$PREF_ITEM_PREFIX$idx")
            } else {
                prefs.put("$PREF_ITEM_PREFIX$idx", value)
            }
        }
    }
}
