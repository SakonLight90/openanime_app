package com.savage.anime.ui.screens.azlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.domain.models.Anime
import com.savage.anime.navigation.Screen
import com.savage.anime.navigation.detailRoute
import com.savage.anime.ui.components.AnimeGrid
import com.savage.anime.ui.components.AnimeGridEmpty
import com.savage.anime.ui.components.AnimeGridError
import com.savage.anime.ui.components.AnimeGridLoading

@Composable
fun AzListScreen(
    navController: NavController,
    initialLetter: String? = null,
    modifier: Modifier = Modifier,
    viewModel: AzListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val alphabet = remember { viewModel.getAlphabet() }

    LaunchedEffect(initialLetter) {
        if (!initialLetter.isNullOrBlank() && initialLetter != state.selectedLetter) {
            viewModel.loadLetter(initialLetter.uppercase())
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(alphabet, key = { it }) { letter ->
                    val isSelected = letter == state.selectedLetter
                    Card(
                        modifier = Modifier
                            .width(if (letter.length > 1) 56.dp else 36.dp)
                            .clickable { viewModel.loadLetter(letter) },
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF222222)
                        )
                    ) {
                        Text(
                            text = letter,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> AnimeGridLoading()

                state.error != null -> AnimeGridError(
                    message = state.error ?: "",
                    onRetry = { viewModel.loadLetter(state.selectedLetter) }
                )

                state.results.isEmpty() -> AnimeGridEmpty(
                    message = stringResource(R.string.az_list_no_results, state.selectedLetter)
                )

                else -> AnimeGrid(
                    items = state.results,
                    onItemClick = { anime: Anime ->
                        navController.navigate(
                            detailRoute(anime.id)
                        )
                    }
                )
            }
        }
    }
}
