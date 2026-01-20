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
        val key = if (q.text.isNotBlank()) norm(q.text) else ""
        
        val filtered = if (key.isEmpty()) {
            all.asSequence()
        } else {
            val candidates = mutableSetOf<StdEntry>()
            // direct bucket
            byToken[key]?.let { candidates.addAll(it) }
            
            // prefix & contains - using a more efficient way might be possible if we have a lot of tokens,
            // but for few thousands, this is usually fine.
            // Optimization: Only scan keys if candidates are few or it's needed
            for ((token, entries) in byToken) {
                if (token.startsWith(key) || token.contains(key)) {
                    candidates.addAll(entries)
                }
            }
            
            // fallback: all for fuzzy scan if none
            if (candidates.isEmpty()) candidates.addAll(all)
            candidates.asSequence()
        }.filter { e ->
            (q.type == null || e.type == q.type) &&
            (q.domainGroup == null || e.domainGroup == q.domainGroup) &&
            (q.domainCategory == null || e.domainCategory == q.domainCategory)
        }

        if (key.isEmpty()) {
            return filtered.map { Ranked(it, 0) }.toList()
        }

        return filtered.map { e -> e to score(e, key) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { Ranked(it.first, it.second) }
            .toList()
    }

    private fun score(e: StdEntry, key: String): Int {
        var s = 0
        fun bump(weight: Int) { s += weight }
        
        // Match weights
        val EXACT_MATCH = 2000
        val PREFIX_MATCH = 800
        val CONTAINS_MATCH = 400
        val SYNONYM_BOOST = 200
        val DESCRIPTION_BOOST = 50

        // 1. Korean Name Match (Highest priority)
        val koNorm = norm(e.koName)
        if (koNorm == key) bump(EXACT_MATCH)
        else if (koNorm.startsWith(key)) bump(PREFIX_MATCH)
        else if (koNorm.contains(key)) bump(CONTAINS_MATCH)

        // 2. English Abbreviation Match
        val abbrNorm = e.enAbbr?.let { norm(it) }
        if (abbrNorm != null) {
            if (abbrNorm == key) bump(EXACT_MATCH - 100)
            else if (abbrNorm.startsWith(key)) bump(PREFIX_MATCH - 50)
            else if (abbrNorm.contains(key)) bump(CONTAINS_MATCH - 20)
        }

        // 3. English Name Match
        val enNorm = e.enName?.let { norm(it) }
        if (enNorm != null) {
            if (enNorm == key) bump(EXACT_MATCH - 200)
            else if (enNorm.startsWith(key)) bump(PREFIX_MATCH - 100)
            else if (enNorm.contains(key)) bump(CONTAINS_MATCH - 50)
        }

        // 4. Synonym Boost
        for (syn in e.synonyms) {
            val synNorm = norm(syn)
            if (synNorm == key) bump(SYNONYM_BOOST)
            else if (synNorm.startsWith(key)) bump(SYNONYM_BOOST / 2)
        }

        // 5. Description Boost
        if (e.description?.let { norm(it) }?.contains(key) == true) {
            bump(DESCRIPTION_BOOST)
        }

        // 6. Fuzzy Match (Only if score is low or matches were weak)
        if (s < CONTAINS_MATCH) {
            val d = dist(koNorm, key)
            if (d <= minOf(2, key.length / 2)) {
                bump(100 - d * 30)
            }
        }

        // 7. Type weight (Terms > Words > Domains)
        when (e.type) {
            EntryType.TERM -> bump(30)
            EntryType.WORD -> bump(20)
            EntryType.DOMAIN -> bump(10)
        }

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