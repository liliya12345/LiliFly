package com.example.lilifly


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val name: String,
    val totalTracks: String = "",
    val releaseDate: String = "",
    val imageUrl: String = ""
) : Parcelable

