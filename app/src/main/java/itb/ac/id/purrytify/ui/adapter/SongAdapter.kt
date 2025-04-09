package itb.ac.id.purrytify.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.entity.Song

class SongAdapter(private val onSongClick: (Song) -> Unit) :
    ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgSongCover: ImageView = itemView.findViewById(R.id.imgSongCover)
        private val tvSongTitle: TextView = itemView.findViewById(R.id.tvSongTitle)
        private val tvArtistName: TextView = itemView.findViewById(R.id.tvArtistName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSongClick(getItem(position))
                }
            }
        }

        fun bind(song: Song) {
            tvSongTitle.text = song.title
            tvArtistName.text = song.artist

            // Load image using Glide
            Glide.with(itemView.context)
                .load(song.imagePath)
                .placeholder(R.drawable.cover_jazz)
                .error(R.drawable.cover_jazz)
                .into(imgSongCover)
        }
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.songId == newItem.songId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}