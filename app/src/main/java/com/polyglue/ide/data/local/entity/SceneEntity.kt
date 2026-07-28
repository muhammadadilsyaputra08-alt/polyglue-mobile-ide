package com.polyglue.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val width: Float = 1920f,
    val height: Float = 1080f,
    val backgroundColor: String = "#0D1117",
    val gravityX: Float = 0f,
    val gravityY: Float = -9.8f,
    val nodeGraphJson: String = "{}",
    val luaScript: String = "",
    val entitiesJson: String = "[]",
    val cameraJson: String = "{}",
    val tilemapJson: String = "{}",
    val lightingJson: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
