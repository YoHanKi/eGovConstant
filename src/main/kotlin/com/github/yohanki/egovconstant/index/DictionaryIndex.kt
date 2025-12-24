package com.github.yohanki.egovconstant.index

import com.github.yohanki.egovconstant.data.EntryType
import com.github.yohanki.egovconstant.data.StdEntry
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

class DictionaryIndex(entries: List<StdEntry>) {
    private val all: List<StdEntry> = entries
    private val byToken: MutableMap<String, MutableList<StdEntry>> = ConcurrentHashMap()

    init {
        for (e in entries) {
            for (t in e.allTokens()) {
                val key = norm(t)
                byToken.computeIfAbsent(key) { mutableListOf() }.add(e)
            }
        }
    }

    data class Query(
        val text: String,
        val type: EntryType? = null,
        val domainGroup: String? = null,
        val domainCategory: String? = null,
    )

    data class Ranked(val entry: StdEntry, val score: Int)

    fun search(q: Query): List<Ranked> {
        val filtered = if (q.text.isBlank()) {
            all.asSequence()
        } else {
            val key = norm(q.text)
            val candidates = mutableSetOf<StdEntry>()
            // direct bucket
            byToken[key]?.let { candidates.addAll(it) }
            // prefix & contains
            byToken.keys.filter { it.startsWith(key) || it.contains(key) }.forEach { k ->
                byToken[k]?.let { candidates.addAll(it) }
            }
            // fallback: all for fuzzy scan if none
            if (candidates.isEmpty()) candidates.addAll(all)
            candidates.asSequence()
        }.filter { e ->
            (q.type == null || e.type == q.type) &&
            (q.domainGroup == null || e.domainGroup == q.domainGroup) &&
            (q.domainCategory == null || e.domainCategory == q.domainCategory)
        }

        if (q.text.isBlank()) {
            return filtered.map { Ranked(it, 0) }.toList()
        }

        val key = norm(q.text)
        return filtered.map { e -> e to score(e, key) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { Ranked(it.first, it.second) }
            .toList()
    }

    private fun score(e: StdEntry, key: String): Int {
        var s = 0
        fun bump(weight: Int) { s += weight }
        // consider koName, enAbbr, enName, description, synonyms
        val fields = listOfNotNull(e.koName, e.enAbbr, e.enName, e.description) + e.synonyms
        for (f in fields) {
            val n = norm(f)
            if (n == key) bump(1000)
            else if (n.startsWith(key)) bump(400)
            else if (n.contains(key)) bump(200)
            else {
                val d = dist(n, key)
                if (d <= min(2, key.length / 2)) bump(50 - d * 10)
            }
        }
        // description boost (lower than name)
        if (e.description?.let { norm(it) }?.contains(key) == true) bump(50)
        // synonym boost
        if (e.synonyms.any { norm(it) == key || norm(it).startsWith(key) }) bump(100)
        return s
    }

    private fun norm(s: String): String = s.lowercase().replace(" ", "").replace("_", "")

    // Levenshtein distance (small, simple)
    private fun dist(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        if (m == 0) return n
        if (n == 0) return m
        val dp = IntArray(n + 1) { it }
        for (i in 1..m) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..n) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + cost)
                prev = temp
            }
        }
        return dp[n]
    }
}