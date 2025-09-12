package com.example.lilifly


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val id: String,
    val name: String,
    val releaseDate: String = "",
    val imageUrl: String = ""
) : Parcelable

