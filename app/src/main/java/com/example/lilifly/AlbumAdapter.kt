package com.example.lilifly

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lilifly.databinding.AlbumItemBinding

class AlbumAdapter(private val list: List<Album>, private val listener: Listener) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(val binding: AlbumItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album, listener: Listener) {
            binding.albumName.text = album.name
            binding.totalTracks.text = "${album.totalTracks} треков"
            binding.releaseDate.text = album.releaseDate

            // Загрузка изображения
            Glide.with(binding.albumImg.context)
                .load(album.imageUrl)
                .into(binding.albumImg)

            // Обработка клика на всю карточку
            binding.root.setOnClickListener {
                listener.onClick(album)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = AlbumItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = list[position]
        holder.bind(album, listener)
    }

    override fun getItemCount(): Int = list.size

    interface Listener {
        fun onClick(album: Album)
    }
}