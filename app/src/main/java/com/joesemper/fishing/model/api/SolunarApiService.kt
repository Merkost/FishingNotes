package com.joesemper.fishing.model.api

import com.joesemper.fishing.model.entity.solunar.Solunar
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SolunarApiService {

    @GET("solunar/{latitude},{longitude},{date},{tz}")
    suspend fun getSolunar(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("date") date: String = "",
        @Path("tz") tz: Int = 0,
    ): Solunar
}