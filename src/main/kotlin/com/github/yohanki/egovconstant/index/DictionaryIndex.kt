package com.github.yohanki.egovconstant.index

import com.github.yohanki.egovconstant.data.EntryType
import com.github.yohanki.egovconstant.data.StdEntry
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

class DictionaryIndex(entries: List<StdEntry>) {
    private val all: List<StdEntry> = entries
    private val normalizedCache = ConcurrentHashMap<StdEntry, NormalizedEntry>()
    private val trie = TrieNode()

    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        val entries = mutableSetOf<StdEntry>()

        fun insert(key: String, entry: StdEntry) {
            var curr = this
            for (char in key) {
                curr = curr.children.getOrPut(char) { TrieNode() }
            }
            curr.entries.add(entry)
        }

        fun find(key: String): TrieNode? {
            var curr = this
            for (char in key) {
                curr = curr.children[char] ?: return null
            }
            return curr
        }

        fun collectAll(result: MutableSet<StdEntry>, limit: Int) {
            if (result.size >= limit) return
            result.addAll(entries)
            for (child in children.values) {
                if (result.size >= limit) return
                child.collectAll(result, limit)
            }
        }
    }

    private class NormalizedEntry(
        val entry: StdEntry,
        val ko: String,
        val abbr: String?,
        val en: String?,
        val synonyms: List<String>,
        val desc: String?
    )

    init {
        for (e in entries) {
            val ne = NormalizedEntry(
                e,
                norm(e.koName),
                e.enAbbr?.let { norm(it) },
                e.enName?.let { norm(it) },
                e.synonyms.map { norm(it) },
                e.description?.let { norm(it) }
            )
            normalizedCache[e] = ne

            for (t in e.allTokens()) {
                val key = norm(t)
                trie.insert(key, e)
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

    fun search(q: Query, limit: Int = Int.MAX_VALUE): List<Ranked> {
        val key = if (q.text.isNotBlank()) norm(q.text) else ""
        
        val sequence = if (key.isEmpty()) {
            all.asSequence()
        } else {
            val candidates = mutableSetOf<StdEntry>()
            // Trie prefix search
            trie.find(key)?.collectAll(candidates, limit)
            
            // fallback: all for fuzzy scan if none
            if (candidates.isEmpty()) candidates.addAll(all)
            candidates.asSequence()
        }

        val filtered = sequence.filter { e ->
            (q.type == null || e.type == q.type) &&
            (q.domainGroup == null || e.domainGroup == q.domainGroup) &&
            (q.domainCategory == null || e.domainCategory == q.domainCategory)
        }

        if (key.isEmpty()) {
            return filtered.take(limit).map { Ranked(it, 0) }.toList()
        }

        return filtered.map { e -> e to score(e, key) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { Ranked(it.first, it.second) }
            .toList()
    }

    private fun score(e: StdEntry, key: String): Int {
        val ne = normalizedCache[e] ?: return 0
        var s = 0
        
        // Match weights
        val EXACT_MATCH = 2000
        val PREFIX_MATCH = 800
        val CONTAINS_MATCH = 400
        val SYNONYM_BOOST = 200
        val DESCRIPTION_BOOST = 50

        // 1. Korean Name Match (Highest priority)
        val koNorm = ne.ko
        if (koNorm == key) s += EXACT_MATCH
        else if (koNorm.startsWith(key)) s += PREFIX_MATCH
        else if (koNorm.contains(key)) s += CONTAINS_MATCH

        // 2. English Abbreviation Match
        val abbrNorm = ne.abbr
        if (abbrNorm != null) {
            if (abbrNorm == key) s += EXACT_MATCH - 100
            else if (abbrNorm.startsWith(key)) s += PREFIX_MATCH - 50
            else if (abbrNorm.contains(key)) s += CONTAINS_MATCH - 20
        }

        // 3. English Name Match
        val enNorm = ne.en
        if (enNorm != null) {
            if (enNorm == key) s += EXACT_MATCH - 200
            else if (enNorm.startsWith(key)) s += PREFIX_MATCH - 100
            else if (enNorm.contains(key)) s += CONTAINS_MATCH - 50
        }

        // 4. Synonym Boost
        for (synNorm in ne.synonyms) {
            if (synNorm == key) s += SYNONYM_BOOST
            else if (synNorm.startsWith(key)) s += SYNONYM_BOOST / 2
        }

        // 5. Description Boost
        if (ne.desc?.contains(key) == true) {
            s += DESCRIPTION_BOOST
        }

        // 6. Fuzzy Match (Only if score is low or matches were weak)
        if (s < CONTAINS_MATCH) {
            val d = dist(koNorm, key)
            if (d <= minOf(2, key.length / 2)) {
                s += 100 - d * 30
            }
        }

        // 7. Type weight (Terms > Words > Domains)
        s += when (e.type) {
            EntryType.TERM -> 30
            EntryType.WORD -> 20
            EntryType.DOMAIN -> 10
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