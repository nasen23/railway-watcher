package com.nasen.railwaywatcher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.SplitPointOutOfBounds
import com.nasen.railwaywatcher.type.Range

class SubRangeAdapter(start: Int, end: Int) :
    RecyclerView.Adapter<SubRangeAdapter.ViewHolder>() {
    val subRanges: MutableList<Range> = mutableListOf(Range(start, end, 30))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rangeDisplay: TextView = itemView.findViewById(R.id.subRangeText)
        val checkPeriodText: EditText = itemView.findViewById(R.id.checkPeriodText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.range_edit_cell, parent, false)
    )

    override fun getItemCount(): Int = subRanges.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val range = subRanges[position]
        holder.apply {
            rangeDisplay.text = itemView.context.getString(R.string.range_format).format(
                range.start / 1000,
                range.start % 1000,
                range.end / 1000,
                range.end % 1000
            )
            checkPeriodText.setText(range.checkDay.toString())
            checkPeriodText.addTextChangedListener { text ->
                range.checkDay = (text.toString().toIntOrNull() ?: 0)
            }
        }
    }

    fun addSplitPoint(pos: Int) {
        val range = subRanges.find { range -> range.start < pos && pos < range.end }
        range ?: throw SplitPointOutOfBounds(pos)
        val l = Range(range.start, pos, range.checkDay)
        val r = Range(pos, range.end, range.checkDay)
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