package com.savage.anime.ui.screens.customlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.navigation.detailRoute
import com.savage.anime.ui.components.AnimeGridCard
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
    var animePendingRemove by rememberSaveable { mutableStateOf<Int?>(null) }
    val pendingRemoveTitle = uiState.items.firstOrNull { it.id == animePendingRemove }?.title.orEmpty()

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.listName.ifEmpty { stringResource(R.string.custom_list_title_fallback) },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
                            tint = Color.White
                        )
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
        } else if (uiState.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.custom_list_empty), color = Color(0xFF666666), fontSize = 14.sp)
            }
        } else {
            val columns = gridColumnsForWidth(currentScreenWidthClass())
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items, key = { it.id }) { anime ->
                    Column {
                        AnimeGridCard(
                            anime = anime,
                            onClick = { navController.navigate(detailRoute(anime.id)) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (uiState.otherLists.isNotEmpty()) {
                                TextButton(onClick = { viewModel.showMoveDialog(anime.id) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = stringResource(R.string.custom_list_move_desc),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        stringResource(R.string.custom_list_move),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            TextButton(onClick = { animePendingRemove = anime.id }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.remove),
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.remove),
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp
                                )
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
                title = { Text(stringResource(R.string.custom_list_move), color = Color.White) },
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
                        Text(stringResource(R.string.cancel), color = Color(0xFFB3B3B3))
                    }
                }
            )
        }

        if (animePendingRemove != null) {
            AlertDialog(
                onDismissRequest = { animePendingRemove = null },
                containerColor = Color(0xFF1A1A1A),
                title = { Text(stringResource(R.string.custom_list_remove_title), color = Color.White) },
                text = { Text(stringResource(R.string.custom_list_remove_message, pendingRemoveTitle), color = Color(0xFFB3B3B3)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeItem(listId, animePendingRemove!!)
                        animePendingRemove = null
                    }) {
                        Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { animePendingRemove = null }) {
                        Text(stringResource(R.string.cancel), color = Color(0xFFB3B3B3))
                    }
                }
            )
        }
    }
}
