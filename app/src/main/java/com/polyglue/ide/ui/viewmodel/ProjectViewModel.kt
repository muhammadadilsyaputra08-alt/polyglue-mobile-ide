package com.polyglue.ide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polyglue.ide.data.local.entity.ProjectEntity
import com.polyglue.ide.data.repository.ProjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectViewModel(private val repository: ProjectRepository) : ViewModel() {

    val projects: StateFlow<List<ProjectEntity>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

    fun createProject(name: String, description: String = "") {
        viewModelScope.launch { repository.createProject(name, description) }
    }

    fun selectProject(project: ProjectEntity) { _selectedProject.value = project }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { repository.deleteProject(project) }
    }

    fun duplicateProject(projectId: String, newName: String) {
        viewModelScope.launch { repository.duplicateProject(projectId, newName) }
    }
}
