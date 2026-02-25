package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.offsetAt

class GetFishActivityUseCase(private val solunarRepository: SolunarRepository) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        hour: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    ) = flow {
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val date = "${localDate.year}${localDate.monthNumber.toString().padStart(2, '0')}${localDate.dayOfMonth.toString().padStart(2, '0')}"
        val offset = TimeZone.currentSystemDefault().offsetAt(now)
        val timeZone = offset.totalSeconds / 3600

        solunarRepository.getSolunar(latitude, longitude, date, timeZone)
            .single().fold(
                onSuccess = {
                    emit(it.hourlyRating[hour])
                },
                onFailure = { throw it }
            )
    }
}
