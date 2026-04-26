package com.helios.redshark.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.usecase.issue.GetHomeFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val issues: List<Issue> = emptyList(),
    val isLoading: Boolean = false,
    /** TC-C22: network failure details; null = no error. */
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeFeedUseCase: GetHomeFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeFeed()
    }

    private fun observeFeed() {
        viewModelScope.launch {
            getHomeFeedUseCase()
                .catch { e ->
                    // TC-C22: capture network/remote error and expose Retry surface
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải dữ liệu.")
                    }
                }
                .collect { issues ->
                    _uiState.update { it.copy(issues = issues, isLoading = false, errorMessage = null) }
                }
        }
    }

    /** TC-C22: re-subscribe after network failure. */
    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeFeed()
    }
}
