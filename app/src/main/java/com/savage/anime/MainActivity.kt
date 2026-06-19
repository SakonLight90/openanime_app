package com.savage.anime

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.savage.anime.domain.repository.LocalUserDataRepository
import com.savage.anime.navigation.NavGraph
import com.savage.anime.ui.screens.player.PiPState
import com.savage.anime.ui.screens.versioncheck.UpdateRequiredScreen
import com.savage.anime.ui.screens.versioncheck.VersionCheckViewModel
import com.savage.anime.ui.theme.AnimeAppTheme
import com.savage.anime.ui.theme.RedNetflix
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var localUserDataRepository: LocalUserDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            val versionState by hiltViewModel<VersionCheckViewModel>().state.collectAsState()
            val savedColor by localUserDataRepository.getAccentColorFlow().collectAsState(initial = null)
            val accentColor = savedColor?.let { Color(it.toInt()) } ?: RedNetflix

            AnimeAppTheme(accentColor = accentColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (versionState.blocked) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF141414))
                        ) {
                            UpdateRequiredScreen(versionState)
                        }
                    } else if (!versionState.checking) {
                        NavGraph()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (PiPState.isPlayerActive) {
            PiPState.enteringPiP = true
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }
}
