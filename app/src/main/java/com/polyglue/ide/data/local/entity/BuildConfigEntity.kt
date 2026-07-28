package com.polyglue.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "build_configs")
data class BuildConfigEntity(
    @PrimaryKey val projectId: String,
    val minSdk: Int = 24,
    val targetSdk: Int = 34,
    val abiFilters: List<String> = listOf("arm64-v8a", "armeabi-v7a"),
    val isDebug: Boolean = false,
    val enableProguard: Boolean = true,
    // Orientasi target game hasil export. Nilai valid: "landscape", "portrait",
    // "sensorLandscape" (auto-flip kiri/kanan), "sensorPortrait" (auto-flip atas/bawah).
    // Default "landscape" karena mayoritas game 2D dikembangkan landscape.
    val targetOrientation: String = "landscape",
    val signingConfigJson: String = "{}",
    val dependenciesJson: String = "[]",
    val customGradle: String = "",
    val lastBuildPath: String = "",
    val lastBuildTime: Long = 0,
    val buildCount: Int = 0
)
