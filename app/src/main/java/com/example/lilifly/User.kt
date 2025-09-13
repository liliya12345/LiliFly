import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val name: String,
    val trackList: List<String> = emptyList() // Измените на List<String> для ID треков
) : Parcelable