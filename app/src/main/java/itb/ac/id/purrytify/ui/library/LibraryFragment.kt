package itb.ac.id.purrytify.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.PurrytifyDatabase
import itb.ac.id.purrytify.ui.addsong.AddSongViewModel
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel

@AndroidEntryPoint
class LibraryFragment(private val songPlayerViewModel: SongPlayerViewModel, private val onPlay: () -> Unit) : Fragment() {
    private val viewModel: LibraryViewModel by viewModels()
//    private val addSongViewModel: AddSongViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views

        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        setupViewPager()
    }

    private fun setupViewPager() {
        val pagerAdapter = LibraryPagerAdapter(this, onPlay = onPlay, songPlayerViewModel)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "All"
                1 -> tab.text = "Liked"
                2 -> tab.text = "Downloaded"
                else -> throw IllegalArgumentException("Invalid position $position")
            }
        }.attach()
    }
}