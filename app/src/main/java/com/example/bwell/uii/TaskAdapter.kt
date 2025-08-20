package com.example.bwell.uii

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bwell.R
import com.example.bwell.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val items: MutableList<Task>
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val title: TextView = v.findViewById(R.id.title)
        val descr: TextView = v.findViewById(R.id.description)
        val meta: TextView = v.findViewById(R.id.meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.title.text = t.title
        holder.descr.text = t.description

        val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dueTxt = fmt.format(Date(t.dueAtMillis))
        holder.meta.text = "Due: $dueTxt  •  Area: ${t.area}  •  Urgency: ${t.urgency}"
        // Optionally set icon based on area/urgency
        holder.icon.setImageResource(R.drawable.baseline_task_alt_24)
    }

    fun addTask(task: Task) {
        items.add(0, task)
        notifyItemInserted(0)
    }
}
