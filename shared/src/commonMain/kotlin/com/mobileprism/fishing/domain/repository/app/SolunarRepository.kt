package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.solunar.Solunar

interface SolunarRepository {
    suspend fun getSolunar(latitude: Double, longitude: Double, date: String, timeZone: Int): Result<Solunar>
}