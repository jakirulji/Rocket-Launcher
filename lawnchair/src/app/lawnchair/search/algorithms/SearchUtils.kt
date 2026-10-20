package app.lawnchair.search.algorithms

import android.content.Context
import android.content.pm.ShortcutInfo
import app.lawnchair.launcher
import app.lawnchair.ui.preferences.components.HiddenAppsInSearch
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.popup.PopupPopulator
import com.android.launcher3.search.StringMatcherUtility
import com.android.launcher3.shortcuts.ShortcutRequest
import java.util.Locale

object SearchUtils {
    fun normalSearch(apps: List<AppInfo>, query: String, maxResultsCount: Int, hiddenApps: Set<String>, hiddenAppsInSearch: String): List<AppInfo> {
        // Do an intersection of the words in the query and each title, and filter out all the
        // apps that don't match all of the words in the query.
        val queryTextLower = query.lowercase(Locale.getDefault())
        val matcher = StringMatcherUtility.StringMatcher.getInstance()
        return apps.asSequence()
            .filter { StringMatcherUtility.matches(queryTextLower, it.title.toString(), matcher) }
            .filterHiddenApps(queryTextLower, hiddenApps, hiddenAppsInSearch)
            .take(maxResultsCount)
            .toList()
    }

    fun fuzzySearch(apps: List<AppInfo>, query: String, maxResultsCount: Int, hiddenApps: Set<String>, hiddenAppsInSearch: String): List<AppInfo> {
        val queryTextLower = query.lowercase(Locale.getDefault())
        return apps.asSequence()
            .filterHiddenApps(queryTextLower, hiddenApps, hiddenAppsInSearch)
            .map { app -> app to weightedSimilarity(queryTextLower, app.sectionName + app.title) }
            .filter { (_, score) -> score >= 65 }
            .sortedByDescending { (_, score) -> score }
            .take(maxResultsCount)
            .map { (app, _) -> app }
            .toList()
    }

    /**
     * Local fuzzy-search score replacing the GPL-2.0-only fuzzywuzzy dependency.
     * Uses the best normalized Levenshtein similarity across the whole name and its tokens.
     */
    private fun weightedSimilarity(query: String, candidate: String): Int {
        val normalizedCandidate = candidate.lowercase(Locale.getDefault())
        val wholeScore = similarityRatio(query, normalizedCandidate)
        val tokenScore = normalizedCandidate.split(Regex("\\s+"))
            .maxOfOrNull { token -> similarityRatio(query, token) } ?: 0
        return maxOf(wholeScore, tokenScore)
    }

    private fun similarityRatio(left: String, right: String): Int {
        if (left.isEmpty() && right.isEmpty()) return 100
        if (left.isEmpty() || right.isEmpty()) return 0

        val longer = if (left.length >= right.length) left else right
        val shorter = if (left.length >= right.length) right else left
        var previous = IntArray(shorter.length + 1) { it }
        var current = IntArray(shorter.length + 1)

        for (i in longer.indices) {
            current[0] = i + 1
            for (j in shorter.indices) {
                val substitutionCost = if (longer[i] == shorter[j]) 0 else 1
                current[j + 1] = minOf(
                    current[j] + 1,
                    previous[j + 1] + 1,
                    previous[j] + substitutionCost,
                )
            }
            val temp = previous
            previous = current
            current = temp
        }

        val distance = previous[shorter.length]
        return ((longer.length - distance) * 100) / longer.length
    }

    fun getShortcuts(app: AppInfo, context: Context): List<ShortcutInfo> {
        val shortcuts = ShortcutRequest(context.launcher, app.user)
            .withContainer(app.targetComponent)
            .query(ShortcutRequest.PUBLISHED)
        return PopupPopulator.sortAndFilterShortcuts(shortcuts)
    }
}

fun Sequence<AppInfo>.filterHiddenApps(
    query: String,
    hiddenApps: Set<String>,
    hiddenAppsInSearch: String,
): Sequence<AppInfo> {
    return when (hiddenAppsInSearch) {
        HiddenAppsInSearch.ALWAYS -> {
            this
        }

        HiddenAppsInSearch.IF_NAME_TYPED -> {
            filter {
                it.toComponentKey().toString() !in hiddenApps ||
                    it.title.toString().lowercase(Locale.getDefault()) == query
            }
        }

        else -> {
            filter { it.toComponentKey().toString() !in hiddenApps }
        }
    }
}
