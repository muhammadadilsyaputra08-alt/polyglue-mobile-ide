package com.polyglue.ide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polyglue.ide.data.local.entity.BuildConfigEntity
import com.polyglue.ide.data.repository.BuildRepository
import com.polyglue.ide.data.repository.ProjectRepository
import com.polyglue.ide.data.repository.SceneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BuildViewModel(
    private val buildRepository: BuildRepository,
    private val projectRepository: ProjectRepository,
    private val sceneRepository: SceneRepository
) : ViewModel() {

    val buildState: StateFlow<BuildRepository.BuildState> = buildRepository.buildState

    private val _buildConfig = MutableStateFlow<BuildConfigEntity?>(null)
    val buildConfig: StateFlow<BuildConfigEntity?> = _buildConfig.asStateFlow()

    /** Muat (atau buat default) `BuildConfigEntity` project ini — sumber kebenaran tunggal
     * untuk `targetOrientation`, dipakai bareng oleh BuildScreen & EditorScreen (HANDOFF.md §5.2). */
    fun loadConfig(projectId: String) {
        viewModelScope.launch {
            _buildConfig.value = buildRepository.getBuildConfig(projectId) ?: BuildConfigEntity(projectId = projectId)
        }
    }

    fun setOrientation(orientation: String) {
        val current = _buildConfig.value ?: return
        val updated = current.copy(targetOrientation = orientation)
        _buildConfig.value = updated
        viewModelScope.launch { buildRepository.saveBuildConfig(updated) }
    }

    fun setPackageName(packageName: String) {
        val current = _buildConfig.value ?: return
        _buildConfig.value = current // package name disimpan di ProjectEntity, bukan BuildConfigEntity
        viewModelScope.launch {
            val project = projectRepository.getProject(current.projectId) ?: return@launch
            projectRepository.updateProject(project.copy(targetPackage = packageName))
        }
    }

    fun setVersionName(versionName: String) {
        val current = _buildConfig.value ?: return
        viewModelScope.launch {
            val project = projectRepository.getProject(current.projectId) ?: return@launch
            projectRepository.updateProject(project.copy(versionName = versionName))
        }
    }

    fun setEnableProguard(enabled: Boolean) {
        val current = _buildConfig.value ?: return
        val updated = current.copy(enableProguard = enabled)
        _buildConfig.value = updated
        viewModelScope.launch { buildRepository.saveBuildConfig(updated) }
    }

    fun buildAPK(project: com.polyglue.ide.data.local.entity.ProjectEntity, scene: com.polyglue.ide.data.local.entity.SceneEntity) {
        viewModelScope.launch { buildRepository.buildAPK(project, scene) }
    }

    /** Ambil project + scene pertamanya lalu build — dipanggil langsung dari BuildScreen. */
    fun build(projectId: String) {
        viewModelScope.launch {
            val project = projectRepository.getProject(projectId) ?: run {
                return@launch
            }
            val scene = sceneRepository.getScenesByProject(projectId).first().firstOrNull() ?: return@launch
            buildRepository.buildAPK(project, scene)
        }
    }

    fun resetState() { buildRepository.resetState() }
}
