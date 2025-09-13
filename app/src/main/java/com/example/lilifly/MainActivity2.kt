package com.example.lilifly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.lilifly.databinding.ActivityMain3Binding

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain3Binding

    private lateinit var viewModel: DataModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val artist = intent.getParcelableExtra<Artist>("artist")
        val beaver = intent.getStringExtra("beaver")
        viewModel = ViewModelProvider(this)[DataModel::class.java]
        viewModel.setData(beaver.toString())
        viewModel.setArtist(artist)

//
        if (artist != null) {
            Log.i("artist", "onCreate: ${artist.name}")
            Toast.makeText(this, "Artist: ${artist.name}", Toast.LENGTH_LONG).show()

            binding.artistName.text = artist.name
            binding.followersCount?.text = artist.followers.toString()
            binding.popularityScore?.text = artist.popularity.toString()
            if (artist.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(artist.imageUrl)
                    .into(binding.artistImage)
            }

        } else {
            Log.e("artist", "onCreate: Artist is null")
            Toast.makeText(this, "Artist data not found", Toast.LENGTH_LONG).show()
            finish() // Закрываем активити, если данные не получены
        }

            var frag = AlbumFragment()
            val mBundle = Bundle()
//            mBundle.putParcelable("artist", artist)
            mBundle.putString("beaver", beaver)
            frag.arguments = mBundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, frag)
                .addToBackStack("album_fragment")
                .commit()

        binding.homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.albBtn.setOnClickListener {
//            val intent = Intent(this, MainActivity2::class.java)
//            startActivity(intent)
            val fragment = AlbumFragment().apply {
                arguments = Bundle().apply {
                    putString("beaver", beaver)
                    // Don't need to pass artist as it's in ViewModel
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit() // Removed addToBackStack to avoid fragment stacking issues
        }



        binding.topBtn.setOnClickListener {
            val fragment = TopFragment().apply {
                arguments = Bundle().apply {
                    putString("beaver",beaver)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack("top_fragment") // Add to back stack only if you want back navigation
                .commit()

        }


    }


}