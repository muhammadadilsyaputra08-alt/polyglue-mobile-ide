package com.polyglue.ide.data.repository

import com.polyglue.ide.data.local.dao.SnippetDao
import com.polyglue.ide.data.local.entity.SnippetEntity
import kotlinx.coroutines.flow.Flow

class SnippetRepository(private val snippetDao: SnippetDao) {
    fun getAllSnippets(): Flow<List<SnippetEntity>> = snippetDao.getAllSnippets()
    fun getSnippetsByCategory(category: String): Flow<List<SnippetEntity>> = snippetDao.getSnippetsByCategory(category)
    fun getFavorites(): Flow<List<SnippetEntity>> = snippetDao.getFavoriteSnippets()
    fun searchSnippets(query: String): Flow<List<SnippetEntity>> = snippetDao.searchSnippets(query)

    suspend fun getSnippet(id: String): SnippetEntity? = snippetDao.getSnippetById(id)

    suspend fun insertSnippets(snippets: List<SnippetEntity>) {
        snippetDao.insertSnippets(snippets)
    }

    suspend fun updateSnippet(snippet: SnippetEntity) {
        snippetDao.updateSnippet(snippet)
    }

    suspend fun toggleFavorite(id: String) {
        val snippet = snippetDao.getSnippetById(id) ?: return
        snippetDao.setFavorite(id, !snippet.isFavorite)
    }

    suspend fun incrementUsage(id: String) {
        snippetDao.incrementUsage(id)
    }
}
