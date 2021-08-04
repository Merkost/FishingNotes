package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.raw.RawUserCatch

class NewCatchRepositoryImpl(private val provider: DatabaseProvider): NewCatchRepository {
    override suspend fun addNewCatch(newCatch: RawUserCatch) = provider.addNewCatch(newCatch)
}