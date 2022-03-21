package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import java.text.SimpleDateFormat
import java.util.*

class GetFishActivityUseCase(private val solunarRepository: SolunarRepository) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        hour: Int = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
    ) = flow {

        val currentDate = Date()
        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        val date = format.format(currentDate)
        //val timeZone = TimeZone.getDefault().getOffset(currentDate.time)
        val timeZone = SimpleDateFormat("ZZZZZ", Locale.getDefault())
            .format(System.currentTimeMillis()).split(":")[0]

        solunarRepository.getSolunar(latitude, longitude, date, timeZone.toIntOrNull() ?: 0)
            .single().fold(
                onSuccess = {
                    emit(it.hourlyRating[hour])
                },
                onFailure = {
                    // TODO: emit error
                }
            )

    }
}
