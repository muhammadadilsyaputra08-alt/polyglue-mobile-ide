package com.polyglue.ide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polyglue.ide.data.local.entity.SnippetEntity
import com.polyglue.ide.data.repository.SnippetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SnippetViewModel(private val repository: SnippetRepository) : ViewModel() {

    val allSnippets: StateFlow<List<SnippetEntity>> = repository.getAllSnippets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<SnippetEntity>> = _searchQuery
        .flatMapLatest { query -> if (query.isBlank()) flowOf(emptyList()) else repository.searchSnippets(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<SnippetEntity>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) { _searchQuery.value = query }

    fun toggleFavorite(snippetId: String) { viewModelScope.launch { repository.toggleFavorite(snippetId) } }

    fun useSnippet(snippetId: String) { viewModelScope.launch { repository.incrementUsage(snippetId) } }
}
