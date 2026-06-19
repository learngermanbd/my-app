package com.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamapp.data.api.LiveEvent
import com.streamapp.data.local.CachedChannel
import com.streamapp.data.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveUiState(
    val events: List<LiveEvent> = emptyList(),
    val channels: List<CachedChannel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LiveViewModel @Inject constructor(
    private val repository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    fun loadLiveData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val liveResult = repository.getLive()
                liveResult.onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            events = response.events,
                            isLoading = false
                        )
                    }
                }
                repository.getAllChannels().collect { channels ->
                    val liveChannels = channels.filter { ch -> ch.isLive == 1 }
                    _uiState.update { it.copy(channels = liveChannels, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}