package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.solunar.Solunar
import kotlinx.coroutines.flow.Flow

interface SolunarRepository {
    fun getSolunar(latitude: Double, longitude: Double, date: String, timeZone: Int): Flow<Result<Solunar>>
}