package itb.ac.id.purrytify.ui.library

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel

class LibraryPagerAdapter(fragment: Fragment, private val onPlay: () -> Unit, private val songPlayerViewModel: SongPlayerViewModel) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllSongsFragment(songPlayerViewModel, onPlay)
            1 -> LikedSongsFragment(songPlayerViewModel, onPlay)
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}
