package com.example.lilifly

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lilifly.databinding.Fragment1Binding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track

class AlbumFragment : Fragment(), AlbumAdapter.Listener {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var binding: Fragment1Binding
    private lateinit var requestQueue: RequestQueue
    private lateinit var beaver: String
    val listAlbum = mutableListOf<Album>()
    private lateinit var viewModel: DataModel
    var isPlaying: Boolean = true


//    val  beaver= arguments?.getString("beaver")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = Fragment1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        var viewModel: DataModel
        requestQueue = Volley.newRequestQueue(requireContext())
        binding.rvAlbum.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]

        val id = viewModel.artistData.value?.id.toString()
        val token = viewModel.data.value
        beaver=token.toString()


        // Получаем artistId из аргументов или используем дефолтный

        getAlbumInfo(id)


    }

    private fun getAlbumInfo(artistId: String) {
        // ПРАВИЛЬНЫЙ URL для получения альбомов артиста
        val url =
            "https://api.spotify.com/v1/artists/$artistId/albums?include_groups=album,single&limit=20"

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.i("SpotifyAPI", "Album Response: ${response.toString()}")

                try {
                    val itemsArray = response.getJSONArray("items")

                    for (i in 0 until itemsArray.length()) {
                        val album = itemsArray.getJSONObject(i)
                        val name = album.getString("name")
                        val id = album.getString("id")
                        val releaseDate = album.getString("release_date")
                        val totalTracks = album.getInt("total_tracks") // Исправлено на getInt

                        // Получаем изображение альбома
                        var imageUrl = ""
                        val imagesArray = album.getJSONArray("images")
                        if (imagesArray.length() > 0) {
                            val firstImage = imagesArray.getJSONObject(0)
                            imageUrl = firstImage.getString("url")
                        }

                        listAlbum.add(
                            Album(
                                id,
                                name,
                                totalTracks.toString(),
                                releaseDate,
                                imageUrl
                            )
                        )
                    }

                    // Устанавливаем адаптер в главном потоке
                    requireActivity().runOnUiThread {
                        val adapter = AlbumAdapter(listAlbum, this@AlbumFragment)
                        binding.rvAlbum.adapter = adapter
                    }

                } catch (e: Exception) {
                    Log.e("SpotifyAPI", "Error parsing album JSON: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("SpotifyAPI", "Album Error: ${error.message}")
                error.networkResponse?.let {
                    Log.e("SpotifyAPI", "Status code: ${it.statusCode}")
                    Log.e("SpotifyAPI", "Response data: ${String(it.data)}")
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val beaver ="BQBpmCFF5fArL_B6SjxA_bsGQCZzva-tmA4EvLb7uSYfUPHJqUKZMXMIfVv4aYoi0c5JyCRDt3NirZ4MCMxO7RlKvH1Xrj8fQI6RvTaBJCA52ESSorlbHrylUNTxF-H6SA9vx_Jdgc8"
                headers["Authorization"] = "Bearer $beaver"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    override fun onClick(album: Album) {
        Log.i("Music", "hello")
        playMusic(album)

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
                    Log.d("AlbumFragment", "Connected to Spotify App Remote!")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("AlbumFragment", "Connection failed: ${throwable.message}")
                }
            })
    }

    private fun playMusic(album: Album) {
        spotifyAppRemote?.let { appRemote ->
            val playlistURI = "spotify:album:${album.id}"
            if (isPlaying) {
                appRemote.playerApi.play(playlistURI)
                isPlaying = false

            } else {
                appRemote.playerApi.pause()
                isPlaying = true
            }
        }


    }


}