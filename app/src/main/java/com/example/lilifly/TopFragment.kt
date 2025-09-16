package com.example.lilifly

import User
import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.lilifly.databinding.Fragment2Binding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote


class TopFragment : Fragment(), TopAdapter.Listener {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var user: User
    private lateinit var binding: Fragment2Binding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: RequestQueue
    private lateinit var beaver: String
    val trackList = mutableListOf<Track>()
    var followers: Int =0
    private lateinit var viewModel: DataModel
    var isPlaying: Boolean = true
    val CHANNEL_ID = "my_channel_id"
    val CHANNEL_NAME = "My Notifications"
    val CHANNEL_DESCRIPTION = "Notifications from my app"
    val NOTIFICATION_ID = 2


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
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", MODE_PRIVATE)
        viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]

        val id = viewModel.artistData.value?.id.toString()
        val artist = viewModel.artistData.value
        followers = viewModel.artistData.value?.followers?.toInt() ?: 0
        val token = viewModel.data.value
        beaver = token.toString()

        if (artist != null) {
            Log.i("artist", "onCreate: ${artist.name}")
            Toast.makeText(requireContext(), "Artist: ${artist.name}", Toast.LENGTH_LONG).show()

            binding.artistName.text = artist.name
            binding.followersCount?.text = artist.followers.toString()
            binding.popularityScore?.text = artist.popularity.toString()
            if (artist.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(artist.imageUrl)
                    .into(binding.artistImage)
            }
        }


        getTrackInfo(id)


    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Lilifly Notifications"
            val importance =
                NotificationManager.IMPORTANCE_HIGH // Измените на HIGH для лучшей видимости

            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = CHANNEL_DESCRIPTION
                // Дополнительные настройки
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400)
            }
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun runNotify(context: Context) {
        // Проверяем разрешение
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Если разрешения нет, просто выходим
                Log.d("Notification", "Notification permission not granted")
                return
            }
        }
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ava99)
            .setContentTitle("Super artist with ${followers} followers")
            .setContentText("Track added to favorites!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())

        try {
            with(NotificationManagerCompat.from(requireContext())) {
                notify(NOTIFICATION_ID, builder.build())
                Log.d("Notification", "Notification shown successfully")
            }
        } catch (e: Exception) {
            Log.e("Notification", "Failed to show notification: ${e.message}")
            // Показываем Toast если уведомление не работает
            Toast.makeText(requireContext(), "Track added to favorites!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
                val beaver = sharedPreferences.getString("token", "")
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
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", MODE_PRIVATE)

        viewModel.addToFavorites(track)
        var value = viewModel.favoriteTrackIds.value
        val editor = sharedPreferences.edit()
        editor.putString("melodyId", track.id)
        editor.putString("melodyName", track.name)
        editor.putString("melodyRelease", track.releaseDate)
        editor.putString("melodyImg", track.imageUrl)
        editor.apply()

        Toast.makeText(requireContext(), "Added to favorite: ${track.name}", Toast.LENGTH_SHORT)
            .show()

        if(followers>2000000){
        runNotify(requireContext())
    }}


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