package com.mobileprism.fishing.ui.use_cases

import android.util.Log
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.model.entity.solunar.Solunar
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.SolunarRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import java.text.SimpleDateFormat
import java.util.*

class AddNewPlaceUseCase(private val markersRepository: MarkersRepository) {

    suspend operator fun invoke(
        newMarker: RawMapMarker
    ) = flow {
        emit(markersRepository.addNewMarker(newMarker).single())
    }
}
