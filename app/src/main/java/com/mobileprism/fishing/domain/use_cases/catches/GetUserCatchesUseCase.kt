package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import kotlinx.coroutines.flow.flow

class GetUserCatchesUseCase(val repository: CatchesRepository) {

    suspend operator fun invoke() = flow<List<UserCatch>> {
        val currentCatches: MutableList<UserCatch> = mutableListOf()

        repository.getAllUserCatchesList().collect { content ->
            emit(content)

            /*contentState.modified.forEach { newCatch ->
                currentCatches.removeAll { oldCatch ->
                    newCatch.id == oldCatch.id
                }
            }

            currentCatches.apply {
                addAll(contentState.added)
                removeAll(contentState.deleted)
                addAll(contentState.modified)
            }

            emit(currentCatches)*/
        }
    }
}