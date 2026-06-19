package com.savage.anime.domain.util

fun cleanAnimeTitle(title: String): String {
    return title
        .replace(Regex("\\s*\\(ITA\\)\\s*$", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*ITA\\s*$", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*\\(DUB\\)\\s*$", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*DUB\\s*$", RegexOption.IGNORE_CASE), "")
        .trim()
}

fun isItaDubVariant(title: String): Boolean {
    return Regex("\\s*\\((ITA|DUB)\\)\\s*$", RegexOption.IGNORE_CASE).containsMatchIn(title) ||
           Regex("\\s*(ITA|DUB)\\s*$", RegexOption.IGNORE_CASE).containsMatchIn(title)
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
        val group = cleanedCount[clean]!!
        if (isItaDubVariant(raw)) {
            val hasOriginal = group.any { !isItaDubVariant(titleSelector(it)) }
            !hasOriginal
        } else {
            true
        }
    }
}
