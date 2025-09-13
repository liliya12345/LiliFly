package com.example.lilifly


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    var id: String,
    var name: String,
    var releaseDate: String = "",
    var imageUrl: String = ""
) : Parcelable
