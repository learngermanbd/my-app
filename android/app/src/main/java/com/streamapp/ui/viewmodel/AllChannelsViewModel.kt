package com.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamapp.data.local.CachedChannel
import com.streamapp.data.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllChannelsUiState(
    val channels: List<CachedChannel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AllChannelsViewModel @Inject constructor(
    private val repository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllChannelsUiState())
    val uiState: StateFlow<AllChannelsUiState> = _uiState.asStateFlow()

    init {
        loadAllChannels()
    }

    fun loadAllChannels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getAllChannels(forceRefresh = true).collect { channels ->
                    _uiState.update { it.copy(channels = channels, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
