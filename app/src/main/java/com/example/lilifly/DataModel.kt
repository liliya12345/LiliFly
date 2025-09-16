package com.example.lilifly

import User
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataModel : ViewModel() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson
    private lateinit var mDataBase: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    val data = MutableLiveData<String>()
    val artistData = MutableLiveData<Artist>()
    val userData = MutableLiveData<User>()
    val trackData = MutableLiveData<Track>()

    // Список ID избранных треков
    val favoriteTrackIds = MutableLiveData<MutableSet<Track>>(mutableSetOf())

    init {
        // Инициализируем базу данных при создании ViewModel
        initializeDatabase()
    }

    private fun initializeDatabase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val databaseUrl = "https://lilifly-c4daf-default-rtdb.europe-west1.firebasedatabase.app"
            mDataBase = FirebaseDatabase.getInstance(databaseUrl)

                .getReference("users")
                .child(userId)
                .child("favorites")

            // Загружаем избранные треки из Firebase при инициализации
            loadFavoritesFromFirebase()
        } else {
            Log.e("DataModel", "User not authenticated, using local storage only")
        }
    }

    private fun loadFavoritesFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {

            val databaseUrl = "https://lilifly-c4daf-default-rtdb.europe-west1.firebasedatabase.app"
            mDataBase = FirebaseDatabase.getInstance(databaseUrl)

                .getReference("users")
                .child(userId)
                .child("favorites")

            mDataBase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorites = mutableSetOf<Track>()
                    for (childSnapshot in snapshot.children) {
                        val track = childSnapshot.getValue(Track::class.java)
                        track?.let { favorites.add(it) }
                    }
                    favoriteTrackIds.value = favorites
                    Log.i("DataModel", "Favorites loaded from Firebase: ${favorites.size} tracks")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DataModel", "Failed to load favorites from Firebase: ${error.message}")
                }
            })
        }
    }

    // Добавить трек в фавориты
    fun addToFavorites(track: Track) {
        val currentFavorites = favoriteTrackIds.value ?: mutableSetOf()

        // Проверяем, нет ли уже этого трека в избранном
        if (currentFavorites.any { it.id == track.id }) {
            Log.d("DataModel", "Track already in favorites: $track")
            return
        }

        currentFavorites.add(track)
        favoriteTrackIds.value = currentFavorites

        // Сохраняем в Firebase
        saveTrackToFirebase(track)

        Log.d("DataModel", "Track added to favorites: $track")
    }

    private fun saveTrackToFirebase(track: Track) {
        try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val databaseUrl = "https://lilifly-c4daf-default-rtdb.europe-west1.firebasedatabase.app"
                mDataBase = FirebaseDatabase.getInstance(databaseUrl)
                    .getReference("users")
                    .child(userId)
                    .child("favorites")

                // Сохраняем трек с его ID в качестве ключа
                mDataBase.child(track.id).setValue(track)
                    .addOnSuccessListener {
                        Log.i("DataModel", "Track saved to Firebase: $track")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DataModel", "Failed to save track to Firebase: ${e.message}")
                        // Если не удалось сохранить в Firebase, удаляем из локального списка
                        removeTrackFromLocalFavorites(track.id)
                    }
            } else {
                Log.e("DataModel", "User not authenticated, cannot save to Firebase")
            }
        } catch (e: Exception) {
            Log.e("DataModel", "Error saving to Firebase: ${e.message}")
            removeTrackFromLocalFavorites(track.id)
        }
    }

    fun deleteFromFavorite(track: Track) {

        val userId = auth.currentUser?.uid ?: return
        val currentFavorites = favoriteTrackIds.value ?: mutableSetOf()
        currentFavorites.removeAll { it.id == track.id}
        favoriteTrackIds.value = currentFavorites
        val databaseUrl = "https://lilifly-c4daf-default-rtdb.europe-west1.firebasedatabase.app"
        val database = FirebaseDatabase.getInstance(databaseUrl)
        val favoritesRef = database.getReference("users").child(userId).child("favorites")

        favoritesRef.child(track.id).removeValue()
            .addOnSuccessListener {
                Log.i("DataModel", "✅ Track removed from Firebase: $track")
                // Обновляем локальный список
                removeFromLocalFavorites(track.id)
                loadFavoritesFromFirebase()
            }
            .addOnFailureListener { e ->
                Log.e("DataModel", "❌ Failed to remove: ${e.message}")
            }
    }

    private fun removeFromLocalFavorites(trackId: String) {
        val currentFavorites = favoriteTrackIds.value ?: mutableSetOf()
        currentFavorites.removeAll { it.id == trackId }
        favoriteTrackIds.value = currentFavorites

    }

    private fun deleteTrackFromFirebase(trackId: String) {
        try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                mDataBase = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("favorites")

                mDataBase.child(trackId).removeValue()
                    .addOnSuccessListener {
                        Log.i("DataModel", "Track removed from Firebase: $trackId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DataModel", "Failed to remove track from Firebase: ${e.message}")
                    }
            }
        } catch (e: Exception) {
            Log.e("DataModel", "Error removing from Firebase: ${e.message}")
        }
    }

    private fun removeTrackFromLocalFavorites(trackId: String) {
        val currentFavorites = favoriteTrackIds.value ?: mutableSetOf()
        currentFavorites.removeAll { it.id == trackId }
        favoriteTrackIds.value = currentFavorites
        loadFavoritesFromFirebase()
    }

    // Обновляем базу данных при изменении аутентификации пользователя
    fun updateDatabaseReference() {
        initializeDatabase()
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