package com.example.lilifly


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataModel : ViewModel() {
    val data = MutableLiveData<String>()
    val artistData = MutableLiveData<Artist>()

    fun setData(value: String) {
        data.value = value
    }

    fun setArtist(artist: Artist?) {
        artistData.value = artist
    }
}