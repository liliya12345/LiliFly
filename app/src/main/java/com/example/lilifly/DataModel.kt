package com.example.lilifly

import User
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataModel : ViewModel() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson

    val data = MutableLiveData<String>()
    val artistData = MutableLiveData<Artist>()
    val userData = MutableLiveData<User>()
    val trackData = MutableLiveData<Track>()


    // Список ID избранных треков
    val favoriteTrackIds = MutableLiveData<MutableSet<Track>>(mutableSetOf())


    // Добавить трек в фавориты
    fun addToFavorites(track: Track) {

        val currentFavorites = favoriteTrackIds.value ?: mutableSetOf()

        currentFavorites.add(track)

        favoriteTrackIds.value = currentFavorites

        Log.d("DataModel", "Track added to favorites: $track")

    }






    fun setData(value: String) {
        data.value = value
    }

    fun setArtist(artist: Artist?) {
        artistData.value = artist
    }

    fun setUser(user: User) {
        userData.value = user
    }

    fun setTrack(track: Track) {
        trackData.value = track
    }
}