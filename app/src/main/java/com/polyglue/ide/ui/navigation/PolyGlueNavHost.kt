package com.polyglue.ide.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.polyglue.ide.ui.screens.*

@Composable
fun PolyGlueNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Projects.route) { ProjectsScreen(navController) }
        composable(Screen.Editor.route + "/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            EditorScreen(navController, projectId)
        }
        composable(Screen.Assets.route + "/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            AssetsScreen(navController, projectId)
        }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.Snippets.route + "/{sceneId}") { backStackEntry ->
            val sceneId = backStackEntry.arguments?.getString("sceneId") ?: "none"
            SnippetsScreen(navController, sceneId)
        }
        composable(Screen.Build.route + "/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            BuildScreen(navController, projectId)
        }
    }
}
