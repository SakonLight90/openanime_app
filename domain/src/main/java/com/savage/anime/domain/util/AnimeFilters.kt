package com.savage.anime.domain.util

private val ITA_PAREN = Regex("\\s*\\(ITA\\)\\s*$", RegexOption.IGNORE_CASE)

fun cleanAnimeTitle(title: String): String {
    return title.replace(ITA_PAREN, "").trim()
}

private fun isItaVariant(title: String): Boolean {
    return ITA_PAREN.containsMatchIn(title)
}

fun <T> List<T>.filterItaDubDuplicates(
    titleSelector: (T) -> String
): List<T> {
    val groups = mutableMapOf<String, MutableList<T>>()
    for (item in this) {
        val clean = cleanAnimeTitle(titleSelector(item))
        groups.getOrPut(clean) { mutableListOf() }.add(item)
    }
    return filter { item ->
        val raw = titleSelector(item)
        val clean = cleanAnimeTitle(raw)
        val group = groups[clean] ?: return@filter false
        if (isItaVariant(raw)) {
            val hasOriginal = group.any { !isItaVariant(titleSelector(it)) }
            !hasOriginal
        } else {
            true
        }
    }
}
