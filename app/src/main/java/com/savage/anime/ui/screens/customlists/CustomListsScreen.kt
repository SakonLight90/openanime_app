package com.savage.anime.ui.screens.customlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
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
import com.savage.anime.navigation.customListRoute
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomListsScreen(
    navController: NavController,
    viewModel: CustomListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var listName by rememberSaveable { mutableStateOf("") }
    var listPendingDelete by rememberSaveable { mutableStateOf<Int?>(null) }
    val pendingDeleteName = uiState.lists.firstOrNull { it.id == listPendingDelete }?.name.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_lists_title), color = Color.White, fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    listName = ""
                    viewModel.showCreateDialog()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.custom_lists_create_desc), tint = Color.White)
            }
        },
        containerColor = Color(0xFF141414)
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.lists.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.custom_lists_empty), color = Color(0xFF666666), fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.lists, key = { it.id }) { list ->
                    Card(
                        onClick = {
                            navController.navigate(customListRoute(list.id))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(list.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    stringResource(
                                        R.string.custom_lists_created_on,
                                        dateFormat.format(Date(list.createdAt))
                                    ),
                                    color = Color(0xFFB3B3B3),
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { listPendingDelete = list.id }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.custom_lists_delete_desc),
                                    tint = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideCreateDialog() },
                containerColor = Color(0xFF1A1A1A),
                title = { Text(stringResource(R.string.custom_lists_new), color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = listName,
                        onValueChange = { listName = it },
                        label = { Text(stringResource(R.string.custom_lists_name_hint), color = Color(0xFFB3B3B3)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.createList(listName.trim())
                            listName = ""
                        },
                        enabled = listName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.custom_lists_create), color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideCreateDialog() }) {
                        Text(stringResource(R.string.cancel), color = Color(0xFFB3B3B3))
                    }
                }
            )
        }

        if (listPendingDelete != null) {
            AlertDialog(
                onDismissRequest = { listPendingDelete = null },
                containerColor = Color(0xFF1A1A1A),
                title = { Text(stringResource(R.string.custom_lists_delete_title), color = Color.White) },
                text = { Text(stringResource(R.string.custom_lists_delete_message, pendingDeleteName), color = Color(0xFFB3B3B3)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteList(listPendingDelete!!)
                        listPendingDelete = null
                    }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listPendingDelete = null }) {
                        Text(stringResource(R.string.cancel), color = Color(0xFFB3B3B3))
                    }
                }
            )
        }
    }
}
