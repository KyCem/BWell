package com.example.bwell

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bwell.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // If already signed in, skip login (optional)
        //auth.currentUser?.let {
          //  goToMain()
            //return
        //}

        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_LONG).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.localizedMessage ?: "Reset failed", Toast.LENGTH_LONG).show()
                    }
            }
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        clearErrors()

        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass  = binding.etPassword.text?.toString().orEmpty()

        var ok = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.ilEmail.error = "Invalid email"
            ok = false
        }
        if (pass.isEmpty()) {
            binding.ilPassword.error = "Enter password"
            ok = false
        }
        if (!ok) return

        setLoading(true)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    setLoading(false)
                    val msg = "Your email or password is wrong"
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }
                goToMain()
            }
    }

    private fun clearErrors() {
        binding.ilEmail.error = null
        binding.ilPassword.error = null
    }

    private fun setLoading(loading: Boolean) {
        binding.progressOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }

    private fun goToMain() {
        setLoading(false)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
