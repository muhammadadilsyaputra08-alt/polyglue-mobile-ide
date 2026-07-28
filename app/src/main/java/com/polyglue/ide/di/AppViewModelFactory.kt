package com.polyglue.ide.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.polyglue.ide.core.engine.NativeEngine
import com.polyglue.ide.ui.viewmodel.BuildViewModel
import com.polyglue.ide.ui.viewmodel.EditorViewModel
import com.polyglue.ide.ui.viewmodel.ProjectViewModel
import com.polyglue.ide.ui.viewmodel.SnippetViewModel

/**
 * Factory manual (project ini tidak pakai Hilt) untuk menyuplai Repository dari
 * [AppModule] + [NativeEngine] ke tiap ViewModel lewat `viewModel(factory = ...)`
 * di layer Compose. Lihat HANDOFF.md §4/§13 — sebelumnya TIDAK ADA
 * ViewModelProvider.Factory sama sekali di project ini sehingga screen tidak
 * pernah benar-benar terhubung ke ViewModel/Room.
 */
class AppViewModelFactory(
    private val appModule: AppModule,
    private val engine: NativeEngine
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProjectViewModel::class.java) ->
                ProjectViewModel(appModule.projectRepository) as T

            modelClass.isAssignableFrom(SnippetViewModel::class.java) ->
                SnippetViewModel(appModule.snippetRepository) as T

            modelClass.isAssignableFrom(EditorViewModel::class.java) ->
                EditorViewModel(engine, appModule.sceneRepository) as T

            modelClass.isAssignableFrom(BuildViewModel::class.java) ->
                BuildViewModel(appModule.buildRepository, appModule.projectRepository, appModule.sceneRepository) as T

            else -> throw IllegalArgumentException("ViewModel tidak dikenal: ${modelClass.name}")
        }
    }
}
