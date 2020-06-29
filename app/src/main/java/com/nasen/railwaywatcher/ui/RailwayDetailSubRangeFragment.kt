package com.nasen.railwaywatcher.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.vvalidator.form
import com.nasen.railwaywatcher.Global
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.type.Railway
import com.nasen.railwaywatcher.type.Range
import kotlinx.android.synthetic.main.fragment_subrange_list.*

class RailwayDetailSubRangeFragment(val parent: Fragment, val idx: Int) : Fragment(),
    SetCheckDayDialogFragment.Listener {
    val railway: Railway = Global.get(idx)
    lateinit var rangeAdapter: SubRangesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_subrange_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manager = LinearLayoutManager(requireContext())
        rangeAdapter = SubRangesAdapter(this, railway.ranges)
        subRangeView.apply {
            setHasFixedSize(true)
            layoutManager = manager
            this.adapter = rangeAdapter
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, subIdx: Int, result: Int) {
        Global.setCheckPeriod(idx, subIdx, result)
        rangeAdapter.notifyItemChanged(subIdx)
        parent.childFragmentManager.fragments.filterIsInstance(RailwayDetailReminderFragment::class.java)
            .forEach {
                it.updateAll()
            }
    }
}

class SubRangesAdapter(val fragment: Fragment, var subRanges: List<Range>) :
    RecyclerView.Adapter<SubRangesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context
        val startPos: TextView = itemView.findViewById(R.id.startPosText)
        val endPos: TextView = itemView.findViewById(R.id.endPosText)
        val leftText: TextView = itemView.findViewById(R.id.detailLeftText)
        val rightText: TextView = itemView.findViewById(R.id.detailRightText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.range_cell, parent, false)
    )

    override fun getItemCount(): Int = subRanges.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subRange = subRanges[position]
        holder.apply {
            startPos.text = context.getString(
                R.string.position_format,
                subRange.start / 1000,
                subRange.start % 1000
            )
            endPos.text = context.getString(
                R.string.position_format,
                subRange.end / 1000,
                subRange.end % 1000
            )
            leftText.text = context.getString(R.string.day_format, subRange.checkDay)
            rightText.text =
                context.getString(R.string.remind_count_format, subRange.getUncheckedOrLateCount())
            itemView.setOnClickListener {
                val dialog = SetCheckDayDialogFragment(layoutPosition)
                dialog.setTargetFragment(fragment, 0)
                dialog.show(fragment.parentFragmentManager, null)
            }
        }
    }
}

class SetCheckDayDialogFragment(val idx: Int) : DialogFragment() {
    lateinit var listener: Listener

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment, subIdx: Int, result: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            val view = inflater.inflate(R.layout.dialog_set_check_day, null, false)
            val dayText: EditText = view.findViewById(R.id.checkDayText)
            val myForm = form {
                useRealTimeValidation()
                input(dayText, "checkDay") {
                    isNumber().greaterThan(0)
                }
            }
            builder.setView(view).setTitle(R.string.set_check_date)
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    val result = myForm.validate()
                    if (result.success()) {
                        listener.onDialogPositiveClick(
                            this,
                            idx,
                            result["checkDay"]!!.asInt()!!
                        )
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            listener = targetFragment as Listener
        } catch (e: ClassCastException) {
            throw IllegalStateException("$targetFragment must implement Listener")
        }
    }
}