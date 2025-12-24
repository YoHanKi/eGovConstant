package com.github.yohanki.egovconstant.data

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CanonicalStoreMergeTest {

    private fun createEntry(
        type: EntryType,
        ko: String,
        abbr: String? = null,
        desc: String? = null,
        synonyms: Set<String> = emptySet()
    ) = StdEntry(
        type = type,
        koName = ko,
        enAbbr = abbr,
        enName = null,
        description = desc,
        synonyms = synonyms,
        source = EntrySource.DEFAULT
    )

    @Test
    fun testStableKey() {
        val e1 = createEntry(EntryType.TERM, "공통표준용어명", "ABBR")
        assertEquals("ABBR", e1.stableKey)

        val e2 = createEntry(EntryType.TERM, "  공통  표준  용어명  ")
        assertEquals("공통 표준 용어명", e2.stableKey)

        val e3 = createEntry(EntryType.WORD, "단어", "word")
        assertEquals("WORD", e3.stableKey)
    }

    @Test
    fun testMergeLogic() {
        val existing = createEntry(EntryType.WORD, "단어", "WORD", desc = "Old Description", synonyms = setOf("syn1"))
        val incoming = createEntry(EntryType.WORD, "단어", "WORD", desc = "New Description", synonyms = setOf("syn2"))

        // Merge policy: keep existing non-empty fields, union sets
        val store = CanonicalStore()
        val mergeMethod = store.javaClass.getDeclaredMethod("merge", StdEntry::class.java, StdEntry::class.java)
        mergeMethod.isAccessible = true
        val merged = mergeMethod.invoke(store, existing, incoming) as StdEntry

        assertEquals("Old Description", merged.description)
        assertEquals(setOf("syn1", "syn2"), merged.synonyms)
    }

    @Test
    fun testMergeEmptyToNonEmpty() {
        val existing = createEntry(EntryType.WORD, "단어", "WORD", desc = null)
        val incoming = createEntry(EntryType.WORD, "단어", "WORD", desc = "New Description")

        val store = CanonicalStore()
        val mergeMethod = store.javaClass.getDeclaredMethod("merge", StdEntry::class.java, StdEntry::class.java)
        mergeMethod.isAccessible = true
        val merged = mergeMethod.invoke(store, existing, incoming) as StdEntry

        assertEquals("New Description", merged.description)
    }
}
