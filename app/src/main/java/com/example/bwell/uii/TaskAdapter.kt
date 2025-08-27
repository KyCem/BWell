package com.example.bwell.uii

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bwell.R
import com.example.bwell.model.Task
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val items: MutableList<Task>,
    private val onStartEdit: (Task) -> Unit,     // show ✅, hide +
    private val onRequestDelete: (Task) -> Unit, // fragment will confirm + delete in Firestore
    private val onCancelEditUI: () -> Unit       // hide ✅, show +
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    private var editingId: String? = null

    private var pendingTitle: String = ""
    private var pendingDescription: String = ""
    private var pendingArea: String = ""

    fun isEditing() = editingId != null

    fun exitEditMode() {
        editingId = null
        pendingTitle = ""
        pendingDescription = ""
        pendingArea = ""
        notifyDataSetChanged()
        onCancelEditUI()
    }

    fun getEditedTask(): Task? {
        val id = editingId ?: return null
        val original = items.firstOrNull { it.id == id } ?: return null
        return original.copy(
            title = pendingTitle,
            description = pendingDescription,
            area = pendingArea
        )
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        // read views
        val ivTaskIcon: ImageView = v.findViewById(R.id.ivTaskIcon)
        val title: TextView = v.findViewById(R.id.title)
        val descr: TextView = v.findViewById(R.id.description)
        val meta: TextView = v.findViewById(R.id.meta)
        val groupRead: View = v.findViewById(R.id.groupRead)

        // edit views
        val groupEdit: View = v.findViewById(R.id.groupEdit)
        val etTitle: TextInputEditText = v.findViewById(R.id.etTitle)
        val etDescription: TextInputEditText = v.findViewById(R.id.etDescription)
        val etArea: TextInputEditText = v.findViewById(R.id.etArea)

        // toggle button
        val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)

        private val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        private val twTitle = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION &&
                    items[bindingAdapterPosition].id == editingId
                ) pendingTitle = s?.toString().orEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        private val twDescr = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION &&
                    items[bindingAdapterPosition].id == editingId
                ) pendingDescription = s?.toString().orEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        private val twArea = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION &&
                    items[bindingAdapterPosition].id == editingId
                ) pendingArea = s?.toString().orEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        fun bind(t: Task, inEdit: Boolean) {
            // read mode content
            title.text = t.title
            descr.text = t.description
            meta.text = "Due: ${fmt.format(Date(t.dueAtMillis))}  •  Area: ${t.area}  •  Urgency: ${t.urgency}"
            ivTaskIcon.setImageResource(R.drawable.baseline_task_alt_24)

            groupRead.visibility = if (inEdit) View.GONE else View.VISIBLE
            groupEdit.visibility = if (inEdit) View.VISIBLE else View.GONE

            if (inEdit) {
                // button shows delete icon
                btnEdit.setImageResource(R.drawable.baseline_delete_24)

                // SHORT TAP = cancel edit (exit without saving)
                btnEdit.setOnClickListener {
                    onRequestDelete(t)

                }
                // LONG PRESS = request delete (fragment will confirm & delete)
                btnEdit.setOnLongClickListener {
                    exitEditMode()
                    true
                }

                // init pending buffers (first time)
                if (editingId == t.id &&
                    pendingTitle.isEmpty() && pendingDescription.isEmpty() && pendingArea.isEmpty()
                ) {
                    pendingTitle = t.title
                    pendingDescription = t.description
                    pendingArea = t.area
                }

                // remove old watchers (avoid duplication after recycling)
                etTitle.removeTextChangedListener(twTitle)
                etDescription.removeTextChangedListener(twDescr)
                etArea.removeTextChangedListener(twArea)

                etTitle.setText(pendingTitle)
                etDescription.setText(pendingDescription)
                etArea.setText(pendingArea)

                etTitle.addTextChangedListener(twTitle)
                etDescription.addTextChangedListener(twDescr)
                etArea.addTextChangedListener(twArea)
            } else {
                // normal edit button
                btnEdit.setImageResource(R.drawable.baseline_edit_24)
                btnEdit.setOnClickListener {
                    editingId = t.id
                    pendingTitle = t.title
                    pendingDescription = t.description
                    pendingArea = t.area
                    notifyDataSetChanged()
                    onStartEdit(t)
                }

                // safety: remove watchers
                etTitle.removeTextChangedListener(twTitle)
                etDescription.removeTextChangedListener(twDescr)
                etArea.removeTextChangedListener(twArea)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        val inEdit = (t.id == editingId)
        holder.bind(t, inEdit)
    }

    override fun getItemCount(): Int = items.size

    fun addTask(task: Task) {
        items.add(0, task)
        notifyItemInserted(0)
    }
}
