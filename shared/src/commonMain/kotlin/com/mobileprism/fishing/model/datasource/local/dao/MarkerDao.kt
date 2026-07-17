package com.mobileprism.fishing.model.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobileprism.fishing.model.datasource.local.entity.MarkerEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markers WHERE userId = :userId ORDER BY dateOfCreation DESC")
    fun getAllByUser(userId: String): Flow<List<MarkerEntity>>

    @Query("SELECT * FROM markers WHERE id = :markerId")
    suspend fun getById(markerId: String): MarkerEntity?

    @Query("SELECT * FROM markers WHERE syncStatus != ${SyncStatus.SYNCED}")
    suspend fun getPending(): List<MarkerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MarkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MarkerEntity>)

    @Update
    suspend fun update(entity: MarkerEntity)

    @Query("DELETE FROM markers WHERE id = :markerId")
    suspend fun deleteByMarkerId(markerId: String)

    @Query("UPDATE markers SET syncStatus = :syncStatus WHERE id = :markerId")
    suspend fun updateSyncStatus(markerId: String, syncStatus: Int)

    @Query("DELETE FROM markers")
    suspend fun deleteAll()
}
