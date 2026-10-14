package com.savage.anime.ui.screens.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.navigation.Screen
import com.savage.anime.navigation.detailRoute
import com.savage.anime.ui.components.AnimeGridCard
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidth
import java.util.Calendar

private const val MIN_RELEASE_YEAR = 1970

private data class FilterOption(
    val value: String?,
    val labelRes: Int
)

private val TYPE_FILTERS = listOf(
    FilterOption(null, R.string.search_filter_all),
    FilterOption("anime", R.string.search_filter_anime),
    FilterOption("tv", R.string.search_filter_tv),
    FilterOption("movie", R.string.search_filter_film),
    FilterOption("ova", R.string.search_filter_ova),
    FilterOption("ona", R.string.search_filter_ona),
    FilterOption("special", R.string.search_filter_special)
)

private val STATUS_FILTERS = listOf(
    FilterOption(null, R.string.search_filter_all),
    FilterOption("ongoing", R.string.search_filter_in_corso),
    FilterOption("completed", R.string.search_filter_completato),
    FilterOption("upcoming", R.string.search_filter_imminente)
)

private val LANGUAGE_FILTERS = listOf(
    FilterOption(null, R.string.search_filter_all),
    FilterOption("jp", R.string.search_filter_giapponese),
    FilterOption("it", R.string.search_filter_italiano)
)

@Composable
fun SearchScreen(
    navController: NavController,
    onAzListClick: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var yearExpanded by rememberSaveable { mutableStateOf(false) }

    val yearOptions = remember {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        buildList {
            add(null to null)
            for (year in currentYear downTo MIN_RELEASE_YEAR) {
                add(year.toString() to year.toString())
            }
        }
    }

    val selectedYearLabel = uiState.selectedYear ?: stringResource(R.string.search_filter_year)
    val hasFilters = uiState.hasActiveFilters

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.search(it)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.search(query)
                            keyboardController?.hide()
                        }
                    )
                )

                Button(
                    onClick = onAzListClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
                ) {
                    Text(stringResource(R.string.az_list_title), color = Color.White)
                }

                Button(
                    onClick = { navController.navigate(Screen.Genres.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
                ) {
                    Text(stringResource(R.string.search_genres), color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterRow {
                    FilterDropdown(
                        selectedValue = uiState.selectedType,
                        allOptions = TYPE_FILTERS,
                        onSelect = { viewModel.setTypeFilter(it) }
                    )

                    FilterDropdown(
                        selectedValue = uiState.selectedStatus,
                        allOptions = STATUS_FILTERS,
                        onSelect = { viewModel.setStatusFilter(it) }
                    )
                }

                FilterRow {
                    FilterDropdown(
                        selectedValue = uiState.selectedLanguage,
                        allOptions = LANGUAGE_FILTERS,
                        onSelect = { viewModel.setLanguageFilter(it) }
                    )

                    Box {
                        FilterChip(
                            selected = uiState.selectedYear != null,
                            onClick = { yearExpanded = true },
                            label = { Text(selectedYearLabel, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color(0xFF222222)
                            )
                        )
                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            yearOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label ?: stringResource(R.string.search_filter_all),
                                            color = if (uiState.selectedYear == value) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.White
                                            }
                                        )
                                    },
                                    onClick = {
                                        viewModel.setYearFilter(value)
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (hasFilters) {
                        AssistChip(
                            onClick = { viewModel.clearFilters() },
                            label = { Text(stringResource(R.string.search_clear_filters), fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF333333)
                            )
                        )
                    }
                }
            }

            when {
                uiState.isLoadingAll -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.search_loading_all),
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                uiState.isLoading && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                query.isBlank() && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.search_empty_hint),
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )
                    }
                }

                uiState.error != null && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error.orEmpty(),
                                color = Color(0xFFCCCCCC),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.refresh() }) {
                                Text(
                                    text = stringResource(R.string.player_retry),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                uiState.results.isNotEmpty() -> {
                    val columns = gridColumnsForWidth(currentScreenWidthClass())
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.results, key = { it.id }) { anime ->
                            AnimeGridCard(
                                anime = anime,
                                onClick = { navController.navigate(detailRoute(anime.id)) }
                            )
                        }

                        if (uiState.hasMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isLoadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        TextButton(
                                            onClick = { viewModel.showMore() },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    R.string.show_more,
                                                    uiState.allResultsCount - uiState.results.size
                                                ),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (hasFilters) {
                                stringResource(R.string.search_no_results_filters)
                            } else {
                                stringResource(R.string.search_no_results)
                            },
                            color = Color.Gray,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@Composable
private fun FilterDropdown(
    selectedValue: String?,
    allOptions: List<FilterOption>,
    onSelect: (String?) -> Unit
) {
    val selectedLabel = allOptions.firstOrNull { it.value == selectedValue }?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.search_filter_all)
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selectedValue != null,
            onClick = { expanded = true },
            label = { Text(selectedLabel, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                containerColor = Color(0xFF222222)
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(option.labelRes),
                            color = if (selectedValue == option.value) MaterialTheme.colorScheme.primary else Color.White
                        )
                    },
                    onClick = {
                        onSelect(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}
