package com.polyglue.ide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ListItem(
                headlineContent = { Text("GitHub Integration") },
                supportingContent = { Text("Hubungkan akun GitHub") },
                leadingContent = { Icon(Icons.Default.Cloud, null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Editor Preferences") },
                supportingContent = { Text("Tema, font, keybindings") },
                leadingContent = { Icon(Icons.Default.Edit, null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Performance") },
                supportingContent = { Text("Optimasi & cache") },
                leadingContent = { Icon(Icons.Default.Speed, null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            Divider()
            ListItem(
                headlineContent = { Text("About PolyGlue") },
                supportingContent = { Text("Versi 1.0.0-alpha") },
                leadingContent = { Icon(Icons.Default.Info, null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
        }
    }
}
