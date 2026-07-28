package com.polyglue.ide.data.local.dao

import androidx.room.*
import com.polyglue.ide.data.local.entity.ScriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM scripts WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getScriptsByProject(projectId: String): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE sceneId = :sceneId")
    fun getScriptsByScene(sceneId: String): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun getScriptById(id: String): ScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity)

    @Update
    suspend fun updateScript(script: ScriptEntity)

    @Query("UPDATE scripts SET content = :content, updatedAt = :time WHERE id = :id")
    suspend fun updateContent(id: String, content: String, time: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteScript(script: ScriptEntity)
}
