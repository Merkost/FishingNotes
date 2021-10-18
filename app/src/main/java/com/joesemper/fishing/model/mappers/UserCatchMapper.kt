package com.joesemper.fishing.model.mappers

import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.utils.getCurrentUser
import com.joesemper.fishing.utils.getNewCatchId

class UserCatchMapper {

    fun mapRawCatch(newCatch: RawUserCatch, photoLinks: List<String>) = UserCatch(
        id = getNewCatchId(),
        userId = getCurrentUser()!!.uid,
        description = newCatch.description ?: "",
        date = newCatch.date,
        fishType = newCatch.fishType,
        fishAmount = newCatch.fishAmount ?: 0,
        fishWeight = newCatch.fishWeight ?: 0.0,
        fishingRodType = newCatch.fishingRodType ?: "",
        fishingBait = newCatch.fishingBait ?: "",
        fishingLure = newCatch.fishingLure ?: "",
        userMarkerId = newCatch.markerId,
        isPublic = newCatch.isPublic,
        downloadPhotoLinks = photoLinks
    )
}