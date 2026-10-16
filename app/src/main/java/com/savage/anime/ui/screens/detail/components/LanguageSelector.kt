package com.savage.anime.ui.screens.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savage.anime.R
import com.savage.anime.domain.models.RelatedVersion
import com.savage.anime.ui.utils.languageLabelRes

private const val CURRENT_VERSION_ID = -1

@Composable
fun LanguageSelector(
    selectedVersionId: Int?,
    currentAnimeId: Int,
    currentTitle: String,
    currentLanguage: String,
    currentIsDub: Boolean,
    versions: List<RelatedVersion>,
    onLanguageSelected: (Int?) -> Unit
) {
    val allOptions = buildList {
        add(
            RelatedVersion(
                id = CURRENT_VERSION_ID,
                title = currentTitle,
                language = currentLanguage,
                isDub = currentIsDub
            )
        )
        addAll(versions)
    }

    val singleVersion = allOptions.size <= 1
    val singleVersionLanguage = allOptions.firstOrNull()?.language

    Row(verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(allOptions, key = { it.id }) { option ->
                val isSelected = if (option.id == CURRENT_VERSION_ID)
                    selectedVersionId == null || selectedVersionId == currentAnimeId
                else
                    option.id == selectedVersionId
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1A1A1A)
                val textColor = if (isSelected) Color.White else Color(0xFFB3B3B3)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgColor)
                        .then(
                            if (!singleVersion) Modifier.clickable {
                                onLanguageSelected(
                                    if (option.id == CURRENT_VERSION_ID) null else option.id
                                )
                            } else Modifier
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(languageLabelRes(option.language)),
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (singleVersion) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(
                    R.string.detail_only_version,
                    stringResource(languageLabelRes(singleVersionLanguage))
                ),
                color = Color(0xFF666666),
                fontSize = 12.sp
            )
        }
    }
}
