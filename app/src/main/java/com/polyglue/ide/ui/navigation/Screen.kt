package com.polyglue.ide.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Projects : Screen("projects")
    object Editor : Screen("editor")
    object Assets : Screen("assets")
    object Settings : Screen("settings")
    object Snippets : Screen("snippets") // dipakai dengan suffix "/{sceneId}", lihat PolyGlueNavHost
    object Build : Screen("build")
}
