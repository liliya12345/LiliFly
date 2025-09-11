package com.example.lilifly

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lilifly.databinding.Fragment1Binding

class AlbumFragment : Fragment(),  AlbumAdapter.Listener{
    private lateinit var binding: Fragment1Binding
    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = Fragment1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestQueue = Volley.newRequestQueue(requireContext())
        binding.rvAlbum.layoutManager = LinearLayoutManager(requireContext())

        // Получаем artistId из аргументов или используем дефолтный
        val artistId = arguments?.getString("artistId") ?: "33qOK5uJ8AR2xuQQAhHump"
        getAlbumInfo(artistId)
    }

    private fun getAlbumInfo(artistId: String) {
        // ПРАВИЛЬНЫЙ URL для получения альбомов артиста
        val url = "https://api.spotify.com/v1/artists/$artistId/albums?include_groups=album,single&limit=20"

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.i("SpotifyAPI", "Album Response: ${response.toString()}")

                try {
                    val itemsArray = response.getJSONArray("items")
                    val listAlbum = mutableListOf<Album>()
                    for (i in 0 until itemsArray.length()) {
                        val album = itemsArray.getJSONObject(i)
                        val name = album.getString("name")
                        val releaseDate = album.getString("release_date")
                        val totalTracks = album.getInt("total_tracks") // Исправлено на getInt

                        // Получаем изображение альбома
                        var imageUrl = ""
                        val imagesArray = album.getJSONArray("images")
                        if (imagesArray.length() > 0) {
                            val firstImage = imagesArray.getJSONObject(0)
                            imageUrl = firstImage.getString("url")
                        }

                        listAlbum.add(Album(name, totalTracks.toString(), releaseDate, imageUrl))
                    }

                    // Устанавливаем адаптер в главном потоке
                    requireActivity().runOnUiThread {
                        val adapter = AlbumAdapter(listAlbum,this@AlbumFragment)
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
                val beaver = "BQCKWM7Q7JBNVAksCdAcqe3ziKltwW4epOJJSgtJB4hsE6Z_LzIddYRGF_VjLdMrw1RngzzT4MCZd3ElLadOR2C3GLx7iUNYXCdtTcn43ofKnZFS-E7--TnbnpNTdKf41KmlI7jYz28"
                headers["Authorization"] = "Bearer $beaver"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    override fun onClick(album: Album) {
        Log.i("Music", "hello")

    }

}