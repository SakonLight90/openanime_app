package com.savage.anime.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.navigation.Screen
import com.savage.anime.ui.utils.openExternalUrl

private const val CREATOR_URL = "https://t.me/SavageGhost90"

private val accentColors = listOf(
    0xFFE50914L to R.string.settings_color_red,
    0xFF46D369L to R.string.settings_color_green,
    0xFF2196F3L to R.string.settings_color_blue,
    0xFFFF9800L to R.string.settings_color_orange,
    0xFF9C27B0L to R.string.settings_color_purple,
    0xFFFFFFFFL to R.string.settings_color_white
)

@Composable
private fun NavCard(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    labelRes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(stringResource(labelRes), color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF666666))
        }
    }
}

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    val versionName = com.savage.anime.BuildConfig.VERSION_NAME

    LaunchedEffect(versionName) {
        viewModel.getCacheSize()
        viewModel.loadAutoPlayNext()
        viewModel.loadAccentColor()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_cache),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.settings_cache_size, uiState.cacheSize),
                        color = Color(0xFFB3B3B3),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.settings_cache_clear), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_auto_play),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(if (uiState.autoPlayNext) R.string.settings_auto_play_on else R.string.settings_auto_play_off),
                            color = Color(0xFFB3B3B3),
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = uiState.autoPlayNext,
                            onCheckedChange = { viewModel.toggleAutoPlayNext() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            NavCard(
                onClick = { navController.navigate(Screen.History.route) },
                icon = Icons.Default.History,
                labelRes = R.string.settings_history
            )

            NavCard(
                onClick = { navController.navigate(Screen.CustomLists.route) },
                icon = Icons.Default.Folder,
                labelRes = R.string.settings_custom_lists
            )

            NavCard(
                onClick = { navController.navigate(Screen.Seasonal.route) },
                icon = Icons.Default.GridView,
                labelRes = R.string.settings_seasonal
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_accent_color),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        accentColors.forEach { (color, labelRes) ->
                            val label = stringResource(labelRes)
                            val isSelected = uiState.selectedAccentColor == color
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color.toInt()))
                                    .clickable { viewModel.setAccentColor(color) }
                                    .semantics { contentDescription = label },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    val dotColor = if (color == 0xFFFFFFFFL) Color(0xFF1A1A1A) else Color.White
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val isWhiteAccent = uiState.selectedAccentColor == 0xFFFFFFFFL
                    val applyContentColor = if (isWhiteAccent) Color(0xFF1A1A1A) else Color.White
                    Button(
                        onClick = { viewModel.applyAccentColor() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(uiState.selectedAccentColor.toInt()),
                            contentColor = applyContentColor
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.settings_accent_apply), color = applyContentColor)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_version),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val branchLabel = when {
                        versionName.endsWith("a") -> stringResource(R.string.settings_branch_alpha)
                        versionName.endsWith("b") -> stringResource(R.string.settings_branch_beta)
                        versionName.endsWith("h") -> stringResource(R.string.settings_branch_hotfix)
                        else -> stringResource(R.string.settings_branch_stable)
                    }
                    Text(
                        text = versionName,
                        color = Color(0xFFB3B3B3),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "($branchLabel)",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_info),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.settings_creator) + ": ", color = Color(0xFFB3B3B3), fontSize = 14.sp)
                        Text(
                            text = uiState.creator,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { openExternalUrl(context, CREATOR_URL) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.settings_github) + ": ", color = Color(0xFFB3B3B3), fontSize = 14.sp)
                        Text(
                            text = uiState.githubUrl,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { openExternalUrl(context, uiState.githubUrl) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_copyright),
                color = Color(0xFF666666),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text(stringResource(R.string.settings_cache_clear_title), color = Color.White) },
            text = { Text(stringResource(R.string.settings_cache_clear_message), color = Color(0xFFB3B3B3)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCache()
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.settings_cache_clear), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFFB3B3B3))
                }
            }
        )
    }
}
