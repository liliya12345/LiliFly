package com.example.lilifly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lilifly.databinding.AlbumItemBinding
import com.example.lilifly.databinding.ArtistItemBinding

class AlbumAdapter(private val list: List<Album>) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {


    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AlbumItemBinding.bind(itemView)
        fun bind(albumA: Album, listener: Listener)=with(binding){
//            itemView.setOnClickListener {
//                listener.onClick(album)
//            }
        }
        val imageAlbum: ImageView = itemView.findViewById(R.id.album_img)
        val albumName: TextView = itemView.findViewById(R.id.album_name)
        val totalTracks: TextView = itemView.findViewById(R.id.totalTracks)
        val releaseDate: TextView = itemView.findViewById(R.id.releaseDate)
//        val cardAlbum: TextView = itemView.findViewById(R.id.cardAlbum)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.album_item, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = list[position]
//        holder.bind(album,listener)
        holder.albumName.text = album.name
        holder.totalTracks.text = album.totalTracks
        holder.releaseDate.text = album.releaseDate
//        holder.cardAlbum.setOnClickListener {
//
//        }


        // Для загрузки изображения по URL используйте библиотеку типа Glide или Picasso
        // Пример с Glide:
        Glide.with(holder.imageAlbum.context)
            .load(album.imageUrl)
            .into(holder.imageAlbum)



    }

    override fun getItemCount(): Int = list.size

    interface  Listener{
        fun onClick(album:Album)
    }
}