package com.savage.anime.ui.screens.versioncheck

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.BuildConfig
import com.savage.anime.data.network.api.AnimeApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VersionCheckState(
    val checking: Boolean = true,
    val blocked: Boolean = false,
    val updateUrl: String = "",
    val currentVersion: String = ""
)

@HiltViewModel
class VersionCheckViewModel @Inject constructor(
    private val application: Application,
    private val animeApi: AnimeApi
) : ViewModel() {

    private val _state = MutableStateFlow(VersionCheckState())
    val state: StateFlow<VersionCheckState> = _state.asStateFlow()

    init {
        checkVersion()
    }

    fun checkVersion() {
        _state.value = VersionCheckState(checking = true)
        viewModelScope.launch {
            try {
                val response = animeApi.getAppVersion()
                val appVersion = BuildConfig.VERSION_NAME
                if (compareVersions(appVersion, response.minCompatibleVersion) < 0) {
                    _state.value = VersionCheckState(
                        checking = false,
                        blocked = true,
                        updateUrl = response.updateUrl,
                        currentVersion = response.currentVersion
                    )
                } else {
                    _state.value = VersionCheckState(checking = false)
                }
            } catch (_: Exception) {
                _state.value = VersionCheckState(checking = false)
            }
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val base1 = v1.replace(Regex("[abh]$"), "")
        val base2 = v2.replace(Regex("[abh]$"), "")
        val parts1 = base1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = base2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 < p2) return -1
            if (p1 > p2) return 1
        }
        return 0
    }
}
