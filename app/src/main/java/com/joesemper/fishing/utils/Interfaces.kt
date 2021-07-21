package com.joesemper.fishing.utils

import com.joesemper.fishing.model.common.content.UserCatch

class Interfaces {
    interface OnCatchListItemClickListener {
        fun onItemClick(catch: UserCatch)
    }
}