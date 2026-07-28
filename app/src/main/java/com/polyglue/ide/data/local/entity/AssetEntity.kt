package com.polyglue.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val type: String,
    val path: String,
    val relativePath: String,
    val size: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val format: String = "",
    val tags: List<String> = emptyList(),
    val isPacked: Boolean = false,
    val atlasId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
