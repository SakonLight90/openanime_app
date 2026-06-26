package com.savage.anime.ui.screens.customlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.savage.anime.navigation.Screen
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomListDetailScreen(
    navController: NavController,
    listId: Int,
    viewModel: CustomListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141414))
            )
        },
        containerColor = Color(0xFF141414)
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.animeIds.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Lista vuota", color = Color(0xFF666666), fontSize = 14.sp)
            }
        } else {
            val columns = gridColumnsForWidth(currentScreenWidthClass())
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.animeIds, key = { it }) { animeId ->
                    Card(
                        onClick = {
                            navController.navigate(Screen.Detail.route.replace("{animeId}", animeId.toString()))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Box {
                            AsyncImage(
                                model = "https://img.animeworld.so/img/default.jpg",
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                if (uiState.otherLists.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.showMoveDialog(animeId) }
                                    ) {
                                        Text("→", color = Color.White, fontSize = 16.sp)
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.removeItem(listId, animeId) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Rimuovi", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showMoveDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideMoveDialog() },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Sposta in...", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.otherLists.forEach { list ->
                            TextButton(
                                onClick = { viewModel.moveItem(list.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(list.name, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.hideMoveDialog() }) {
                        Text("Annulla", color = Color(0xFFB3B3B3))
                    }
                }
            )
        }
    }
}
