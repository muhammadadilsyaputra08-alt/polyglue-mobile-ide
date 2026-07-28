package com.polyglue.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sceneId: String,
    val name: String,
    val language: String = "lua",
    val content: String = "",
    val isGenerated: Boolean = false,
    val sourceSnippetIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
