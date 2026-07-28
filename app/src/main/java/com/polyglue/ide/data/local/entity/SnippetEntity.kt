package com.polyglue.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey val id: String,
    val category: String,
    val name: String,
    val description: String,
    val icon: String = "",
    val visualJson: String = "{}",
    val luaTemplate: String = "",
    val cppBackend: String = "",
    val paramsJson: String = "{}",
    val previewData: String = "",
    val isBuiltIn: Boolean = true,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0
)
