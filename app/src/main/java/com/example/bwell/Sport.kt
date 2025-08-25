package com.example.bwell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bwell.model.Task
import com.example.bwell.uii.TaskAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class Sport : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    private lateinit var db: FirebaseFirestore
    private var tasksListener: ListenerRegistration? = null

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->

        if (res.resultCode == Activity.RESULT_OK) {
            recycler.smoothScrollToPosition(0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_sport, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        recycler = view.findViewById(R.id.tasksRecycler)
        adapter = TaskAdapter(tasks)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Right-bottom FAB (add task) — keep yours if already set up
        view.findViewById<FloatingActionButton>(R.id.fabAdd)?.setOnClickListener {
            addTaskLauncher.launch(Intent(requireContext(), AddTaskActivity::class.java))
        }

        // Left-bottom FAB (chat) is wired elsewhere in your code

        subscribeToTasks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tasksListener?.remove()
        tasksListener = null
    }

    /** Start a realtime subscription to Firestore */
    private fun subscribeToTasks() {
        // Choose your sort: by createdAt (server timestamp) or by dueAt (epoch millis)
        tasksListener?.remove()
        tasksListener = db.collection("tasks")
            .orderBy("createdAt", Query.Direction.DESCENDING) // or .orderBy("dueAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) {
                    // You can show a Snackbar/Toast here if you like
                    return@addSnapshotListener
                }
                val list = snapshots.documents.mapNotNull { it.toTaskOrNull() }
                replaceAll(list)
            }
    }

    /** Replace adapter data efficiently */
    private fun replaceAll(newList: List<Task>) {
        tasks.clear()
        tasks.addAll(newList)
        adapter.notifyDataSetChanged()
    }

    /** Map Firestore doc -> Task (defensive against nulls/types) */
    private fun DocumentSnapshot.toTaskOrNull(): Task? {
        val title = getString("title") ?: return null
        val description = getString("description").orEmpty()
        val dueAt = getLong("dueAt") ?: 0L
        val area = getString("area").orEmpty().ifBlank { "General" }
        val urgency = (getLong("urgency") ?: 1L).toInt().coerceIn(1, 5)
        return Task(
            title = title,
            description = description,
            dueAtMillis = dueAt,
            area = area,
            urgency = urgency
        )
    }
}
