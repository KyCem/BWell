package com.example.bwell

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bwell.databinding.ActivityMainBinding
import com.example.bwell.model.Task
import com.example.bwell.ui.news.NewsFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Launcher for AddTaskActivity results
    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == RESULT_OK) {
            val task = res.data?.getParcelableExtra<Task>("task")
            if (task != null) {
                // Send the task to Sport fragment
                val bundle = Bundle().apply { putParcelable("task", task) }
                supportFragmentManager.setFragmentResult("new_task_result", bundle)

                // Navigate to Sport so the user sees the new item there
                switchTo(Sport())
                binding.bottomNavigationView.selectedItemId = R.id.sport
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)      // keep this

        if (savedInstanceState == null) {
            switchTo(Sport())
            binding.bottomNavigationView.selectedItemId = R.id.sport
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home    -> { switchTo(NewsFragment()); true }
                R.id.sport   -> { switchTo(Sport()); true }
                R.id.profile -> { switchTo(Profile()); true }
                R.id.diet    -> { switchTo(Diet()); true }
                R.id.chat    -> { switchTo(ChatFragment()); true }
                else -> false
            }
        }

        // FAB -> open AddTaskActivity (safe lookup; works whether in activity_main or included)
        findViewById<FloatingActionButton?>(R.id.fabAdd)?.setOnClickListener {
            addTaskLauncher.launch(Intent(this, AddTaskActivity::class.java))
        }
    }

    private fun switchTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}


