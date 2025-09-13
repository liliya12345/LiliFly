package com.example.lilifly

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lilifly.databinding.AlbumItemBinding
import com.example.lilifly.databinding.TopItemBinding
import com.example.lilifly.databinding.UserItemBinding

class Userdapter(private val list: List<Track>, private val listener: Listener) :
    RecyclerView.Adapter<Userdapter.UserViewHolder>() {

    class UserViewHolder(val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: Track, listener: Listener) {
            binding.trackName.text = track.name
            binding.releaseDate.text = track.releaseDate
            binding.btnPlay.setOnClickListener {
                listener.onClick(track)
            }

            // Обработка клика на кнопку pause
            binding.playStop.setOnClickListener {
                listener.onPause(track)
            }


            // Загрузка изображения
            Glide.with(binding.trackImg.context)
                .load(track.imageUrl)
                .into(binding.trackImg)

//            // Обработка клика на всю карточку
//            binding.root.setOnClickListener {
//                listener.onClick(track)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val track= list[position]
        holder.bind(track, listener)
    }

    override fun getItemCount(): Int = list.size

    interface Listener {
        fun onClick(track: Track)
        fun onPause(track: Track)
        fun onFavorite(track:Track)
    }
}