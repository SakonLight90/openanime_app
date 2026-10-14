package com.savage.anime.ui.screens.versioncheck

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savage.anime.R
import com.savage.anime.ui.utils.openExternalUrl

private const val REPO_URL = "https://github.com/sakonlight90/openanime_app"

@Composable
fun UpdateRequiredScreen(state: VersionCheckState) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.checking) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.update_required_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (state.currentVersion.isNotEmpty()) {
                        stringResource(R.string.update_required_message, state.currentVersion)
                    } else {
                        stringResource(R.string.update_required_message_no_version)
                    },
                    color = Color(0xFFB3B3B3),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                if (!state.updateUrl.isNullOrBlank()) {
                    Button(
                        onClick = { openExternalUrl(context, state.updateUrl) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.update_download),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    text = stringResource(R.string.update_github, REPO_URL),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { openExternalUrl(context, REPO_URL) }
                )
            }
        }
    }
}
