package com.example.lilifly

import User
import android.os.Bundle
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
import com.example.lilifly.databinding.Fragment2Binding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote


class TopFragment : Fragment(), TopAdapter.Listener {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var user: User
    private lateinit var binding: Fragment2Binding
    private lateinit var requestQueue: RequestQueue
    private lateinit var beaver: String
    val trackList = mutableListOf<Track>()
    private lateinit var viewModel: DataModel
    var isPlaying: Boolean = true


//    val  beaver= arguments?.getString("beaver")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = Fragment2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        var viewModel: DataModel
        requestQueue = Volley.newRequestQueue(requireContext())
        binding.rvTop.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]

        val id = viewModel.artistData.value?.id.toString()
        val token = viewModel.data.value
        beaver=token.toString()


        // Получаем artistId из аргументов или используем дефолтный

        getTrackInfo(id)




    }

    private fun getTrackInfo(artistId: String) {
        // Add market parameter which is required for top-tracks endpoint
        val url = "https://api.spotify.com/v1/artists/$artistId/top-tracks?market=US"

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.i("SpotifyTrack", "Track Response: ${response.toString()}")

                try {
                    val itemsArray = response.getJSONArray("tracks")
                    trackList.clear() // Clear previous data

                    for (i in 0 until itemsArray.length()) {
                        val track = itemsArray.getJSONObject(i)
                        val name = track.getString("name")
                        val id = track.getString("id")

                        // Get album info for release date and images
                        val album = track.getJSONObject("album")
                        val releaseDate = album.getString("release_date")

                        // Get album images
                        var imageUrl = ""
                        val imagesArray = album.getJSONArray("images")
                        if (imagesArray.length() > 0) {
                            val firstImage = imagesArray.getJSONObject(0)
                            imageUrl = firstImage.getString("url")
                        }

                        trackList.add(Track(id, name, releaseDate, imageUrl))
                        Log.i("TrackList", "Added track: $name")
                    }

                    Log.i("TrackList", "Total tracks: ${trackList.size}")

                    // Update UI on main thread
                    requireActivity().runOnUiThread {
                        val adapter = TopAdapter(trackList, this@TopFragment)
                        binding.rvTop.adapter = adapter
                    }

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
                headers["Authorization"] = "Bearer $beaver"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    override fun onClick(track: Track) {
        Log.i("Music", "hello")
        playMusic(track)

    }
    override fun onPause(track: Track) {
        Log.i("Music", "hello")
        spotifyAppRemote?.let { appRemote ->
            val playlistURI = "spotify:track:${track.id}"
            appRemote.playerApi.pause()

        }


    }

    override fun onFavorite(track: Track) {

        viewModel.addToFavorites(track)
        var value = viewModel.favoriteTrackIds.value

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
                    Log.d("TopFragment", "Connected to Spotify App Remote!")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("TopFragment", "Connection failed: ${throwable.message}")
                }
            })
    }

    private fun playMusic(track: Track) {
        spotifyAppRemote?.let { appRemote ->
            val playlistURI = "spotify:track:${track.id}"
                appRemote.playerApi.play(playlistURI)



        }


    }




}