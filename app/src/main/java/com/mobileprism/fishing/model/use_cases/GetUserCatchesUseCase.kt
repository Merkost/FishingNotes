package com.mobileprism.fishing.model.use_cases

import com.google.android.gms.common.util.ArrayUtils.removeAll
import com.mobileprism.fishing.compose.ui.home.UiState
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import kotlinx.coroutines.flow.flow
import java.util.Collections.addAll

class GetUserCatchesUseCase(val repository: CatchesRepository) {

    suspend operator fun invoke() = flow<List<UserCatch>> {
        val current_catches: MutableList<UserCatch> = mutableListOf()

        repository.getAllUserCatchesState().collect { contentState ->

            contentState.modified.forEach { newCatch ->
                current_catches.removeAll { oldCatch ->
                    newCatch.id == oldCatch.id
                }
            }
            current_catches.apply {
                addAll(contentState.added)
                removeAll(contentState.deleted)
                addAll(contentState.modified)
            }

            emit(current_catches)
        }
    }
}