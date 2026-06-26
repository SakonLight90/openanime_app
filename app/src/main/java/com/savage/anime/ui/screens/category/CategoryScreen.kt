package com.savage.anime.ui.screens.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.savage.anime.domain.models.Anime
import com.savage.anime.navigation.Screen
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidth

@Composable
fun CategoryScreen(
    navController: NavController,
    category: String,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(category) {
        viewModel.loadCategory(category)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        Column {
            Text(
                text = category,
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.error ?: "", color = Color.White, fontSize = 16.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadCategory(category) }) {
                                Text("Riprova")
                            }
                        }
                    }
                }

                state.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun risultato", color = Color.Gray)
                    }
                }

                else -> {
                    val columns = gridColumnsForWidth(currentScreenWidthClass())
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.items) { anime ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                onClick = {
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", anime.id.toString())
                                    )
                                }
                            ) {
                                Column {
                                    AsyncImage(
                                        model = anime.image,
                                        contentDescription = anime.title,
                                        modifier = Modifier.fillMaxWidth().height(160.dp),
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
                        if (state.allItems.size > state.displayLimit) {
                            item {
                                Button(
                                    onClick = { viewModel.showMore() },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Mostra altri (${state.allItems.size - state.displayLimit} rimanenti)", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}