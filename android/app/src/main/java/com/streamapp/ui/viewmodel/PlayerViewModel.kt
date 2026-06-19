package com.streamapp.ui.viewmodel

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamapp.data.api.StreamLink
import com.streamapp.data.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class PlayerUiState(
    val channelName: String = "",
    val links: List<StreamLink> = emptyList(),
    val activeUrl: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isPlaying: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: StreamRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // Session tracking
    private var sessionId: String? = null
    private var heartbeatJob: Job? = null
    private var currentChannelId: Int = -1

    fun loadStream(channelId: Int) {
        currentChannelId = channelId
        // End previous session if any
        endAnalyticsSession()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = repository.getStreamLinks(channelId)
                result.onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            channelName = response.channel.name,
                            links = response.links,
                            activeUrl = response.links.firstOrNull { l -> l.isHealthy == 1 }?.url
                                ?: response.links.firstOrNull()?.url,
                            isLoading = false
                        )
                    }
                    // Start analytics session after stream loads successfully
                    startAnalyticsSession(channelId, response.channel.name)
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun selectLink(url: String) {
        _uiState.update { it.copy(activeUrl = url, isPlaying = false, error = null) }
    }

    fun onPlaying() {
        _uiState.update { it.copy(isPlaying = true) }
    }

    fun togglePlay() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun onError(message: String) {
        // Try next link
        val currentUrl = _uiState.value.activeUrl
        val remaining = _uiState.value.links.filter { it.url != currentUrl && it.isHealthy == 1 }
        if (remaining.isNotEmpty()) {
            selectLink(remaining.first().url)
        } else {
            val anyRemaining = _uiState.value.links.filter { it.url != currentUrl }
            if (anyRemaining.isNotEmpty()) {
                selectLink(anyRemaining.first().url)
            } else {
                _uiState.update { it.copy(error = "No working streams available") }
            }
        }
    }

    // === Analytics Session Tracking ===

    private fun startAnalyticsSession(channelId: Int, channelName: String) {
        viewModelScope.launch {
            val deviceId = try {
                Settings.Secure.getString(
                    application.contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown_android"
            } catch (_: Exception) { "unknown_android" }

            sessionId = repository.startSession(
                deviceId = deviceId,
                deviceType = "android",
                channelId = channelId,
                channelName = channelName
            )

            // Start periodic heartbeat (every 60 seconds)
            val sid = sessionId ?: return@launch
            heartbeatJob = viewModelScope.launch {
                while (isActive) {
                    delay(60_000)
                    repository.heartbeatSession(sid)
                }
            }
        }
    }

    private fun endAnalyticsSession() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        val sid = sessionId ?: return
        sessionId = null
        viewModelScope.launch {
            repository.endSession(sid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        endAnalyticsSession()
    }
}
