package com.mobileprism.fishing.model.repository

import com.mobileprism.fishing.model.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.repository.app.MarkersRepository

interface UserContentRepository: CatchesRepository, MarkersRepository