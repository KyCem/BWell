package com.example.bwell

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // TODO: when you add real auth, check here if user is already logged in.
        // If yes -> goToMain()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogin)
            .setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java)) // ✅ open login screen
            }

        findViewById<MaterialButton>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // prevent back to landing after entering the app
    }
}
