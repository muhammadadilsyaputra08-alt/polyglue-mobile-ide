package com.polyglue.ide.data.repository

import com.polyglue.ide.data.local.dao.ProjectDao
import com.polyglue.ide.data.local.dao.SceneDao
import com.polyglue.ide.data.local.entity.ProjectEntity
import com.polyglue.ide.data.local.entity.SceneEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val sceneDao: SceneDao
) {
    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProject(id: String): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun createProject(name: String, description: String = ""): ProjectEntity {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(id = projectId, name = name, description = description)
        projectDao.insertProject(project)

        val sceneId = UUID.randomUUID().toString()
        val defaultScene = SceneEntity(id = sceneId, projectId = projectId, name = "Level 1")
        sceneDao.insertScene(defaultScene)
        projectDao.updateActiveScene(projectId, sceneId)

        return project
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.deleteProject(project)
    }

    suspend fun duplicateProject(projectId: String, newName: String): ProjectEntity? {
        val original = projectDao.getProjectById(projectId) ?: return null
        val newId = UUID.randomUUID().toString()
        val copy = original.copy(id = newId, name = newName, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        projectDao.insertProject(copy)
        return copy
    }
}
