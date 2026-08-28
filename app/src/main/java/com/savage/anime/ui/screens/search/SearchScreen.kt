package com.savage.anime.ui.screens.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.domain.models.Anime
import com.savage.anime.navigation.Screen
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidth
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onAzListClick: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                        if (it.isNotEmpty()) viewModel.search(it)
                        else viewModel.clearResults()
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (query.isNotEmpty()) {
                                viewModel.search(query)
                                keyboardController?.hide()
                            }
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
                    Text("Generi", color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(null to "Tutti", "anime" to "TV", "movie" to "Film", "ova" to "OVA", "ona" to "ONA", "special" to "Special")
                    types.forEach { (type, label) ->
                        FilterChip(
                            selected = uiState.selectedType == type,
                            onClick = { viewModel.setTypeFilter(type) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color(0xFF222222)
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statuses = listOf(null to "Tutti", "completed" to "Completato", "ongoing" to "In corso", "upcoming" to "Imminente")
                    statuses.forEach { (status, label) ->
                        FilterChip(
                            selected = uiState.selectedStatus == status,
                            onClick = { viewModel.setStatusFilter(status) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color(0xFF222222)
                            )
                        )
                    }
                }

                var yearExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val yearOptions = buildList {
                        add(null to "Tutti")
                        for (y in currentYear downTo 1970 step 1) {
                            add(y.toString() to y.toString())
                        }
                    }
                    val selectedYearLabel = yearOptions.firstOrNull { it.first == uiState.selectedYear }?.second ?: "Anno"

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
                            yearOptions.forEach { (year, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            color = if (uiState.selectedYear == year) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    },
                                    onClick = {
                                        viewModel.setYearFilter(year)
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
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
                                text = "Caricamento anime in corso...",
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                query.isEmpty() && uiState.results.isEmpty() && uiState.allResultsCount == 0
                    && !uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cerca per titolo o sfoglia per genere / A-Z",
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )
                    }
                }

                uiState.results.isNotEmpty() -> {
                    val columns = gridColumnsForWidth(currentScreenWidthClass())
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.results) { anime ->
                            SearchResultCard(
                                anime = anime,
                                onClick = {
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", anime.id.toString())
                                    )
                                }
                            )
                        }

                        if (uiState.allResultsCount > uiState.displayLimit) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(
                                        onClick = { viewModel.showMore() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            text = "Mostra altri (${uiState.allResultsCount - uiState.displayLimit} rimanenti)",
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                query.isNotEmpty() && !uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val msg = if (uiState.selectedType != null || uiState.selectedStatus != null || uiState.selectedYear != null) {
                            "Nessun risultato per i filtri selezionati"
                        } else {
                            stringResource(R.string.search_no_results)
                        }
                        Text(msg, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    anime: Anime,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column {
            AsyncImage(
                model = anime.image,
                contentDescription = anime.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = anime.title,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}
