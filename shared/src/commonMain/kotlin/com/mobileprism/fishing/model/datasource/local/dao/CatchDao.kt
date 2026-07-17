package com.mobileprism.fishing.model.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobileprism.fishing.model.datasource.local.entity.CatchEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CatchDao {

    @Query("SELECT * FROM catches WHERE userId = :userId ORDER BY date DESC")
    fun getAllByUser(userId: String): Flow<List<CatchEntity>>

    @Query("SELECT * FROM catches WHERE userMarkerId = :markerId ORDER BY date DESC")
    fun getByMarkerId(markerId: String): Flow<List<CatchEntity>>

    @Query("SELECT * FROM catches WHERE id = :catchId")
    fun getById(catchId: String): Flow<CatchEntity?>

    @Query("SELECT * FROM catches WHERE syncStatus != ${SyncStatus.SYNCED}")
    suspend fun getPending(): List<CatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CatchEntity>)

    @Update
    suspend fun update(entity: CatchEntity)

    @Query("DELETE FROM catches WHERE id = :catchId")
    suspend fun deleteByCatchId(catchId: String)

    @Query("UPDATE catches SET syncStatus = :syncStatus WHERE id = :catchId")
    suspend fun updateSyncStatus(catchId: String, syncStatus: Int)

    @Query("DELETE FROM catches WHERE userMarkerId = :markerId")
    suspend fun deleteByMarkerId(markerId: String)

    @Query("DELETE FROM catches")
    suspend fun deleteAll()
}
