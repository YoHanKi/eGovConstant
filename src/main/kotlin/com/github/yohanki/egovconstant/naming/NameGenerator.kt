package com.github.yohanki.egovconstant.naming

object NameGenerator {
    // Known acronyms to preserve as upper-case tokens
    private val acronyms = setOf("API","CPU","GPU","IP","URL","HTML","XML","JSON","DB","UI","ID")

    // Suffix rules to keep short tokens upper-cased in camel-case tail
    private val suffixes = setOf("YN","CD","NO","SN","DT","DTTM")

    data class Variants(val camel: String, val snake: String, val pascal: String)

    fun fromAbbreviation(abbr: String): Variants {
        val tokens = tokenize(abbr)
        val camel = toCamel(tokens)
        val snake = toSnake(tokens)
        val pascal = toPascal(tokens)
        return Variants(camel, snake, pascal)
    }

    private fun tokenize(abbr: String): List<String> {
        // Split by underscores and non-alphanumeric boundaries and uppercase
        return abbr
            .trim()
            .replace('-', '_')
            .split('_')
            .filter { it.isNotBlank() }
            .map { it.trim().uppercase() }
    }

    private fun toCamel(tokens: List<String>): String {
        if (tokens.isEmpty()) return ""
        val first = lowerToken(tokens.first())
        val rest = tokens.drop(1).joinToString("") { camelToken(it) }
        return first + rest
    }

    private fun toSnake(tokens: List<String>): String = tokens.joinToString("_") { snakeToken(it) }

    private fun toPascal(tokens: List<String>): String = tokens.joinToString("") { camelToken(it) }

    private fun lowerToken(t: String): String {
        return if (t in acronyms || t in suffixes) t.lowercase().replaceFirstChar { it.lowercase() } else t.lowercase()
    }

    private fun camelToken(t: String): String {
        return when {
            t in acronyms -> t.capitalizeFirst() // API -> Api in Pascal/camel tail
            else -> t.lowercase().capitalizeFirst()
        }
    }

    private fun snakeToken(t: String): String {
        return when {
            t in suffixes -> t
            else -> t.lowercase()
        }
    }
}

private fun String.capitalizeFirst(): String =
    if (this.isEmpty()) this else this.substring(0, 1).uppercase() + this.substring(1).lowercase()
