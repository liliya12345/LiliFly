package com.example.lilifly


import ArtistAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lilifly.databinding.ActivityMainBinding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.json.JSONObject

class MainActivity : AppCompatActivity(), ArtistAdapter.Listener {
    private lateinit var binding: ActivityMainBinding
    private val clientId = "87f8307bb500473c95c72766f33dadd6"
    private val redirectUri = "com.example.lilifly://callback"
    private val requestCode = 1337
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var beaver = ""
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestQueue = Volley.newRequestQueue(this)
        binding.rvArtist.layoutManager = LinearLayoutManager(this)

        startAuth()

        binding.btn.setOnClickListener {
            var search = binding.edText.text
            searchByArtist(search.toString())

        }

    }


    private fun startAuth() {
        val request = AuthorizationRequest.Builder(
            clientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri
        )
            .setScopes(arrayOf("streaming", "user-read-private"))
            .setShowDialog(true)
            .build()

        try {
            AuthorizationClient.openLoginActivity(this, requestCode, request)
        } catch (e: Exception) {
            Log.e("MainActivity", "Auth failed: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == this.requestCode) {
            val response = AuthorizationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    beaver = response.accessToken
                    Log.d("MainActivity", "Success! Token: ${beaver.take(10)}...")
                    connectToSpotifyAppRemote()
                    getArtistInfo()
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e("MainActivity", "Error: ${response.error}")
                }

                else -> {
                    Log.d("MainActivity", "Auth cancelled")
                }
            }
        }
    }

    private fun connectToSpotifyAppRemote() {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("MainActivity", "Connected to Spotify App Remote!")
//                playMusic()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("MainActivity", "Connection failed: ${throwable.message}")
            }
        })
    }

    private fun searchByArtist(qwert: String) {
        val url = "https://api.spotify.com/v1/search?q=$qwert&type=artist"
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.i("SpotifyAPI", "Response: ${response.toString()}")

                try {
                    val listArtist = mutableListOf<Artist>()
                    val artists = response.getJSONObject("artists")
                    val artistsArray=artists.getJSONArray("items")
                    for (i in 0 until artistsArray.length()) {
                        val artist = artistsArray.getJSONObject(i)
                        val artistName = artist.getString("name")
                        val followers = artist.getJSONObject("followers").getInt("total")
                        val popularity = artist.getInt("popularity")

                        // Получаем изображение
                        var imageUrl = ""
                        val imagesArray = artist.getJSONArray("images")
                        if (imagesArray.length() > 0) {
                            val firstImage = imagesArray.getJSONObject(0)
                            imageUrl = firstImage.getString("url")
                        }
                        listArtist.add(Artist(artistName, followers, popularity, imageUrl))
                    }

                    // Устанавливаем адаптер в главном потоке
                    runOnUiThread {
                        binding.rvArtist.adapter = ArtistAdapter(listArtist, this@MainActivity)
                        Log.d("RecyclerView", "Adapter set with ${listArtist.size} items")
                    }

                } catch (e: Exception) {
                    Log.e("SpotifyAPI", "Error parsing JSON: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("SpotifyAPI", "Error: ${error.message}")
                error.networkResponse?.let {
                    Log.e("SpotifyAPI", "Status code: ${it.statusCode}")
                    Log.e("SpotifyAPI", "Response data: ${String(it.data)}")
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

    private fun getArtistInfo() {
        val artistIds =
            "2CIMQHirSU0MQqyYHq0eOx,57dN52uHvrHOxijzpIgu3E,1vCWHaC5f2uS3yhpwWbIA6,33qOK5uJ8AR2xuQQAhHump"
        val url = "https://api.spotify.com/v1/artists?ids=$artistIds"

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("SpotifyAPI", "Response: ${response.toString()}")

                try {
                    val listArtist = mutableListOf<Artist>()
                    val artistsArray = response.getJSONArray("artists")

                    for (i in 0 until artistsArray.length()) {
                        val artist = artistsArray.getJSONObject(i)
                        val artistName = artist.getString("name")
                        val followers = artist.getJSONObject("followers").getInt("total")
                        val popularity = artist.getInt("popularity")

                        // Получаем изображение
                        var imageUrl = ""
                        val imagesArray = artist.getJSONArray("images")
                        if (imagesArray.length() > 0) {
                            val firstImage = imagesArray.getJSONObject(0)
                            imageUrl = firstImage.getString("url")
                        }
                        listArtist.add(Artist(artistName, followers, popularity, imageUrl))
                    }

                    // Устанавливаем адаптер в главном потоке
                    runOnUiThread {
                        binding.rvArtist.adapter = ArtistAdapter(listArtist, this@MainActivity)
                        Log.d("RecyclerView", "Adapter set with ${listArtist.size} items")
                    }

                } catch (e: Exception) {
                    Log.e("SpotifyAPI", "Error parsing JSON: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("SpotifyAPI", "Error: ${error.message}")
                error.networkResponse?.let {
                    Log.e("SpotifyAPI", "Status code: ${it.statusCode}")
                    Log.e("SpotifyAPI", "Response data: ${String(it.data)}")
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

//    private fun playMusic() {
//        spotifyAppRemote?.let { appRemote ->
//            val playlistURI = "spotify:artist:45eNHdiiabvmbp4erw26rg"
//            appRemote.playerApi.play(playlistURI)
//
//            appRemote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
//                val track: Track = playerState.track
//                Log.d("MainActivity", "${track.name} by ${track.artist.name}")
//            }
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        spotifyAppRemote?.let {
//            SpotifyAppRemote.disconnect(it)
//            Log.d("MainActivity", "Disconnected from Spotify")
//        }
//    }

    override fun onClick(artist: Artist) {
        Toast.makeText(this, "Artist: ${artist.name}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity2::class.java)
        intent.putExtra("artist", artist)
        intent.putExtra("beaver",beaver)

        startActivity(intent)
    }
}