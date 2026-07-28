package com.polyglue.ide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.polyglue.ide.PolyGlueApplication
import com.polyglue.ide.data.local.entity.ProjectEntity
import com.polyglue.ide.data.repository.BuildRepository
import com.polyglue.ide.ui.viewmodel.BuildViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildScreen(navController: NavController, projectId: String) {
    val app = LocalContext.current.applicationContext as PolyGlueApplication
    val viewModel: BuildViewModel = viewModel(factory = app.viewModelFactory)

    var project by remember { mutableStateOf<ProjectEntity?>(null) }
    val buildConfig by viewModel.buildConfig.collectAsState()
    val buildState by viewModel.buildState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.resetState()
        viewModel.loadConfig(projectId)
        project = app.appModule.projectRepository.getProject(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build & Export") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = buildState) {
                is BuildRepository.BuildState.Idle -> {
                    val config = buildConfig
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Konfigurasi Build", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = project?.targetPackage ?: "",
                                onValueChange = { pkg -> project = project?.copy(targetPackage = pkg); viewModel.setPackageName(pkg) },
                                label = { Text("Package Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = project?.versionName ?: "",
                                onValueChange = { v -> project = project?.copy(versionName = v); viewModel.setVersionName(v) },
                                label = { Text("Version Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = config?.enableProguard ?: true,
                                    onCheckedChange = { viewModel.setEnableProguard(it) }
                                )
                                Text("Enable ProGuard")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Orientasi Game", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                listOf("landscape" to "Landscape", "portrait" to "Portrait").forEachIndexed { index, (value, label) ->
                                    SegmentedButton(
                                        selected = (config?.targetOrientation ?: "landscape") == value,
                                        onClick = { viewModel.setOrientation(value) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                                    ) { Text(label) }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.build(projectId) }, modifier = Modifier.fillMaxWidth(0.6f)) {
                        Icon(Icons.Default.Build, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Build APK")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Catatan: hasil saat ini adalah paket source (Lua + scene JSON), belum APK terinstall sungguhan — lihat HANDOFF.md §4/§10 untuk rencana \"Export Project\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                is BuildRepository.BuildState.Preparing -> BuildProgress(state.message, null)
                is BuildRepository.BuildState.Compiling -> BuildProgress("Mengompilasi...", state.progress)
                is BuildRepository.BuildState.Packaging -> BuildProgress("Mengemas...", state.progress)
                is BuildRepository.BuildState.Signing -> BuildProgress("Menandatangani...", state.progress)
                is BuildRepository.BuildState.Success -> {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Build Berhasil!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.apkPath, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.resetState() }) { Text("Build Lagi") }
                }
                is BuildRepository.BuildState.Error -> {
                    Icon(Icons.Default.Error, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Build Gagal", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.message, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.resetState() }) { Text("Coba Lagi") }
                }
            }
        }
    }
}

@Composable
private fun BuildProgress(message: String, progress: Float?) {
    CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 4.dp)
    Spacer(modifier = Modifier.height(16.dp))
    Text(message, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    if (progress != null) {
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
    } else {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.7f))
    }
}
