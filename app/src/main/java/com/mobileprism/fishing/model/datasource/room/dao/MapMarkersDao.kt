package com.mobileprism.fishing.model.datasource.room.dao

import androidx.room.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

@Dao
interface MapMarkersDao {

    @Query("SELECT * FROM map_markers ORDER BY dateOfCreation")
    fun getMapMarkers(): Flow<List<UserMapMarker>>

    @Query("SELECT * FROM map_markers WHERE id == :markerId")
    fun getMarkerById(markerId: String): Flow<UserMapMarker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMapMarker(userMapMarker: UserMapMarker)

    @Delete
    suspend fun deleteMapMarker(userMapMarker: UserMapMarker)

    @Update
    suspend fun updateMapMarker(userMapMarker: UserMapMarker)
}