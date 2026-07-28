package com.polyglue.ide.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.polyglue.ide.PolyGlueApplication
import com.polyglue.ide.data.local.entity.AssetEntity
import kotlinx.coroutines.launch
import java.io.File

private val TAB_TYPES = listOf("image", "audio", "font", "script", "shader")
private val TAB_LABELS = listOf("Images", "Audio", "Fonts", "Scripts", "Shaders")

private fun guessAssetType(fileName: String, mime: String?): String = when {
    mime?.startsWith("image/") == true -> "image"
    mime?.startsWith("audio/") == true -> "audio"
    fileName.endsWith(".ttf") || fileName.endsWith(".otf") -> "font"
    fileName.endsWith(".lua") -> "script"
    fileName.endsWith(".glsl") || fileName.endsWith(".frag") || fileName.endsWith(".vert") -> "shader"
    else -> "image"
}

private fun queryFileName(context: android.content.Context, uri: Uri): String {
    var name = uri.lastPathSegment ?: "asset"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) name = cursor.getString(idx)
    }
    return name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(navController: NavController, projectId: String) {
    val context = LocalContext.current
    val app = context.applicationContext as PolyGlueApplication
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var assets by remember { mutableStateOf<List<AssetEntity>>(emptyList()) }

    LaunchedEffect(projectId) {
        app.appModule.assetRepository.getAssetsByProject(projectId).collect { assets = it }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val name = queryFileName(context, uri)
            val mime = context.contentResolver.getType(uri)
            val type = guessAssetType(name, mime)
            val assetsDir = File(context.getExternalFilesDir(null), "projects/$projectId/assets").apply { mkdirs() }
            val destFile = File(assetsDir, name)
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
            app.appModule.assetRepository.addAsset(
                projectId = projectId,
                name = name,
                type = type,
                path = destFile.absolutePath,
                relativePath = "assets/${destFile.name}"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assets") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Import")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                TAB_LABELS.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            val filtered = assets.filter { it.type == TAB_TYPES[selectedTab] }
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada asset ${TAB_LABELS[selectedTab].lowercase()}. Tekan ikon import di kanan atas.")
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 100.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(filtered, key = { it.id }) { asset ->
                        Card(modifier = Modifier.padding(4.dp).aspectRatio(1f)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        when (asset.type) {
                                            "image" -> Icons.Default.Image
                                            "audio" -> Icons.Default.AudioFile
                                            "font" -> Icons.Default.FontDownload
                                            "script" -> Icons.Default.Description
                                            else -> Icons.Default.InsertDriveFile
                                        },
                                        contentDescription = null
                                    )
                                    Text(asset.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
