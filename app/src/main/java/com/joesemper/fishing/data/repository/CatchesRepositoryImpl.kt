package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.datasource.DatabaseProvider

class CatchesRepositoryImpl(private val provider: DatabaseProvider): CatchesRepository {
    override fun getCatchesByMarkerId(markerId: String) = provider.getCatchesByMarkerId(markerId)
}