package itb.ac.id.purrytify.ui.library

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllSongsFragment()
            1 -> LikedSongsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}
