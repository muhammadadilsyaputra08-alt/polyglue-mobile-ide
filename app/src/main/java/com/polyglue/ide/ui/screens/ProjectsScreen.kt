package com.polyglue.ide.ui.screens

import androidx.compose.foundation.clickable
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
import com.polyglue.ide.data.local.entity.ProjectEntity
import com.polyglue.ide.ui.navigation.Screen
import com.polyglue.ide.ui.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*

private fun relativeTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val minutes = diff / 60000
    return when {
        minutes < 1 -> "baru saja"
        minutes < 60 -> "$minutes menit lalu"
        minutes < 1440 -> "${minutes / 60} jam lalu"
        minutes < 10080 -> "${minutes / 1440} hari lalu"
        else -> SimpleDateFormat("dd MMM yyyy", Locale("id")).format(Date(millis))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as PolyGlueApplication
    val viewModel: ProjectViewModel = viewModel(factory = app.viewModelFactory)

    val projects by viewModel.projects.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectPendingDelete by remember { mutableStateOf<ProjectEntity?>(null) }
    var menuTargetId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proyek") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Project")
            }
        }
    ) { padding ->
        if (projects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Belum ada proyek. Tekan + untuk membuat proyek baru.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.selectProject(project)
                            navController.navigate(Screen.Editor.route + "/${project.id}")
                        }
                    ) {
                        ListItem(
                            headlineContent = { Text(project.name) },
                            supportingContent = { Text("Terakhir diedit: ${relativeTime(project.updatedAt)}") },
                            leadingContent = { Icon(Icons.Default.VideogameAsset, contentDescription = null) },
                            trailingContent = {
                                Box {
                                    IconButton(onClick = { menuTargetId = project.id }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                    }
                                    DropdownMenu(
                                        expanded = menuTargetId == project.id,
                                        onDismissRequest = { menuTargetId = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Duplikat") },
                                            onClick = {
                                                menuTargetId = null
                                                viewModel.duplicateProject(project.id, "${project.name} (Copy)")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Hapus") },
                                            onClick = {
                                                menuTargetId = null
                                                projectPendingDelete = project
                                            }
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; projectName = "" },
            title = { Text("Proyek Baru") },
            text = {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Nama Proyek") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = projectName.isNotBlank(),
                    onClick = {
                        viewModel.createProject(projectName.trim())
                        showDialog = false
                        projectName = ""
                    }
                ) { Text("Buat") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; projectName = "" }) { Text("Batal") }
            }
        )
    }

    projectPendingDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text("Hapus Proyek?") },
            text = { Text("\"${project.name}\" akan dihapus permanen beserta semua scene di dalamnya.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProject(project)
                    projectPendingDelete = null
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) { Text("Batal") }
            }
        )
    }
}
