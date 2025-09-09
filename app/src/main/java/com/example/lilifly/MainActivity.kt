package com.example.lilifly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "App started")
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
                    val token = response.accessToken
                    Log.d("MainActivity", "Success! Token: ${token?.take(10)}...")
                    connectToSpotifyAppRemote()
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
}