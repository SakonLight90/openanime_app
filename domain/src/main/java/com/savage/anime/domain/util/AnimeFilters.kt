package com.savage.anime.domain.util

private val ITA_PAREN = Regex("\\s*\\(ITA\\)\\s*$", RegexOption.IGNORE_CASE)
private val ITA_PLAIN = Regex("\\s+ITA\\s*$", RegexOption.IGNORE_CASE)
private val DUB_PAREN = Regex("\\s*\\(DUB\\)\\s*$", RegexOption.IGNORE_CASE)
private val DUB_PLAIN = Regex("\\s+DUB\\s*$", RegexOption.IGNORE_CASE)
private val DUB_ITA_PAREN = Regex("\\s*\\((ITA|DUB)\\)\\s*$", RegexOption.IGNORE_CASE)
private val DUB_ITA_PLAIN = Regex("\\s+(ITA|DUB)\\s*$", RegexOption.IGNORE_CASE)

fun cleanAnimeTitle(title: String): String {
    return title
        .replace(ITA_PAREN, "")
        .replace(ITA_PLAIN, "")
        .replace(DUB_PAREN, "")
        .replace(DUB_PLAIN, "")
        .trim()
}

fun isItaDubVariant(title: String): Boolean {
    return DUB_ITA_PAREN.containsMatchIn(title) || DUB_ITA_PLAIN.containsMatchIn(title)
}

fun <T> List<T>.filterItaDubDuplicates(
    titleSelector: (T) -> String
): List<T> {
    val cleanedCount = mutableMapOf<String, MutableList<T>>()
    for (item in this) {
        val raw = titleSelector(item)
        val clean = cleanAnimeTitle(raw)
        cleanedCount.getOrPut(clean) { mutableListOf() }.add(item)
    }
    return filter { item ->
        val raw = titleSelector(item)
        val clean = cleanAnimeTitle(raw)
        val group = cleanedCount[clean] ?: return@filter false
        if (isItaDubVariant(raw)) {
            val hasOriginal = group.any { !isItaDubVariant(titleSelector(it)) }
            !hasOriginal
        } else {
            true
        }
    }
}
