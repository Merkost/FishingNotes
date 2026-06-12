package com.mobileprism.fishing.domain.use_cases.catches

import app.cash.turbine.test
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.testutils.FakeCatchesRepositoryRead
import com.mobileprism.fishing.testutils.userCatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest

class GetUserCatchesUseCaseTest {

    @Test
    fun emitsAddedCatches() = runTest {
        val stateFlow = MutableSharedFlow<ContentStateOld<UserCatch>>()
        val useCase = GetUserCatchesUseCase(FakeCatchesRepositoryRead(catchesState = stateFlow))

        useCase().test {
            val catch1 = userCatch(id = "c1")
            val catch2 = userCatch(id = "c2")
            stateFlow.emit(ContentStateOld(added = mutableListOf(catch1, catch2)))

            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.any { it.id == "c1" })
            assertTrue(result.any { it.id == "c2" })
        }
    }

    @Test
    fun removesDeletedCatches() = runTest {
        val stateFlow = MutableSharedFlow<ContentStateOld<UserCatch>>()
        val useCase = GetUserCatchesUseCase(FakeCatchesRepositoryRead(catchesState = stateFlow))

        useCase().test {
            val catch1 = userCatch(id = "c1")
            val catch2 = userCatch(id = "c2")
            stateFlow.emit(ContentStateOld(added = mutableListOf(catch1, catch2)))
            awaitItem()

            stateFlow.emit(ContentStateOld(deleted = mutableListOf(catch1)))
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("c2", result.first().id)
        }
    }

    @Test
    fun replacesModifiedCatchById() = runTest {
        val stateFlow = MutableSharedFlow<ContentStateOld<UserCatch>>()
        val useCase = GetUserCatchesUseCase(FakeCatchesRepositoryRead(catchesState = stateFlow))

        useCase().test {
            val original = userCatch(id = "c1", fishType = "Bass")
            stateFlow.emit(ContentStateOld(added = mutableListOf(original)))
            awaitItem()

            val modified = userCatch(id = "c1", fishType = "Trout")
            stateFlow.emit(ContentStateOld(modified = mutableListOf(modified)))
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Trout", result.first().fishType)
        }
    }

    @Test
    fun sequentialEmissionsAccumulate() = runTest {
        val stateFlow = MutableSharedFlow<ContentStateOld<UserCatch>>()
        val useCase = GetUserCatchesUseCase(FakeCatchesRepositoryRead(catchesState = stateFlow))

        useCase().test {
            stateFlow.emit(ContentStateOld(added = mutableListOf(userCatch(id = "c1"))))
            assertEquals(1, awaitItem().size)

            stateFlow.emit(ContentStateOld(added = mutableListOf(userCatch(id = "c2"))))
            assertEquals(2, awaitItem().size)

            stateFlow.emit(ContentStateOld(added = mutableListOf(userCatch(id = "c3"))))
            assertEquals(3, awaitItem().size)
        }
    }

    @Test
    fun addThenModifySameCatch() = runTest {
        val stateFlow = MutableSharedFlow<ContentStateOld<UserCatch>>()
        val useCase = GetUserCatchesUseCase(FakeCatchesRepositoryRead(catchesState = stateFlow))

        useCase().test {
            stateFlow.emit(ContentStateOld(added = mutableListOf(userCatch(id = "c1", fishWeight = 2.0))))
            awaitItem()

            stateFlow.emit(ContentStateOld(modified = mutableListOf(userCatch(id = "c1", fishWeight = 5.0))))
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(5.0, result.first().fishWeight)
        }
    }

    @Test
    fun addThenDeleteSameCatch() = runTest {
        val stateFlow = MutableSharedFlow<ContentStateOld<UserCatch>>()
        val useCase = GetUserCatchesUseCase(FakeCatchesRepositoryRead(catchesState = stateFlow))

        useCase().test {
            val catch1 = userCatch(id = "c1")
            stateFlow.emit(ContentStateOld(added = mutableListOf(catch1)))
            assertEquals(1, awaitItem().size)

            stateFlow.emit(ContentStateOld(deleted = mutableListOf(catch1)))
            val result = awaitItem()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun emptyStateEmitsEmptyList() = runTest {
        val stateFlow = MutableSharedFlow<ContentStateOld<UserCatch>>()
        val useCase = GetUserCatchesUseCase(FakeCatchesRepositoryRead(catchesState = stateFlow))

        useCase().test {
            stateFlow.emit(ContentStateOld())
            val result = awaitItem()
            assertTrue(result.isEmpty())
        }
    }
}
