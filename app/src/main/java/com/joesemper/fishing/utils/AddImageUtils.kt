package com.joesemper.fishing.utils

import java.text.SimpleDateFormat
import java.util.*

fun getTimeStamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())