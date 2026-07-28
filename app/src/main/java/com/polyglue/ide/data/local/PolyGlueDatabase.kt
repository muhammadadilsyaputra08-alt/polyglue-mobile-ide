package com.polyglue.ide.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.polyglue.ide.data.local.dao.*
import com.polyglue.ide.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        SceneEntity::class,
        AssetEntity::class,
        SnippetEntity::class,
        ScriptEntity::class,
        BuildConfigEntity::class,
        EditorStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PolyGlueDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun sceneDao(): SceneDao
    abstract fun assetDao(): AssetDao
    abstract fun snippetDao(): SnippetDao
    abstract fun scriptDao(): ScriptDao
    abstract fun buildConfigDao(): BuildConfigDao
    abstract fun editorStateDao(): EditorStateDao

    companion object {
        @Volatile
        private var INSTANCE: PolyGlueDatabase? = null

        fun getDatabase(context: Context): PolyGlueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PolyGlueDatabase::class.java,
                    "polyglue_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(SeedCallback)
                .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Seed 28 built-in Smart Snippets (lihat [SnippetSeed]) sekali saat
         * database pertama kali dibuat. `onCreate` hanya terpanggil kalau file
         * DB belum ada sama sekali, jadi tidak akan duplikat di run berikutnya.
         */
        private object SeedCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    INSTANCE?.snippetDao()?.insertSnippets(SnippetSeed.ALL)
                }
            }
        }
    }
}
