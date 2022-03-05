package com.mobileprism.fishing.model.use_cases

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.solunar.Solunar
import com.mobileprism.fishing.model.repository.app.SolunarRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import java.util.*

class GetFishActivityUseCase(private val solunarRepository: SolunarRepository) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        hour: Int = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
    ) = flow {
        val result = solunarRepository.getSolunar(latitude, longitude).single()
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
