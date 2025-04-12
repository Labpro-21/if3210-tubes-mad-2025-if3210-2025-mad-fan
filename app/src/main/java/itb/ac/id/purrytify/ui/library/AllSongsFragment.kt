package itb.ac.id.purrytify.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.ui.adapter.SongAdapter
import itb.ac.id.purrytify.ui.addsong.AddSongViewModel
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
        songAdapter = SongAdapter { song ->
            songPlayerViewModel.playSong(song)
            onPlay()
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.allSongs.observe(viewLifecycleOwner) { songs ->
//            viewModel.loadAllSongs()
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