package com.mobileprism.fishing.domain.entity.content

import android.os.Parcelable
import androidx.room.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.model.datasource.room.Converters
import com.mobileprism.fishing.model.entity.FishingWeather
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "catches",
    foreignKeys = [ForeignKey(entity = UserMapMarker::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("markerId"),
        onDelete = ForeignKey.CASCADE)]
)
data class UserCatch(
    @PrimaryKey
    var id: String = "",
    var markerId: String = "",
    var userId: String = "",
    var description: String = "",
    @TypeConverters(Converters::class)
    var note: Note = Note(),
    var date: Long = 0,
    var dateOfCreation: Long = 0,
    var fishType: String = "",
    var fishAmount: Int = 0,
    var fishWeight: Double = 0.0,
    var fishingRodType: String = "",
    var fishingBait: String = "",
    var fishingLure: String = "",
    var placeTitle: String = "",
    @JvmField
    var isPublic: Boolean = false,
    @Ignore
    var downloadPhotoLinks: List<String> = listOf(),
    var weather: FishingWeather = FishingWeather.SUN,
    var weatherTemperature: Float = 0.0f,
    var weatherWindSpeed: Float = 0.0f,
    var weatherWindDeg: Int = 0,
    var weatherPressure: Int = 0,
    var weatherMoonPhase: Float = 0.0f
) : Parcelable