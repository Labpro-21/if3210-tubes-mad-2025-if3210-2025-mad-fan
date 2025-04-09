package itb.ac.id.purrytify.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.PurrytifyDatabase

class LibraryFragment : Fragment() {

    private lateinit var viewModel: LibraryViewModel
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

        // Inisialisasi database
        val db = Room.databaseBuilder(
            requireContext(),
            PurrytifyDatabase::class.java,
            "purrytify-database"
        ).build()

        // Create the ViewModel
        val factory = LibraryViewModelFactory(db.songDao(), db.likedSongDao())
        viewModel = ViewModelProvider(this, factory)[LibraryViewModel::class.java]

        // Initialize views
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        setupViewPager()
    }

    private fun setupViewPager() {
        val pagerAdapter = LibraryPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "All"
                1 -> tab.text = "Liked"
            }
        }.attach()
    }
}