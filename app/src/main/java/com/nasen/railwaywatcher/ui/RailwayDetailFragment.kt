package com.nasen.railwaywatcher.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nasen.railwaywatcher.Global
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.type.Railway
import kotlinx.android.synthetic.main.fragment_railway_detail.*

class RailwayDetailFragment : Fragment() {
    private val args: RailwayDetailFragmentArgs by navArgs()
    lateinit var railway: Railway

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        railway = Global.get(args.which)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        viewpager.currentItem = 0
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.add_new_record -> {
            val action =
                RailwayDetailFragmentDirections.actionRailwayDetailFragmentToNewRecordFragment(args.which)
            findNavController().navigate(action)
            true
        }
        else -> findNavController().navigateUp()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_railway_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "${railway.from} - ${railway.to}"
        viewpager.adapter = RailwayDetailPagerAdapter(args.which, this)
        TabLayoutMediator(tabs, viewpager) { tab: TabLayout.Tab, i: Int ->
            when (i) {
                0 -> tab.text = getString(R.string.sub_ranges)
                1 -> tab.text = getString(R.string.reminder)
            }
            viewpager.setCurrentItem(i, true)
        }.attach()
        viewpager.setCurrentItem(0, false)
    }
}

class RailwayDetailPagerAdapter(private val idx: Int, val frag: Fragment) :
    FragmentStateAdapter(frag) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> RailwayDetailSubRangeFragment(frag, idx)
        1 -> RailwayDetailReminderFragment(idx)
        else -> RailwayDetailSubRangeFragment(frag, idx)
    }
}

