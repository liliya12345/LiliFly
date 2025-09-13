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
    val favoriteTrackIds = MutableLiveData<MutableSet<String>>(mutableSetOf())

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        gson = Gson()
        loadFavorites()
    }

    // Добавить трек в фавориты
    fun addToFavorites(trackId: String) {
        val currentFavorites = favoriteTrackIds.value ?: mutableSetOf()
        if (currentFavorites.add(trackId)) {
            favoriteTrackIds.value = currentFavorites
            saveFavorites()
            Log.d("DataModel", "Track added to favorites: $trackId")
        }
    }

    // Удалить трек из фаворитов
    fun removeFromFavorites(trackId: String) {
        val currentFavorites = favoriteTrackIds.value ?: mutableSetOf()
        if (currentFavorites.remove(trackId)) {
            favoriteTrackIds.value = currentFavorites
            saveFavorites()
            Log.d("DataModel", "Track removed from favorites: $trackId")
        }
    }

    // Проверить, есть ли трек в фаворитах
    fun isFavorite(trackId: String): Boolean {
        return favoriteTrackIds.value?.contains(trackId) ?: false
    }

    // Сохранить избранное в SharedPreferences
    private fun saveFavorites() {
        val favoritesJson = gson.toJson(favoriteTrackIds.value)
        sharedPreferences.edit().putString("favorite_tracks", favoritesJson).apply()
    }

    // Загрузить избранное из SharedPreferences
    private fun loadFavorites() {
        val favoritesJson = sharedPreferences.getString("favorite_tracks", null)
        if (favoritesJson != null) {
            val type = object : TypeToken<MutableSet<String>>() {}.type
            val favorites = gson.fromJson<MutableSet<String>>(favoritesJson, type)
            favoriteTrackIds.value = favorites ?: mutableSetOf()
            Log.d("DataModel", "Loaded favorites: ${favoriteTrackIds.value?.size} tracks")
        }
    }

    // Очистить избранное
    fun clearFavorites() {
        favoriteTrackIds.value = mutableSetOf()
        sharedPreferences.edit().remove("favorite_tracks").apply()
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