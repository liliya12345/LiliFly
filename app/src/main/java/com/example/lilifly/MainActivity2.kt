package com.example.lilifly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.lilifly.databinding.ActivityMain3Binding
import com.example.lilifly.databinding.ActivityMainBinding
import kotlin.text.replace

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val artist = intent.getParcelableExtra<Artist>("artist")
        val beaver = intent.getStringExtra("beaver")

//
        if (artist != null) {
            Log.i("artist", "onCreate: ${artist.name}")
            Toast.makeText(this, "Artist: ${artist.name}", Toast.LENGTH_LONG).show()

            binding.artistName.text = artist.name
            binding.followersCount.text = artist.followers.toString()
            binding.popularityScore.text = artist.popularity.toString()
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
            mBundle.putParcelable("artist", artist)
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
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
        binding.topBtn.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, TopFragment())
                .addToBackStack("top_fragment")
                .commit()

        }


    }


}