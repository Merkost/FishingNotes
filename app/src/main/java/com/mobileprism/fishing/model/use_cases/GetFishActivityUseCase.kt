package com.mobileprism.fishing.model.use_cases

import android.util.Log
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.solunar.Solunar
import com.mobileprism.fishing.model.repository.app.SolunarRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
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

        val result =
            solunarRepository.getSolunar(latitude, longitude, date, timeZone.toIntOrNull() ?: 0)
                .single()

        when (result) {
            is RetrofitWrapper.Success<Solunar> -> {
                val solunar = result.data
                val fishActivity = solunar.hourlyRating[hour]
                emit(fishActivity)
            }
            is RetrofitWrapper.Error -> {
                Log.d("SOLUNAR ERROR", result.errorType.error.toString())
                //emit(Result.failure(result.errorType.error ?: Throwable()))
                //_weatherState.value = RetrofitWrapper.Error(result.errorType)
            }
            else -> {}
        }
    }
}
