package com.polyglue.ide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.polyglue.ide.core.engine.NativeEngine
import com.polyglue.ide.core.lua.LuaRuntime
import com.polyglue.ide.data.local.entity.SceneEntity
import com.polyglue.ide.data.repository.SceneRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/** Satu node/entity ringan di Hierarchy panel, disimpan sebagai JSON array di `SceneEntity.entitiesJson`. */
data class SceneNode(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String = "Node",
    val x: Float = 0f,
    val y: Float = 0f
)

class EditorViewModel(
    private val engine: NativeEngine,
    private val sceneRepository: SceneRepository
) : ViewModel() {

    private val luaRuntime = LuaRuntime(engine)
    private val gson = Gson()
    private val nodeListType = object : TypeToken<List<SceneNode>>() {}.type

    private val _currentScene = MutableStateFlow<SceneEntity?>(null)
    val currentScene: StateFlow<SceneEntity?> = _currentScene.asStateFlow()

    val nodes: StateFlow<List<SceneNode>> = _currentScene
        .map { scene -> parseNodes(scene?.entitiesJson) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _consoleOutput = MutableStateFlow<List<String>>(emptyList())
    val consoleOutput: StateFlow<List<String>> = _consoleOutput.asStateFlow()

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId.asStateFlow()

    private fun parseNodes(json: String?): List<SceneNode> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return runCatching { gson.fromJson<List<SceneNode>>(json, nodeListType) }.getOrDefault(emptyList())
    }

    /** Cari & muat scene pertama milik project ini (§4/§14: satu-satunya cara UI Editor tahu scene mana yang aktif). */
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val scenes = sceneRepository.getScenesByProject(projectId).first()
            scenes.firstOrNull()?.let { loadScene(it.id) }
        }
    }

    fun loadScene(sceneId: String) {
        viewModelScope.launch {
            _currentScene.value = sceneRepository.getScene(sceneId)
            luaRuntime.initialize()
        }
    }

    fun togglePlay(): Boolean {
        _isPlaying.value = !_isPlaying.value
        if (_isPlaying.value) {
            _currentScene.value?.luaScript?.let { luaRuntime.executeScript(it) }
            log("▶ Play ditekan — preview dimulai")
        } else {
            log("■ Stop ditekan")
        }
        return _isPlaying.value
    }

    fun updateNodeGraph(graphJson: String) {
        viewModelScope.launch {
            _currentScene.value?.let { sceneRepository.updateNodeGraph(it.id, graphJson) }
        }
    }

    fun updateScript(script: String) {
        val scene = _currentScene.value ?: return
        _currentScene.value = scene.copy(luaScript = script)
        viewModelScope.launch {
            sceneRepository.updateScript(scene.id, script)
            luaRuntime.executeScript(script)
        }
    }

    fun selectNode(nodeId: String?) { _selectedNodeId.value = nodeId }

    fun addNode(name: String, type: String = "Node") {
        val scene = _currentScene.value ?: return
        val updated = parseNodes(scene.entitiesJson) + SceneNode(name = name, type = type)
        persistNodes(scene, updated)
    }

    fun deleteNode(nodeId: String) {
        val scene = _currentScene.value ?: return
        val updated = parseNodes(scene.entitiesJson).filterNot { it.id == nodeId }
        if (_selectedNodeId.value == nodeId) _selectedNodeId.value = null
        persistNodes(scene, updated)
    }

    fun updateNodeTransform(nodeId: String, x: Float, y: Float) {
        val scene = _currentScene.value ?: return
        val updated = parseNodes(scene.entitiesJson).map {
            if (it.id == nodeId) it.copy(x = x, y = y) else it
        }
        persistNodes(scene, updated)
    }

    private fun persistNodes(scene: SceneEntity, updated: List<SceneNode>) {
        val json = gson.toJson(updated)
        _currentScene.value = scene.copy(entitiesJson = json)
        viewModelScope.launch { sceneRepository.updateEntities(scene.id, json) }
    }

    fun onTouch(action: Int, x: Float, y: Float) {
        if (_isPlaying.value) {
            engine.nativeTouchEvent(action, x, y, 0)
            // NOTE: LuaRuntime.callFunction() hanya bisa menemukan fungsi yang di-load lewat
            // loadScript(path) dari file; script Editor disimpan di Room (bukan file), jadi
            // panggilan ini masih no-op sampai LuaRuntime punya cara load script dari string.
            // Bukan regresi dari sesi ini — keterbatasan lama LuaRuntime, dicatat di HANDOFF §14.
            luaRuntime.callFunction("", "onTouch", action, x, y, 0)
        }
    }

    fun log(message: String) {
        _consoleOutput.value = _consoleOutput.value + message
    }

    override fun onCleared() {
        luaRuntime.shutdown()
        super.onCleared()
    }
}
