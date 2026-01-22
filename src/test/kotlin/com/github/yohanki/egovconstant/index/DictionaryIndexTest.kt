package com.github.yohanki.egovconstant.index

import com.github.yohanki.egovconstant.data.EntryType
import com.github.yohanki.egovconstant.data.StdEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class DictionaryIndexTest {
    private fun entry(type: EntryType, ko: String, abbr: String?, synonyms: Set<String> = emptySet()): StdEntry =
        StdEntry(
            type = type,
            koName = ko,
            enAbbr = abbr,
            enName = null,
            description = null,
            synonyms = synonyms,
            forbiddenWords = emptySet(),
            source = com.github.yohanki.egovconstant.data.EntrySource.DEFAULT,
            version = 1
        )

    @Test
    fun testRankingOrder() {
        val eExact = entry(EntryType.WORD, "정확", "API_NM")
        val ePrefix = entry(EntryType.WORD, "접두", "API_NAME")
        val eContains = entry(EntryType.WORD, "포함", "MY_API_VALUE")
        val eFuzzy = entry(EntryType.WORD, "퍼지", "APO_NN")
        val idx = DictionaryIndex(listOf(eFuzzy, eContains, ePrefix, eExact))

        val results = idx.search(DictionaryIndex.Query(text = "API_NM"))
        // Expect exact first
        assertEquals(eExact, results.first().entry)
    }

    @Test
    fun testSynonymBoost() {
        val e1 = entry(EntryType.WORD, "단어1", "ABC_DEF", synonyms = setOf("테스트"))
        val e2 = entry(EntryType.WORD, "단어2", "ABD_DEF")
        val idx = DictionaryIndex(listOf(e1, e2))
        val results = idx.search(DictionaryIndex.Query(text = "테스"))
        assertEquals(e1, results.first().entry)
    }

    @Test
    fun testTriePrefixSearch() {
        val e1 = entry(EntryType.WORD, "학교", "SCHOOL")
        val e2 = entry(EntryType.WORD, "학생", "STUDENT")
        val e3 = entry(EntryType.WORD, "학원", "ACADEMY")
        val idx = DictionaryIndex(listOf(e1, e2, e3))

        // "학"으로 검색 시 학교, 학생, 학원이 모두 나와야 함
        val results = idx.search(DictionaryIndex.Query(text = "학"))
        assertEquals(3, results.size)
        val entries = results.map { it.entry }.toSet()
        assert(entries.contains(e1))
        assert(entries.contains(e2))
        assert(entries.contains(e3))

        // "학교"로 검색 시 학교만 나와야 함 (또는 점수가 압도적으로 높아야 함)
        val results2 = idx.search(DictionaryIndex.Query(text = "학교"))
        assertEquals(e1, results2.first().entry)
    }
}