package com.polyglue.ide.data.repository

import com.polyglue.ide.data.local.dao.SceneDao
import com.polyglue.ide.data.local.entity.SceneEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SceneRepository(private val sceneDao: SceneDao) {
    fun getScenesByProject(projectId: String): Flow<List<SceneEntity>> = sceneDao.getScenesByProject(projectId)

    suspend fun getScene(id: String): SceneEntity? = sceneDao.getSceneById(id)

    suspend fun createScene(projectId: String, name: String): SceneEntity {
        val scene = SceneEntity(id = UUID.randomUUID().toString(), projectId = projectId, name = name)
        sceneDao.insertScene(scene)
        return scene
    }

    suspend fun updateScene(scene: SceneEntity) {
        sceneDao.updateScene(scene.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateScript(sceneId: String, script: String) {
        sceneDao.updateScript(sceneId, script)
    }

    suspend fun updateNodeGraph(sceneId: String, graph: String) {
        sceneDao.updateNodeGraph(sceneId, graph)
    }

    suspend fun appendScript(sceneId: String, snippetLua: String) {
        sceneDao.appendScript(sceneId, "\n\n" + snippetLua)
    }

    suspend fun updateEntities(sceneId: String, entitiesJson: String) {
        sceneDao.updateEntities(sceneId, entitiesJson)
    }

    suspend fun deleteScene(scene: SceneEntity) {
        sceneDao.deleteScene(scene)
    }
}
