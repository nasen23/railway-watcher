package com.nasen.railwaywatcher.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.type.Railway

class RailwayAdapter : RecyclerView.Adapter<RailwayAdapter.ViewHolder>() {
    var railways: MutableList<Railway> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context
        val seqNumView: TextView = itemView.findViewById(R.id.seqNumText)
        val titleView: TextView = itemView.findViewById(R.id.titleText)
        val descriptionView: TextView = itemView.findViewById(R.id.descriptionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.main_cell, parent, false)
        )

    override fun getItemCount() = railways.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val railway = railways[position]
        holder.apply {
            seqNumView.text = (position + 1).toString()
            titleView.text = context.getString(R.string.railway_title, railway.from, railway.to)
            descriptionView.text =
                context.getString(R.string.railway_description, railway.ranges.size)
            itemView.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToRailwayDetailFragment(holder.layoutPosition)
                it.findNavController().navigate(action)
            }
        }
    }
}