package com.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamapp.data.api.SportsMatch
import com.streamapp.data.local.CachedCategory
import com.streamapp.data.local.CachedChannel
import com.streamapp.data.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val categories: List<CachedCategory> = emptyList(),
    val selectedCategoryId: Int? = null,
    val popularChannels: List<CachedChannel> = emptyList(),
    val liveChannels: List<CachedChannel> = emptyList(),
    val allChannels: List<CachedChannel> = emptyList(),
    val matches: List<SportsMatch> = emptyList(),
    val selectedTab: Int = 0, // 0=Recent, 1=Live, 2=Upcoming
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun selectCategory(categoryId: Int?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                launch {
                    repository.getCategories(forceRefresh = true).collect { categories ->
                        _uiState.update { it.copy(categories = categories) }
                    }
                }
                launch {
                    repository.getAllChannels(forceRefresh = true).collect { channels ->
                        _uiState.update {
                            it.copy(
                                allChannels = channels,
                                popularChannels = channels.filter { ch -> ch.featured == 1 },
                                liveChannels = channels.filter { ch -> ch.isLive == 1 },
                                isLoading = false
                            )
                        }
                    }
                }
                launch {
                    val result = repository.getMatches()
                    result.onSuccess { matchesResponse ->
                        val allMatches = matchesResponse.live + matchesResponse.upcoming + matchesResponse.recent
                        _uiState.update { it.copy(matches = allMatches) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
