package com.polyglue.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val targetPackage: String = "com.polyglue.game",
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val orientation: String = "landscape",
    val isLandscape: Boolean = true,
    val iconPath: String = "",
    val splashPath: String = "",
    val activeSceneId: String = "",
    val settingsJson: String = "{}"
)
