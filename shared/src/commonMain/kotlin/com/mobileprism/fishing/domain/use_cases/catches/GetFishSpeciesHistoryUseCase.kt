package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetFishSpeciesHistoryUseCase(private val repository: CatchesRepositoryRead) {

    operator fun invoke(): Flow<List<String>> =
        repository.getAllUserCatchesList()
            .map { catches ->
                catches.map { it.fishType }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            }
            .flowOn(Dispatchers.Default)
}
