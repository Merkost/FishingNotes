package com.mobileprism.fishing.model.datasource.room.dao

import androidx.room.*
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow

@Dao
interface CatchesDao {

    @Query("SELECT * FROM catches ORDER BY date")
    fun getAllCatches(): Flow<List<UserCatch>>

    @Query("SELECT * FROM catches WHERE markerId == :markerId")
    fun getCatchesByMarker(markerId: String): Flow<List<UserCatch>>

    @Query("SELECT * FROM catches WHERE id == :catchId")
    fun getCatchById(catchId: String): Flow<UserCatch?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCatch(userCatch: UserCatch)

    @Delete
    suspend fun deleteCatch(userCatch: UserCatch)

    @Update
    suspend fun updateCatch(userCatch: UserCatch)
}