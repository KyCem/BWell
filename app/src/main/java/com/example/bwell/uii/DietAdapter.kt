package com.example.bwell.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bwell.R
import com.example.bwell.model.DietEntry

class DietAdapter(
    private val items: MutableList<DietEntry>
) : RecyclerView.Adapter<DietAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvFoodName)
        val calories: TextView = v.findViewById(R.id.tvCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_diet_food, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.name.text = e.name
        holder.calories.text = "${e.calories} kcal"
    }

    fun addFirst(entry: DietEntry) {
        items.add(0, entry)
        notifyItemInserted(0)
    }

    fun replaceAll(newItems: List<DietEntry>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun totalCalories(): Int = items.sumOf { it.calories }
}
