package com.example.lilifly

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.LoginFilter
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lilifly.databinding.FragmentUserBinding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

class UserFragment : Fragment(), Userdapter.Listener {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var binding: FragmentUserBinding
    private lateinit var requestQueue: RequestQueue
    private lateinit var viewModel: DataModel
    private val trackList = mutableListOf<Track>()
    private lateinit var track: Track
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()


    }

    override fun onPause() {
        super.onPause()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val id = sharedPreferences.getString("melodyId", null)
        val name = sharedPreferences.getString("melodyName", null)
        val date = sharedPreferences.getString("melodyRelease", null)
        val img = sharedPreferences.getString("melodyImg", null)

        // Проверяем, что все необходимые значения есть
        if (id != null && name != null && date != null && img != null) {
            // Создаем объект Track
            track = Track(
                id = id,
                name = name,
                releaseDate = date,
                imageUrl = img
            )
            viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]
            // Добавляем в избранное
            viewModel.addToFavorites(track)
            var value = viewModel.favoriteTrackIds.value
            Log.i("ViewModel", ""+value)
        }


        requestQueue = Volley.newRequestQueue(requireContext())
        binding.rvPlaylist.layoutManager = LinearLayoutManager(requireContext())

        // Получаем токен из ViewModel
        val token = "BQDbDHruwC4r7ARBeN8q04LPmVJTTVPUD-7R6VW3oNAL8NB-kQPAKCCfd9bTmxFo0docMJd1pDqd7_HhdiMk0ClxmRLs7D2O2lIwuWkmO7Vh0gvmDqG6gn1tQbynXqG0o2dNyoVNA1I"


        // Используем правильный ID трека (не artist ID)
        val trackId = "11dFghVXANMlKmJXsNCbNl" // Это ID трека
        // Инициализируем ViewModel
//        viewModel.initSharedPreferences(requireContext())

        getTrackInfo( token)
    }

    private fun getTrackInfo( token: String) {
        var tarckIdtarckId: String =""
        viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]
        var list = viewModel.favoriteTrackIds.value
        for (i in list!!){
            tarckIdtarckId = i.id
        }

        val url = "https://api.spotify.com/v1/tracks/$tarckIdtarckId"

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.i("SpotifyTrack", "Track Response: ${response.toString()}")

                try {
                    // Parse single track object
                    val name = response.getString("name")
                    val id = response.getString("id")

                    // Get album info for release date and images
                    val album = response.getJSONObject("album")
                    val releaseDate = album.getString("release_date")

                    // Get album images
                    var imageUrl = ""
                    val imagesArray = album.getJSONArray("images")
                    if (imagesArray.length() > 0) {
                        val firstImage = imagesArray.getJSONObject(0)
                        imageUrl = firstImage.getString("url")
                    }

                    // Get artists
                    val artistsArray = response.getJSONArray("artists")
                    val artistNames = mutableListOf<String>()
                    for (i in 0 until artistsArray.length()) {
                        val artist = artistsArray.getJSONObject(i)
                        artistNames.add(artist.getString("name"))
                    }

                    val artistName = artistNames.joinToString(", ")

                    // Create track object
                    val track = Track(id, name, releaseDate, imageUrl)
                    trackList.add(track)

                    // Сохраняем в ViewModel


                    Log.i("TrackList", "Added track: $name")

                    // Update UI
                    val adapter = Userdapter(trackList, this@UserFragment)
                    binding.rvPlaylist.adapter = adapter

                } catch (e: Exception) {
                    Log.e("SpotifyTracks", "Error parsing track JSON: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("SpotifyTracks", "Track Error: ${error.message}")
                error.networkResponse?.let {
                    Log.e("SpotifyTracks", "Status code: ${it.statusCode}")
                    Log.e("SpotifyTracks", "Response data: ${String(it.data)}")
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    override fun onClick(track: Track) {
        Log.i("Music", "Playing track: ${track.name}")
        playMusic(track)
    }

    override fun onPause(track: Track) {
        Log.i("Music", "Pausing track: ${track.name}")
        spotifyAppRemote?.playerApi?.pause()
    }

    override fun onFavorite(track: Track) {
        viewModel.addToFavorites(track)
        Toast.makeText(requireContext(), "Added to favorite: ${track.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        connectToSpotifyAppRemote()
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    private fun connectToSpotifyAppRemote() {
        val connectionParams = ConnectionParams.Builder("87f8307bb500473c95c72766f33dadd6")
            .setRedirectUri("com.example.lilifly://callback")
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            requireContext(), connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d("UserFragment", "Connected to Spotify App Remote!")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("UserFragment", "Connection failed: ${throwable.message}")
                }
            })
    }

    private fun playMusic(track: Track) {
        spotifyAppRemote?.let { appRemote ->
            val trackURI = "spotify:track:${track.id}"
            appRemote.playerApi.play(trackURI)
        }
    }
}