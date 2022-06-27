package com.mobileprism.fishing.domain.entity.common

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mobileprism.fishing.model.datasource.room.Converters
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = "",
    val markerId: String = "",
    val title: String = "",
    val description: String = "",
    val dateCreated: Long = 0
) : Parcelable
