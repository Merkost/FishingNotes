package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository

interface UserContentRepository: CatchesRepository, MarkersRepository