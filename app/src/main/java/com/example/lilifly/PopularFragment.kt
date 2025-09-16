package com.example.lilifly

import ArtistAdapter
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.lilifly.databinding.Fragment1Binding
import com.example.lilifly.databinding.FragmentPopularBinding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track

class PopularFragment : Fragment(), ArtistAdapter.Listener {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var binding: FragmentPopularBinding
    private lateinit var sharedPreferences: SharedPreferences
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
        binding = FragmentPopularBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        var viewModel: DataModel
        requestQueue = Volley.newRequestQueue(requireContext())
        binding.rvArtist.layoutManager = LinearLayoutManager(requireContext())
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
        }
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", MODE_PRIVATE)

        viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]

        val id = viewModel.artistData.value?.id.toString()
        val token = viewModel.data.value
        beaver = token.toString()
        val viewModel = ViewModelProvider(this)[DataModel::class.java]

        getArtistInfo()
        binding.btn.setOnClickListener {
            var search = binding.edText.text
            searchByArtist(search.toString())

        }
        setupNavigation()


        // Получаем artistId из аргументов или используем дефолтный

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
                    val artistsArray = artists.getJSONArray("items")
                    for (i in 0 until artistsArray.length()) {
                        val artist = artistsArray.getJSONObject(i)
                        val id = artist.getString("id")
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
                        listArtist.add(Artist(id, artistName, followers, popularity, imageUrl))
                    }

                    // Устанавливаем адаптер в главном потоке

                    binding.rvArtist.adapter = ArtistAdapter(listArtist, this@PopularFragment)
                    Log.d("RecyclerView", "Adapter set with ${listArtist.size} items")


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
                val beaver = sharedPreferences.getString("token", "")
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
                        val id = artist.getString("id")
                        val followers = artist.getJSONObject("followers").getInt("total")
                        val popularity = artist.getInt("popularity")

                        // Получаем изображение
                        var imageUrl = ""
                        val imagesArray = artist.getJSONArray("images")
                        if (imagesArray.length() > 0) {
                            val firstImage = imagesArray.getJSONObject(0)
                            imageUrl = firstImage.getString("url")
                        }
                        listArtist.add(Artist(id, artistName, followers, popularity, imageUrl))
                    }

                    // Устанавливаем адаптер в главном потоке

                    binding.rvArtist.adapter = ArtistAdapter(listArtist, this@PopularFragment)
                    Log.d("RecyclerView", "Adapter set with ${listArtist.size} items")


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

                viewModel = ViewModelProvider(requireActivity())[DataModel::class.java]
                val token = viewModel.data.value
                val beaver = sharedPreferences.getString("token", "")
                headers["Authorization"] = "Bearer $beaver"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun setupNavigation() {
        binding.userBtn?.setOnClickListener () {

//                runNotify(this)
            val controller = findNavController()
            controller.navigate(R.id.userFragment)
        }


    }
    override fun onClick(artist: Artist) {
        Toast.makeText(requireContext(), "Artist: ${artist.name}", Toast.LENGTH_SHORT).show()
        viewModel.setArtist(artist)

        val controller = findNavController()
        controller.navigate(R.id.albumFragment)


//        val intent = Intent(requireContext(), MainActivity2::class.java)
//        intent.putExtra("artist", artist)
//        intent.putExtra("beaver", beaver)
//        startActivity(intent)

    }
}