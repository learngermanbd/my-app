package com.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamapp.data.local.CachedCategory
import com.streamapp.data.local.CachedChannel
import com.streamapp.data.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelsUiState(
    val categories: List<CachedCategory> = emptyList(),
    val channels: List<CachedChannel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val repository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChannelsUiState())
    val uiState: StateFlow<ChannelsUiState> = _uiState.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getCategories(forceRefresh = true).collect { categories ->
                    _uiState.update { it.copy(categories = categories, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun loadChannels(categoryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getChannels(categoryId, forceRefresh = true).collect { channels ->
                    _uiState.update { it.copy(channels = channels, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
