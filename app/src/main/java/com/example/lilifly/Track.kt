package com.example.lilifly


import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
@IgnoreExtraProperties
@Parcelize
data class Track(
    var id: String,
    var name: String,
    var releaseDate: String = "",
    var imageUrl: String = ""
) : Parcelable{
// Пустой конструктор для Firebase
constructor() : this("", "", "", "")
}
