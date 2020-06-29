package com.nasen.railwaywatcher.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nasen.railwaywatcher.Global
import com.nasen.railwaywatcher.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    val railwayAdapter = RailwayAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Global.railways.observe(viewLifecycleOwner) {
            railwayAdapter.railways = it
        }
        fab.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_newRailwayFragment))
        val linearLayoutManager = LinearLayoutManager(requireContext())
        railwayRecycler.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = railwayAdapter
        }
        val simpleItemTouchCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                Global.remove(pos)
                railwayAdapter.notifyItemRemoved(pos)
                if (pos < railwayAdapter.railways.size) {
                    railwayAdapter.notifyItemChanged(pos)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(railwayRecycler)
        railwayRecycler.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
    }
}