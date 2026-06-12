package com.mobileprism.fishing.ui.home.map

sealed class GeocoderResult {
    class Success(val placeName: String) : GeocoderResult()
    data object NoNamePlace : GeocoderResult()
    data object Failed : GeocoderResult()
    data object InProgress : GeocoderResult()
}

data class PlaceTileState(
    val geocoderResult: GeocoderResult = GeocoderResult.InProgress,
    val pointerState: PointerState = PointerState.ShowMarker,
)

sealed class CameraMoveState {
    data object MoveStart : CameraMoveState()
    data object MoveFinish : CameraMoveState()
}

sealed class PointerState {
    data object HideMarker : PointerState()
    data object ShowMarker : PointerState()
}

sealed class LocationState {
    data object NoPermission : LocationState()
    class LocationGranted(val latitude: Double, val longitude: Double) : LocationState()
    data object GpsNotEnabled : LocationState()
    data object Unavailable : LocationState()
}

sealed class MyLocationButtonState {
    data object Ready : MyLocationButtonState()
    data object Searching : MyLocationButtonState()
    data object NeedsPermission : MyLocationButtonState()
    data object PermissionBlocked : MyLocationButtonState()
    data object GpsDisabled : MyLocationButtonState()
    data object Unavailable : MyLocationButtonState()
}

sealed class MapUiState {
    data object NormalMode : MapUiState()
    data object PlaceSelectMode : MapUiState()
    data object BottomSheetInfoMode : MapUiState()
}

enum class AppMapType { Roadmap, Satellite, Hybrid, Terrain }

const val DEFAULT_ZOOM = 15f
const val DEFAULT_BEARING = 0f
