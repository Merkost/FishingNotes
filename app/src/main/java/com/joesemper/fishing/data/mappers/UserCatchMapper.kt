package com.joesemper.fishing.data.mappers

import com.joesemper.fishing.data.entity.raw.RawUserCatch
import com.joesemper.fishing.data.entity.content.UserCatch
import com.joesemper.fishing.utils.getCurrentUser
import com.joesemper.fishing.utils.getNewCatchId

class UserCatchMapper {

    fun mapRawCatch(newCatch: RawUserCatch, photoLinks: List<String>) = UserCatch(
        id = getNewCatchId(),
        userId = getCurrentUser()!!.uid,
        title = newCatch.title,
        description = newCatch.description ?: "",
        date = newCatch.date,
        time = newCatch.time,
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