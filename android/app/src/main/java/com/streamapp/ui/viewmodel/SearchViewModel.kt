package com.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamapp.data.api.Channel
import com.streamapp.data.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Channel> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            if (query.isBlank()) {
                _uiState.update { it.copy(results = emptyList(), hasSearched = false, error = null) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, hasSearched = true, error = null) }
            try {
                val result = repository.search(query)
                result.onSuccess { channels ->
                    _uiState.update { it.copy(results = channels) }
                }.onFailure { e ->
                    _uiState.update { it.copy(results = emptyList(), error = e.message ?: "Search failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(results = emptyList(), error = e.message ?: "Network error") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
