package com.helios.redshark.ui.myideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.NetworkChecker
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.usecase.idea.GetMyIdeasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MyIdeasUiState(
    val allIdeas: List<Idea> = emptyList(),
    val activeTagFilter: UUID? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val displayedIdeas: List<Idea>
        get() = if (activeTagFilter == null) allIdeas
                else allIdeas.filter { it.tagIds.contains(activeTagFilter) }
}

@HiltViewModel
class MyIdeasViewModel @Inject constructor(
    private val getMyIdeasUseCase: GetMyIdeasUseCase,
    private val networkChecker: NetworkChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyIdeasUiState())
    val uiState: StateFlow<MyIdeasUiState> = _uiState.asStateFlow()

    init { observe() }

    private fun observe() {
        // TC-C22: detect offline before subscribing to avoid silent empty state
        if (!networkChecker.isOnline()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Không có kết nối. Kiểm tra mạng và nhấn Thử lại.") }
            return
        }
        viewModelScope.launch {
            getMyIdeasUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải danh sách.") }
                }
                .collect { ideas ->
                    _uiState.update { it.copy(allIdeas = ideas, isLoading = false, errorMessage = null) }
                }
        }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observe()
    }

    fun filterByTag(tagId: UUID?) {
        _uiState.update { it.copy(activeTagFilter = tagId) }
    }
}
