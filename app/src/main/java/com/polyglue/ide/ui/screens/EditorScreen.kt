package com.polyglue.ide.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.polyglue.ide.PolyGlueApplication
import com.polyglue.ide.editor.GamePreviewActivity
import com.polyglue.ide.ui.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(navController: NavController, projectId: String) {
    val context = LocalContext.current
    val app = context.applicationContext as PolyGlueApplication
    val editorViewModel: EditorViewModel = viewModel(factory = app.viewModelFactory)

    var selectedTab by remember { mutableStateOf(0) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var targetOrientation by remember { mutableStateOf("landscape") }
    var newNodeDialog by remember { mutableStateOf(false) }
    var newNodeName by remember { mutableStateOf("") }

    val scene by editorViewModel.currentScene.collectAsState()
    val nodes by editorViewModel.nodes.collectAsState()
    val selectedNodeId by editorViewModel.selectedNodeId.collectAsState()
    val isPlaying by editorViewModel.isPlaying.collectAsState()
    val consoleOutput by editorViewModel.consoleOutput.collectAsState()
    val selectedNode = nodes.firstOrNull { it.id == selectedNodeId }

    // Muat scene pertama milik project ini begitu screen dibuka (§14: sebelumnya
    // EditorScreen tidak punya konsep "scene aktif" sama sekali).
    LaunchedEffect(projectId) {
        editorViewModel.loadProject(projectId)
        val config = app.appModule.buildRepository.getBuildConfig(projectId)
        targetOrientation = config?.targetOrientation ?: "landscape"
    }

    var scriptText by remember(scene?.id) { mutableStateOf(scene?.luaScript ?: "") }
    LaunchedEffect(scene?.id) { scriptText = scene?.luaScript ?: "" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(scene?.name ?: "Scene Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("snippets/${scene?.id ?: "none"}") }) {
                        Icon(Icons.Default.Extension, contentDescription = "Snippets")
                    }
                    IconButton(onClick = {
                        val nowPlaying = editorViewModel.togglePlay()
                        if (nowPlaying) {
                            context.startActivity(
                                GamePreviewActivity.createIntent(context, projectId, targetOrientation)
                            )
                        }
                    }) {
                        Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Stop" else "Play")
                    }
                    IconButton(onClick = { navController.navigate("build/$projectId") }) {
                        Icon(Icons.Default.Build, contentDescription = "Build")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Default.GridView, null) }, label = { Text("Scene") }, selected = selectedTab == 0, onClick = { selectedTab = 0 })
                NavigationBarItem(icon = { Icon(Icons.Default.AccountTree, null) }, label = { Text("Nodes") }, selected = selectedTab == 1, onClick = { selectedTab = 1 })
                NavigationBarItem(icon = { Icon(Icons.Default.Code, null) }, label = { Text("Code") }, selected = selectedTab == 2, onClick = { selectedTab = 2 })
                NavigationBarItem(icon = { Icon(Icons.Default.Terminal, null) }, label = { Text("Console") }, selected = selectedTab == 3, onClick = { selectedTab = 3 })
            }
        }
    ) { padding ->
        if (scene == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        when (selectedTab) {
            2 -> {
                // Tab Code: text editor beneran, terikat ke SceneEntity.luaScript via EditorViewModel.
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    OutlinedTextField(
                        value = scriptText,
                        onValueChange = {
                            scriptText = it
                            editorViewModel.updateScript(it)
                        },
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        label = { Text("main.lua") }
                    )
                }
            }
            3 -> {
                // Tab Console: log dari EditorViewModel (Play/Stop, error Lua, dll).
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp)) {
                    if (consoleOutput.isEmpty()) {
                        item { Text("Console kosong. Tekan Play untuk mulai.", style = MaterialTheme.typography.bodySmall) }
                    }
                    items(consoleOutput) { line ->
                        Text(line, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            1 -> {
                // Tab Nodes: hierarchy penuh dari SceneEntity.entitiesJson, bukan lagi teks statis.
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hierarchy (${nodes.size})", style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { newNodeDialog = true }) { Icon(Icons.Default.Add, "Tambah Node") }
                    }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(nodes, key = { it.id }) { node ->
                            ListItem(
                                headlineContent = { Text(node.name) },
                                supportingContent = { Text(node.type) },
                                leadingContent = { Icon(Icons.Default.Widgets, null) },
                                trailingContent = {
                                    IconButton(onClick = { editorViewModel.deleteNode(node.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                                    }
                                },
                                modifier = Modifier.clickable { editorViewModel.selectNode(node.id) }
                            )
                        }
                    }
                }
            }
            else -> {
                Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Hierarchy Panel (ringkas, versi lengkap ada di tab Nodes)
                    Card(modifier = Modifier.width(200.dp).fillMaxHeight().padding(4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Hierarchy", style = MaterialTheme.typography.titleSmall)
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            if (nodes.isEmpty()) {
                                Text("(kosong — tambah lewat tab Nodes)", style = MaterialTheme.typography.bodySmall)
                            }
                            nodes.forEach { node ->
                                Text(
                                    node.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.clickable { editorViewModel.selectNode(node.id) }
                                )
                            }
                        }
                    }

                    // Canvas
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .background(Color(0xFF0D1117))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale *= zoom
                                    offset += pan
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(Color(0xFF0D1117))
                            val gridSize = 32f
                            for (x in 0..size.width.toInt() step gridSize.toInt()) {
                                drawLine(color = Color(0xFF21262D), start = Offset(x.toFloat() + offset.x % gridSize, 0f), end = Offset(x.toFloat() + offset.x % gridSize, size.height))
                            }
                            for (y in 0..size.height.toInt() step gridSize.toInt()) {
                                drawLine(color = Color(0xFF21262D), start = Offset(0f, y.toFloat() + offset.y % gridSize), end = Offset(size.width, y.toFloat() + offset.y % gridSize))
                            }
                        }
                        if (isPlaying) {
                            Surface(color = Color.Black.copy(alpha = 0.7f), modifier = Modifier.fillMaxSize()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("LIVE PREVIEW", color = Color.Green, style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                        }
                    }

                    // Inspector Panel — terikat ke node terpilih sungguhan
                    Card(modifier = Modifier.width(250.dp).fillMaxHeight().padding(4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Inspector", style = MaterialTheme.typography.titleSmall)
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            if (selectedNode == null) {
                                Text("Pilih node di Hierarchy untuk edit transform.", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text(selectedNode.name, style = MaterialTheme.typography.titleSmall)
                                Text("Transform", style = MaterialTheme.typography.titleSmall)
                                OutlinedTextField(
                                    value = selectedNode.x.toString(),
                                    onValueChange = { v ->
                                        v.toFloatOrNull()?.let { editorViewModel.updateNodeTransform(selectedNode.id, it, selectedNode.y) }
                                    },
                                    label = { Text("X") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = selectedNode.y.toString(),
                                    onValueChange = { v ->
                                        v.toFloatOrNull()?.let { editorViewModel.updateNodeTransform(selectedNode.id, selectedNode.x, it) }
                                    },
                                    label = { Text("Y") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (newNodeDialog) {
        AlertDialog(
            onDismissRequest = { newNodeDialog = false; newNodeName = "" },
            title = { Text("Node Baru") },
            text = {
                OutlinedTextField(value = newNodeName, onValueChange = { newNodeName = it }, label = { Text("Nama") }, singleLine = true)
            },
            confirmButton = {
                TextButton(enabled = newNodeName.isNotBlank(), onClick = {
                    editorViewModel.addNode(newNodeName.trim())
                    newNodeName = ""
                    newNodeDialog = false
                }) { Text("Tambah") }
            },
            dismissButton = {
                TextButton(onClick = { newNodeDialog = false; newNodeName = "" }) { Text("Batal") }
            }
        )
    }
}
