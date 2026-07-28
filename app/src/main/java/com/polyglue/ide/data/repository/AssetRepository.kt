package com.polyglue.ide.data.repository

import com.polyglue.ide.data.local.dao.AssetDao
import com.polyglue.ide.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AssetRepository(private val assetDao: AssetDao) {
    fun getAssetsByProject(projectId: String): Flow<List<AssetEntity>> = assetDao.getAssetsByProject(projectId)
    fun getAssetsByType(projectId: String, type: String): Flow<List<AssetEntity>> = assetDao.getAssetsByType(projectId, type)

    suspend fun getAsset(id: String): AssetEntity? = assetDao.getAssetById(id)

    suspend fun addAsset(projectId: String, name: String, type: String, path: String, relativePath: String): AssetEntity {
        val asset = AssetEntity(id = UUID.randomUUID().toString(), projectId = projectId, name = name, type = type, path = path, relativePath = relativePath)
        assetDao.insertAsset(asset)
        return asset
    }

    suspend fun deleteAsset(asset: AssetEntity) {
        assetDao.deleteAsset(asset)
    }

    suspend fun getUnpackedImages(projectId: String): List<AssetEntity> = assetDao.getUnpackedImages(projectId)
}
