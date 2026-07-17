package com.mobileprism.fishing.model.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobileprism.fishing.model.datasource.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingOperationEntity>

    @Query("SELECT * FROM pending_operations WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getAllForUser(userId: String): List<PendingOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingOperationEntity)

    @Update
    suspend fun update(entity: PendingOperationEntity)

    @Delete
    suspend fun delete(entity: PendingOperationEntity)

    @Query("DELETE FROM pending_operations WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteByEntity(entityType: String, entityId: String)

    @Query("DELETE FROM pending_operations WHERE userId != :userId")
    suspend fun deleteAllNotForUser(userId: String)

    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()
}
