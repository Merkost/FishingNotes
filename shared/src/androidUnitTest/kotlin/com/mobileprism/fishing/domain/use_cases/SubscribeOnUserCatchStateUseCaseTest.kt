package com.mobileprism.fishing.domain.use_cases

import app.cash.turbine.test
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.testutils.userCatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class SubscribeOnUserCatchStateUseCaseTest {

    private val catchesRepository = mockk<CatchesRepositoryUpdate>()

    @Test
    fun delegatesToRepository() = runTest {
        val catch = userCatch(id = "c1")
        every { catchesRepository.subscribeOnUserCatchState("m1", "c1") } returns flowOf(catch)

        val useCase = SubscribeOnUserCatchStateUseCase(catchesRepository)
        useCase("m1", "c1").test {
            awaitItem()
            awaitComplete()
        }

        verify { catchesRepository.subscribeOnUserCatchState(markerId = "m1", catchId = "c1") }
    }

    @Test
    fun emitsCatchUpdates() = runTest {
        val catch1 = userCatch(id = "c1", fishType = "Bass")
        val catch2 = userCatch(id = "c1", fishType = "Trout")
        every { catchesRepository.subscribeOnUserCatchState("m1", "c1") } returns
                flowOf(catch1, catch2)

        val useCase = SubscribeOnUserCatchStateUseCase(catchesRepository)

        useCase("m1", "c1").test {
            val first = awaitItem()
            assertEquals("Bass", first.fishType)
            val second = awaitItem()
            assertEquals("Trout", second.fishType)
            awaitComplete()
        }
    }
}
