package com.polyglue.ide.data.local.dao

import androidx.room.*
import com.polyglue.ide.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getAssetsByProject(projectId: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE projectId = :projectId AND type = :type")
    fun getAssetsByType(projectId: String, type: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: String): AssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("SELECT * FROM assets WHERE projectId = :projectId AND isPacked = 0 AND type = 'image'")
    suspend fun getUnpackedImages(projectId: String): List<AssetEntity>
}
