import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lilifly.Artist
import com.example.lilifly.R
import com.example.lilifly.databinding.ArtistItemBinding

class ArtistAdapter(private val list: List<Artist>, var listener: Listener) :
    RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {


    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ArtistItemBinding.bind(itemView)
        fun bind(artist: Artist, listener: Listener)=with(binding){
            itemView.setOnClickListener {
                listener.onClick(artist)
            }
        }
        val imageView: ImageView = itemView.findViewById(R.id.im)
        val textView: TextView = itemView.findViewById(R.id.tvTitle)
        val cardView: CardView = itemView.findViewById(R.id.cardArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.artist_item, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = list[position]
        holder.bind(artist,listener)
        holder.textView.text = artist.name


        // Для загрузки изображения по URL используйте библиотеку типа Glide или Picasso
        // Пример с Glide:
        Glide.with(holder.imageView.context)
            .load(artist.imageUrl)
            .into(holder.imageView)

        // Временно используем заглушку
       holder.cardView.setOnClickListener {

       }
    }

    override fun getItemCount(): Int = list.size

    interface  Listener{
        fun onClick(artist:Artist)
    }
}