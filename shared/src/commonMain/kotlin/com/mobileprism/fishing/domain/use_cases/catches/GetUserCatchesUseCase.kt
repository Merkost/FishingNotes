package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import kotlinx.coroutines.flow.flow

class GetUserCatchesUseCase(val repository: CatchesRepositoryRead) {

    suspend operator fun invoke() = flow<List<UserCatch>> {
        val currentCatches: MutableList<UserCatch> = mutableListOf()

        repository.getAllUserCatchesState().collect { contentState ->
            val modifiedIds = contentState.modified.map { it.id }.toSet()
            val deletedIds = contentState.deleted.map { it.id }.toSet()
            currentCatches.removeAll { it.id in modifiedIds || it.id in deletedIds }
            currentCatches.addAll(contentState.added)
            currentCatches.addAll(contentState.modified)

            emit(currentCatches)
        }
    }
}
