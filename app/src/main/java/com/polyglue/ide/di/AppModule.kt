package com.polyglue.ide.di

import android.content.Context
import com.polyglue.ide.data.local.PolyGlueDatabase
import com.polyglue.ide.data.repository.*
import kotlinx.coroutines.CoroutineScope

class AppModule(
    private val context: Context,
    private val database: PolyGlueDatabase,
    private val scope: CoroutineScope
) {
    val projectRepository by lazy { 
        ProjectRepository(database.projectDao(), database.sceneDao()) 
    }
    val sceneRepository by lazy { 
        SceneRepository(database.sceneDao()) 
    }
    val snippetRepository by lazy { 
        SnippetRepository(database.snippetDao()) 
    }
    val assetRepository by lazy { 
        AssetRepository(database.assetDao()) 
    }
    val buildRepository by lazy { 
        BuildRepository(context, database.buildConfigDao()) 
    }
}
