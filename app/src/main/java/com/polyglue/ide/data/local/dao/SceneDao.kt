package com.polyglue.ide.data.local.dao

import androidx.room.*
import com.polyglue.ide.data.local.entity.SceneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneDao {
    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY createdAt")
    fun getScenesByProject(projectId: String): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes WHERE id = :id")
    suspend fun getSceneById(id: String): SceneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScene(scene: SceneEntity)

    @Update
    suspend fun updateScene(scene: SceneEntity)

    @Delete
    suspend fun deleteScene(scene: SceneEntity)

    @Query("UPDATE scenes SET luaScript = :script, updatedAt = :time WHERE id = :sceneId")
    suspend fun updateScript(sceneId: String, script: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE scenes SET nodeGraphJson = :graph, updatedAt = :time WHERE id = :sceneId")
    suspend fun updateNodeGraph(sceneId: String, graph: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE scenes SET luaScript = luaScript || :snippet, updatedAt = :time WHERE id = :sceneId")
    suspend fun appendScript(sceneId: String, snippet: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE scenes SET entitiesJson = :entitiesJson, updatedAt = :time WHERE id = :sceneId")
    suspend fun updateEntities(sceneId: String, entitiesJson: String, time: Long = System.currentTimeMillis())
}
