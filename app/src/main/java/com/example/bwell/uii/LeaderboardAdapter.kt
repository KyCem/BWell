// com/example/bwell/LeaderboardAdapter.kt
package com.example.bwell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bwell.model.LeaderBoardItem

class LeaderboardAdapter(
    private val items: MutableList<LeaderBoardItem> = mutableListOf()
) : RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val rank: TextView = v.findViewById(R.id.tvRank)
        val name: TextView = v.findViewById(R.id.tvUserName)
        val score: TextView = v.findViewById(R.id.tvScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.rank.text = "#${position + 1}"
        holder.name.text = item.name
        holder.score.text = item.score.toString()
    }

    override fun getItemCount() = items.size

    fun replaceAll(newItems: List<LeaderBoardItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
