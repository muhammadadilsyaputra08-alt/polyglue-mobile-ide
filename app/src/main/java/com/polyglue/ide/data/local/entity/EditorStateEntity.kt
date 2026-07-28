package com.polyglue.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "editor_states")
data class EditorStateEntity(
    @PrimaryKey val projectId: String,
    val openSceneId: String = "",
    val cameraX: Float = 0f,
    val cameraY: Float = 0f,
    val cameraZoom: Float = 1f,
    val selectedEntityIds: List<String> = emptyList(),
    val panelLayoutJson: String = "{}",
    val undoStackJson: String = "[]",
    val redoStackJson: String = "[]",
    val snippetFavorites: List<String> = emptyList(),
    val lastOpenFiles: List<String> = emptyList(),
    val consoleFilter: String = "all",
    val theme: String = "dark",
    val gridSize: Float = 32f,
    val snapToGrid: Boolean = true,
    val showGizmos: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
