package com.nasen.railwaywatcher.ui

import android.os.Bundle
import android.view.*
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.vvalidator.form.FormResult
import com.nasen.railwaywatcher.Global
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.SplitPointOutOfBounds
import com.nasen.railwaywatcher.type.Railway
import kotlinx.android.synthetic.main.fragment_subrange_list.*

class NewSubRangeFragment : Fragment(), NewSubRangeDialogFragment.Listener {
    private val args: NewSubRangeFragmentArgs by navArgs()
    private var startPos: Int = 0
    private var endPos: Int = 0
    lateinit var startPlace: String
    lateinit var endPlace: String
    lateinit var subRangeAdapter: SubRangeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        startPos = args.startPos
        endPos = args.endPos
        startPlace = args.startPlace
        endPlace = args.endPlace
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_subrange, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.new_subrange -> {
            insertNewSplitPoint()
            true
        }
        R.id.subrange_confirm -> {
            Global.add(
                Railway(
                    startPlace,
                    endPlace,
                    startPos,
                    endPos,
                    subRangeAdapter.subRanges
                )
            )
            Navigation.findNavController(requireView())
                .navigate(NewSubRangeFragmentDirections.actionNewSubRangeFragmentToHomeFragment())
            true
        }
        else -> Navigation.findNavController(requireView()).navigateUp()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_subrange_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val manager = LinearLayoutManager(requireContext())
        subRangeAdapter = SubRangeAdapter(startPos, endPos)
        val simpleItemTouchCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                if (viewHolder.adapterPosition == recyclerView.size - 1) return 0
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                subRangeAdapter.removeRange(pos)
            }

        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        subRangeView.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = subRangeAdapter
        }
        itemTouchHelper.attachToRecyclerView(subRangeView)
    }

    private fun insertNewSplitPoint() {
        val fragment = NewSubRangeDialogFragment()
        fragment.setTargetFragment(this, 0)
        fragment.show(parentFragmentManager, "new_subrange")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, result: FormResult) {
        when (dialog) {
            is NewSubRangeDialogFragment -> {
                val pos = result["newPosKm"]!!.asInt()!! * 1000 + result["newPosM"]!!.asInt()!!
                requireActivity().runOnUiThread {
                    try {
                        subRangeAdapter.addSplitPoint(pos)
                    } catch (ignored: SplitPointOutOfBounds) {
                    }
                }
            }
            else -> {
            }
        }
    }
}