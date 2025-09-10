package com.example.lilifly

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val artist = intent.getParcelableExtra<Artist>("artist")

        if (artist != null) {
            Log.i("artist", "onCreate: ${artist.name}")
            Toast.makeText(this, "Artist: ${artist.name}", Toast.LENGTH_LONG).show()

            // Находим View элементы и устанавливаем значения
            val artistNameTextView = findViewById<TextView>(R.id.artistName)
            val artistFollowersTextView = findViewById<TextView>(R.id.artistFollowers)
            val artistPopularityTextView = findViewById<TextView>(R.id.artistPopularity)

            artistNameTextView.text = artist.name
            artistFollowersTextView.text = "Followers: ${artist.followers}"
            artistPopularityTextView.text = "Popularity: ${artist.popularity}"

        } else {
            Log.e("artist", "onCreate: Artist is null")
            Toast.makeText(this, "Artist data not found", Toast.LENGTH_LONG).show()
            finish() // Закрываем активити, если данные не получены
        }
    }
}