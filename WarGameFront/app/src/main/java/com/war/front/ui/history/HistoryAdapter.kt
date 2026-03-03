package com.war.front.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.war.front.R
import com.war.front.network.models.HistoryEntry

class HistoryAdapter(
    private val entries: List<HistoryEntry>
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvWinnerName: TextView = view.findViewById(R.id.tvWinnerName)
        val tvWonAt: TextView      = view.findViewById(R.id.tvWonAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = entries[position]
        holder.tvWinnerName.text = "🏆 ${entry.winnerName}"
        holder.tvWonAt.text      = entry.wonAt
    }

    override fun getItemCount(): Int = entries.size
}