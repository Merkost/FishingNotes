package com.mobileprism.fishing.domain.entity.content

import androidx.compose.runtime.Immutable
import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class UserMapMarker(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "My marker",
    val description: String = "",
    val markerColor: Int = DEFAULT_MARKER_COLOR,
    val catchesCount: Int = 0,
    val dateOfCreation: Long = 0,
    val visible: Boolean = true,
    val public: Boolean = false,
    val notes: List<Note> = listOf(),
    val lastModified: Long = 0
) : MapMarker {
    companion object {
        const val DEFAULT_MARKER_COLOR: Int = 0xFFEC407A.toInt()
    }
}
