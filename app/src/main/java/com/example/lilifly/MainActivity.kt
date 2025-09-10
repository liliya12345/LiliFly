package com.example.lilifly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainActivity : AppCompatActivity() {

    private val clientId = "87f8307bb500473c95c72766f33dadd6"
    private val redirectUri = "com.example.lilifly://callback"
    private val requestCode = 1337
    private var spotifyAppRemote: SpotifyAppRemote? = null
   private var beaver= "BQCnaJS18A3ToMuxX0g8UvrGxyhHjxysF_71krQGYUQpaezZPYwnnxtAqptjQrHiEbSjvx3bQMDY4UBIUJwQD3lR7vOykqsRl5d1Jf0Ed77mTrDG2xSLwP7pAKYXAoU6Chd_6RwJ1BY"
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestQueue = Volley.newRequestQueue(this)
        startAuth()
    }

    private fun startAuth() {
        val request = AuthorizationRequest.Builder(
            clientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri
        )
            .setScopes(arrayOf("streaming"))
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
                    Log.d("MainActivity", "Success! Token: ${beaver?.take(10)}...")
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
                playMusic()

            }

            override fun onFailure(throwable: Throwable) {
                Log.e("MainActivity", "Connection failed: ${throwable.message}")
            }
        })
    }
    private fun getArtistInfo() {

            val url = "https://api.spotify.com/v1/artists/4Z8W4fKeB5YxbusRsdQVPb"

            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    Log.d("SpotifyAPI", "Response: ${response.toString()}")
                    // Обрабатываем данные артиста
                    val artistName = response.getString("name")
                    val followers = response.getJSONObject("followers").getInt("total")
                    val popularity = response.getInt("popularity")

                    Log.d("SpotifyAPI", "Artist: $artistName")
                    Log.d("SpotifyAPI", "Followers: $followers")
                    Log.d("SpotifyAPI", "Popularity: $popularity")
                },
                { error ->
                    Log.e("SpotifyAPI", "Error: ${error.message}")
                    if (error.networkResponse != null) {
                        Log.e("SpotifyAPI", "Status code: ${error.networkResponse.statusCode}")
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

    private fun playMusic() {
        spotifyAppRemote?.let { appRemote ->
            // Play a playlist
            val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
            appRemote.playerApi.play(playlistURI)

            // Subscribe to player state
            appRemote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                val track: Track = playerState.track
                Log.d("MainActivity", "${track.name} by ${track.artist.name}")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            Log.d("MainActivity", "Disconnected from Spotify")
        }
    }
  fun sendRequest() {



}
}