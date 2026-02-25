package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import kotlinx.coroutines.flow.flow

class GetUserCatchesUseCase(val repository: CatchesRepositoryRead) {

    suspend operator fun invoke() = flow<List<UserCatch>> {
        val currentCatches: MutableList<UserCatch> = mutableListOf()

        repository.getAllUserCatchesState().collect { contentState ->

            contentState.modified.forEach { newCatch ->
                currentCatches.removeAll { oldCatch ->
                    newCatch.id == oldCatch.id
                }
            }

            currentCatches.apply {
                addAll(contentState.added)
                removeAll(contentState.deleted)
                addAll(contentState.modified)
            }

            emit(currentCatches)
        }
    }
}
