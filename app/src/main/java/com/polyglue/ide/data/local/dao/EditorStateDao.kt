package com.polyglue.ide.data.local.dao

import androidx.room.*
import com.polyglue.ide.data.local.entity.EditorStateEntity

@Dao
interface EditorStateDao {
    @Query("SELECT * FROM editor_states WHERE projectId = :projectId")
    suspend fun getEditorState(projectId: String): EditorStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditorState(state: EditorStateEntity)

    @Update
    suspend fun updateEditorState(state: EditorStateEntity)

    @Query("UPDATE editor_states SET cameraX = :x, cameraY = :y, cameraZoom = :zoom, updatedAt = :time WHERE projectId = :projectId")
    suspend fun updateCamera(projectId: String, x: Float, y: Float, zoom: Float, time: Long = System.currentTimeMillis())
}
