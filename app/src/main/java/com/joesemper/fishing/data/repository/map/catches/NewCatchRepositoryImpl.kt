package com.joesemper.fishing.data.repository.map.catches

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.RawUserCatch

class NewCatchRepositoryImpl(private val provider: DatabaseProvider): NewCatchRepository {
    override suspend fun addNewCatch(newCatch: RawUserCatch) = provider.addNewCatch(newCatch)
}