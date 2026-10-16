package com.savage.anime.ui.utils

import androidx.annotation.StringRes
import com.savage.anime.R

@StringRes
fun languageLabelRes(code: String?): Int {
    return when (code?.trim()?.lowercase()) {
        "it" -> R.string.detail_version_italian
        "ch" -> R.string.search_filter_cinese
        "kr" -> R.string.search_filter_coreano
        "en" -> R.string.search_filter_inglese
        "fr" -> R.string.search_filter_francese
        "jp", null, "" -> R.string.detail_version_japanese
        else -> R.string.detail_version_japanese
    }
}