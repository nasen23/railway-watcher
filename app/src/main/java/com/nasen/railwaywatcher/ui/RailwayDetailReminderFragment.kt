package com.nasen.railwaywatcher.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nasen.railwaywatcher.Global
import com.nasen.railwaywatcher.Quintuple
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.daysBetween
import com.nasen.railwaywatcher.type.Railway
import kotlinx.android.synthetic.main.fragment_railway_reminder.*
import java.util.*

class RailwayDetailReminderFragment(idx: Int) : Fragment() {
    val railway = Global.get(idx)
    lateinit var reminderAdapter: RailwayDetailReminderAdapter
    var full = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_railway_reminder, container, false)

    fun updateAll() {
        reminderAdapter.ranges = if (full) {
            railway.getAll()
        } else {
            railway.getUncheckedOrLate()
        }
        reminderAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manager = object : LinearLayoutManager(requireContext()) {
            override fun supportsPredictiveItemAnimations(): Boolean = true
        }
        reminderAdapter = RailwayDetailReminderAdapter(railway)
        reminderAdapter.setHasStableIds(true)
        subRangeView.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = reminderAdapter
        }
        swipeRefresh.setOnRefreshListener {
            full = !full
            updateAll()
            swipeRefresh.isRefreshing = false
        }
    }
}

class RailwayDetailReminderAdapter(railway: Railway) :
    RecyclerView.Adapter<RailwayDetailReminderAdapter.ViewHolder>() {
    var ranges: List<Quintuple<Int, Int, Int, Date?, Boolean?>> = railway.getUncheckedOrLate()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context
        val startPos: TextView = itemView.findViewById(R.id.startPosText)
        val endPos: TextView = itemView.findViewById(R.id.endPosText)
        val leftText: TextView = itemView.findViewById(R.id.detailLeftText)
        val rightText: TextView = itemView.findViewById(R.id.detailRightText)
        val singleText: TextView = itemView.findViewById(R.id.singleOrDouble)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.range_cell, parent, false)
    )

    override fun getItemCount(): Int = ranges.size

    override fun getItemId(position: Int): Long = ranges[position].hashCode().toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val range = ranges[position]
        holder.apply {
            startPos.text =
                context.getString(R.string.position_format, range.t1 / 1000, range.t1 % 1000)
            endPos.text =
                context.getString(R.string.position_format, range.t2 / 1000, range.t2 % 1000)
            leftText.text = if (range.t4 == null) {
                context.getString(R.string.never_checked)
            } else {
                context.getString(
                    R.string.last_checked_at_format, range.t4.month + 1,
                    range.t4.day
                )
            }
            rightText.text = if (range.t4 == null) {
                ""
            } else {
                val between: Long = daysBetween(range.t4, Date()) - range.t3
                if (between > 0) {
                    context.getString(R.string.past_days_format, between)
                } else {
                    context.getString(R.string.remaining_days_format, -between)
                }
            }
            singleText.text = if (range.t5 == null) {
                context.getString(R.string.single)
            } else if (range.t5) {
                context.getString(R.string.upstream)
            } else {
                context.getString(R.string.downstream)
            }
        }
    }
}
