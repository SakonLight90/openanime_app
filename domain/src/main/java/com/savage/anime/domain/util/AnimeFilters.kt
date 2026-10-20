package com.savage.anime.domain.util

private val ITA_TRAILING = Regex("\\s*\\(ITA\\)(\\s*\\([A-Za-z]{2,4}\\))*\\s*$", RegexOption.IGNORE_CASE)

private val ITA_TOKEN = Regex("\\(\\s*ITA\\s*\\)", RegexOption.IGNORE_CASE)

fun cleanAnimeTitle(title: String): String {
    return title.replace(ITA_TRAILING, "").trim()
}

private fun isItaVariant(title: String): Boolean {
    return ITA_TOKEN.containsMatchIn(title)
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
