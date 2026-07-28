package com.polyglue.ide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.polyglue.ide.PolyGlueApplication
import com.polyglue.ide.ui.viewmodel.SnippetViewModel
import kotlinx.coroutines.launch

/**
 * @param sceneId ID scene Editor yang aktif, atau "none" kalau layar ini
 * dibuka dari luar konteks Editor (mis. dari Home). Kalau bukan "none",
 * tombol "Add" akan benar-benar menyisipkan `luaTemplate` snippet ke
 * `SceneEntity.luaScript` scene tsb (HANDOFF.md §12 item 4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsScreen(navController: NavController, sceneId: String = "none") {
    val app = LocalContext.current.applicationContext as PolyGlueApplication
    val viewModel: SnippetViewModel = viewModel(factory = app.viewModelFactory)
    val coroutineScope = rememberCoroutineScope()
    val canInsert = sceneId != "none" && sceneId.isNotBlank()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var insertedMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val categories = listOf("All", "Tilemap", "Camera", "Animation", "Audio", "Particle", "Collision", "Controller", "AI", "Pathfinding", "RPG", "Multiplayer", "UI", "Data", "Mobile", "Physics", "Shader", "AI Tools", "Build", "Debug", "Productivity")

    val allSnippets by viewModel.allSnippets.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(searchQuery) { viewModel.search(searchQuery) }
    LaunchedEffect(insertedMessage) {
        insertedMessage?.let {
            snackbarHostState.showSnackbar(it)
            insertedMessage = null
        }
    }

    val base = if (searchQuery.isBlank()) allSnippets else searchResults
    val filtered = if (selectedCategory == "All") base else base.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (canInsert) "Smart Snippets — sisipkan ke script" else "Smart Snippets") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari snippet...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            ScrollableTabRow(selectedTabIndex = categories.indexOf(selectedCategory), modifier = Modifier.fillMaxWidth()) {
                categories.forEach { category ->
                    Tab(selected = selectedCategory == category, onClick = { selectedCategory = category }, text = { Text(category) })
                }
            }
            if (allSnippets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { snippet ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(snippet.name) },
                                supportingContent = { Text(snippet.description) },
                                leadingContent = { Icon(Icons.Default.Extension, null) },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = { viewModel.toggleFavorite(snippet.id) }) {
                                            Icon(
                                                if (snippet.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite"
                                            )
                                        }
                                        IconButton(onClick = {
                                            viewModel.useSnippet(snippet.id)
                                            if (canInsert) {
                                                coroutineScope.launch {
                                                    app.appModule.sceneRepository.appendScript(sceneId, snippet.luaTemplate)
                                                    insertedMessage = "\"${snippet.name}\" disisipkan ke script"
                                                }
                                            }
                                        }) {
                                            Icon(
                                                if (canInsert) Icons.Default.AddBox else Icons.Default.Add,
                                                contentDescription = "Add"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
