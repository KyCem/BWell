package com.example.bwell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bwell.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)      // keep this
        // DO NOT call setContent { } here

        if (savedInstanceState == null) {
            switchTo(Home())
            binding.bottomNavigationView.selectedItemId = R.id.home
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home    -> { switchTo(Home()); true }
                R.id.sport   -> { switchTo(Sport()); true }
                R.id.profile -> { switchTo(Profile()); true }
                else -> false
            }
        }
    }

    private fun switchTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}
