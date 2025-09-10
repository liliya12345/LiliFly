package com.example.lilifly


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val name: String,
    val followers: Int = 0,
    val popularity: Int = 0,
    val imageUrl: String = ""
) : Parcelable

