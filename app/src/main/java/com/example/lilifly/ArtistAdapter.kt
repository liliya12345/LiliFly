import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lilifly.Artist
import com.example.lilifly.R

class ArtistAdapter(private val list: List<Artist>) :
    RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.im)
        val textView: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.artist_item, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = list[position]
        holder.textView.text = artist.name

        // Для загрузки изображения по URL используйте библиотеку типа Glide или Picasso
        // Пример с Glide:
         Glide.with(holder.imageView.context)
             .load(artist.imageUrl)
             .into(holder.imageView)

        // Временно используем заглушку

    }

    override fun getItemCount(): Int = list.size
}