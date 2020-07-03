package com.nasen.railwaywatcher.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.size
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.vvalidator.form
import com.afollestad.vvalidator.form.Form
import com.nasen.railwaywatcher.Global
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.SplitPointOutOfBounds
import com.nasen.railwaywatcher.type.DoubleRange
import com.nasen.railwaywatcher.type.Railway
import com.nasen.railwaywatcher.type.SingleRange
import kotlinx.android.synthetic.main.fragment_subrange_list.*

class NewSubRangeFragment : Fragment(), NewSubRangeDialogFragment.Listener {
    private val args: NewSubRangeFragmentArgs by navArgs()
    private var startPos: Int = 0
    private var endPos: Int = 0
    lateinit var name: String
    lateinit var subRangeAdapter: SubRangeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        startPos = args.startPos
        endPos = args.endPos
        name = args.name
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
                    name,
                    startPos,
                    endPos,
                    subRangeAdapter.subRanges.map {
                        when (it.single) {
                            true -> SingleRange(it.start, it.end, it.checkDay)
                            false -> DoubleRange(it.start, it.end, it.checkDay)
                        }
                    }
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

    override fun onDialogPositiveClick(dialog: DialogFragment, pos: Int) {
        when (dialog) {
            is NewSubRangeDialogFragment -> {
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

data class DRange(var start: Int, var end: Int, var checkDay: Int, var single: Boolean)

class SubRangeAdapter(start: Int, end: Int) :
    RecyclerView.Adapter<SubRangeAdapter.ViewHolder>() {
    val subRanges: MutableList<DRange> = mutableListOf(DRange(start, end, 30, true))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startText: TextView = itemView.findViewById(R.id.startPosText)
        val endText: TextView = itemView.findViewById(R.id.endPosText)
        val checkPeriodText: EditText = itemView.findViewById(R.id.checkPeriodText)
        val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.range_edit_cell, parent, false)
    )

    override fun getItemCount(): Int = subRanges.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val range = subRanges[position]
        holder.apply {
            startText.text = itemView.context.getString(
                R.string.position_format,
                range.start / 1000,
                range.start % 1000
            )
            endText.text = itemView.context.getString(
                R.string.position_format,
                range.end / 1000,
                range.end % 1000
            )
            checkPeriodText.setText(range.checkDay.toString())
            checkPeriodText.addTextChangedListener { text ->
                range.checkDay = (text.toString().toIntOrNull() ?: 0)
            }
            radioGroup.check(
                if (range.single) {
                    R.id.single
                } else {
                    R.id.doubled
                }
            )
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.single -> range.single = true
                    else -> range.single = false
                }
            }
        }
    }

    fun addSplitPoint(pos: Int) {
        val range = subRanges.find { range -> range.start < pos && pos < range.end }
        range ?: throw SplitPointOutOfBounds(pos)
        val l = DRange(range.start, pos, range.checkDay, range.single)
        val r = DRange(pos, range.end, range.checkDay, range.single)
        val idx = subRanges.indexOf(range)
        subRanges.removeAt(idx)
        subRanges.add(idx, r)
        subRanges.add(idx, l)
        notifyItemChanged(idx)
        notifyItemInserted(idx + 1)
    }

    fun removeRange(pos: Int) {
        val range = subRanges[pos]
        val nextRange = subRanges[pos + 1]
        nextRange.start = range.start
        notifyItemChanged(pos + 1)
        subRanges.removeAt(pos)
        notifyItemRemoved(pos)
    }
}

class NewSubRangeDialogFragment : DialogFragment() {
    internal lateinit var listener: Listener
    lateinit var posForm: Form
    lateinit var newPosKm: EditText
    lateinit var newPosM: EditText

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment, pos: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_new_subrange, null)
            newPosKm = view.findViewById(R.id.newPosKm)
            newPosM = view.findViewById(R.id.newPosM)
            posForm = form {
                useRealTimeValidation()
                input(newPosKm, "km") {
                    isNumber().atLeast(0)
                }
                input(newPosM, "m") {
                    isNumber().atLeast(0).lessThan(1000)
                }
            }
            builder.setView(view)
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    val result = posForm.validate()
                    if (result.success()) {
                        val pos = result["km"]!!.asInt()!! * 1000 + result["m"]!!.asInt()!!
                        listener.onDialogPositiveClick(this, pos)
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            listener = targetFragment as Listener
        } catch (e: ClassCastException) {
            throw ClassCastException("$targetFragment must implement Listener")
        }
    }

}
