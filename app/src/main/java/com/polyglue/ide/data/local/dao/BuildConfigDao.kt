package com.polyglue.ide.data.local.dao

import androidx.room.*
import com.polyglue.ide.data.local.entity.BuildConfigEntity

@Dao
interface BuildConfigDao {
    @Query("SELECT * FROM build_configs WHERE projectId = :projectId")
    suspend fun getBuildConfig(projectId: String): BuildConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildConfig(config: BuildConfigEntity)

    @Update
    suspend fun updateBuildConfig(config: BuildConfigEntity)

    @Query("UPDATE build_configs SET lastBuildPath = :path, lastBuildTime = :time, buildCount = buildCount + 1 WHERE projectId = :projectId")
    suspend fun updateLastBuild(projectId: String, path: String, time: Long = System.currentTimeMillis())
}
