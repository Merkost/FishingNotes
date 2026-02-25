package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.domain.entity.solunar.Solunar
import retrofit2.http.GET
import retrofit2.http.Path

interface SolunarApiService {

    @GET("solunar/{latitude},{longitude},{date},{tz}")
    suspend fun getSolunar(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("date") date: String = "",
        @Path("tz") tz: Int = 0,
    ): Solunar
}