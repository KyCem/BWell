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

class Sport : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    // Register for result IN THE FRAGMENT
    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val task = res.data?.getParcelableExtra<Task>("task")
            if (task != null) {
                adapter.addTask(task)
                recycler.smoothScrollToPosition(0)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_sport, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.tasksRecycler) // make sure this exists in fragment_sport.xml
        adapter = TaskAdapter(tasks)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // FAB click -> open AddTaskActivity
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            addTaskLauncher.launch(Intent(requireContext(), AddTaskActivity::class.java))
        }
    }
}
