package itb.ac.id.purrytify.ui.library

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.ui.adapter.SongAdapter
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel

class AllSongsFragment(private val songPlayerViewModel: SongPlayerViewModel, private val onPlay: () -> Unit) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private val viewModel: LibraryViewModel by viewModels({ requireParentFragment() })
//    private val addSongViewModel: AddSongViewModel by viewModels({ requireParentFragment() })
//    private val songPlayerViewModel: SongPlayerViewModel by viewModels({ requireParentFragment() })
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        emptyStateTextView = view.findViewById(R.id.tvEmptyState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter (
            onSongClick = { song ->
                songPlayerViewModel.playSong(song)
                onPlay() // harusnya sekali
            },
            onAddToQueueClick = { song ->
                // Add the song to the queue
                songPlayerViewModel.addQueue(song)
                Log.d("AllSongsFragment", "Added ${song.title} to queue.")
            })

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            if (songs.isNotEmpty()) {
                songAdapter.submitList(songs)
                recyclerView.visibility = View.VISIBLE
                emptyStateTextView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                emptyStateTextView.visibility = View.VISIBLE
            }
        }
    }
}