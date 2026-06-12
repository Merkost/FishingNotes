package com.mobileprism.fishing.model.datasource.local.mapper

import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datasource.local.entity.CatchEntity
import com.mobileprism.fishing.model.datasource.local.entity.MarkerEntity
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun UserCatch.toEntity(syncStatus: Int = SyncStatus.SYNCED): CatchEntity {
    return CatchEntity(
        id = id,
        userId = userId,
        description = description,
        noteId = note.id,
        noteTitle = note.title,
        noteDescription = note.description,
        noteDateCreated = note.dateCreated,
        date = date,
        fishType = fishType,
        fishAmount = fishAmount,
        fishWeight = fishWeight,
        fishingRodType = fishingRodType,
        fishingBait = fishingBait,
        fishingLure = fishingLure,
        userMarkerId = userMarkerId,
        placeTitle = placeTitle,
        isPublic = isPublic,
        downloadPhotoLinks = Json.encodeToString(downloadPhotoLinks),
        weatherPrimary = weatherPrimary,
        weatherIcon = weatherIcon,
        weatherTemperature = weatherTemperature,
        weatherWindSpeed = weatherWindSpeed,
        weatherWindDeg = weatherWindDeg,
        weatherPressure = weatherPressure,
        weatherMoonPhase = weatherMoonPhase,
        syncStatus = syncStatus,
        lastModified = lastModified
    )
}

fun CatchEntity.toDomain(): UserCatch {
    val photos: List<String> = try { Json.decodeFromString(downloadPhotoLinks) } catch (_: Exception) { emptyList() }

    return UserCatch(
        id = id,
        userId = userId,
        description = description,
        note = Note(
            id = noteId,
            title = noteTitle,
            description = noteDescription,
            dateCreated = noteDateCreated
        ),
        date = date,
        fishType = fishType,
        fishAmount = fishAmount,
        fishWeight = fishWeight,
        fishingRodType = fishingRodType,
        fishingBait = fishingBait,
        fishingLure = fishingLure,
        userMarkerId = userMarkerId,
        placeTitle = placeTitle,
        isPublic = isPublic,
        downloadPhotoLinks = photos,
        weatherPrimary = weatherPrimary,
        weatherIcon = weatherIcon,
        weatherTemperature = weatherTemperature,
        weatherWindSpeed = weatherWindSpeed,
        weatherWindDeg = weatherWindDeg,
        weatherPressure = weatherPressure,
        weatherMoonPhase = weatherMoonPhase,
        lastModified = lastModified
    )
}

fun UserMapMarker.toEntity(syncStatus: Int = SyncStatus.SYNCED): MarkerEntity {
    return MarkerEntity(
        id = id,
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        title = title,
        description = description,
        markerColor = markerColor,
        catchesCount = catchesCount,
        dateOfCreation = dateOfCreation,
        visible = visible,
        public = public,
        notes = Json.encodeToString(notes),
        syncStatus = syncStatus,
        lastModified = lastModified
    )
}

fun MarkerEntity.toDomain(): UserMapMarker {
    val notesList: List<Note> = try { Json.decodeFromString(notes) } catch (_: Exception) { emptyList() }

    return UserMapMarker(
        id = id,
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        title = title,
        description = description,
        markerColor = markerColor,
        catchesCount = catchesCount,
        dateOfCreation = dateOfCreation,
        visible = visible,
        public = public,
        notes = notesList,
        lastModified = lastModified
    )
}
