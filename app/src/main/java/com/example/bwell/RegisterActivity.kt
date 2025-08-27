package com.example.bwell

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bwell.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnCreateAccount.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        clearErrors()

        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()

        var ok = true
        if (name.length < 2) { binding.ilName.error = "Please enter your name"; ok = false }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.ilEmail.error = "Invalid email"; ok = false }
        if (pass.length < 6) { binding.ilPassword.error = "At least 6 characters"; ok = false }
        if (pass != confirm) { binding.ilConfirm.error = "Passwords do not match"; ok = false }
        if (!ok) return

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                setLoading(false)
                Toast.makeText(this, task.exception?.localizedMessage ?: "Registration failed", Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }

            val user = auth.currentUser
            if (user != null) {
                val updates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                user.updateProfile(updates).addOnCompleteListener {
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener { goToMain() }
                        .addOnFailureListener {
                            setLoading(false)
                            Toast.makeText(this, "Firestore error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }
            } else {
                goToMain()
            }
        }
    }

    private fun clearErrors() {
        binding.ilName.error = null
        binding.ilEmail.error = null
        binding.ilPassword.error = null
        binding.ilConfirm.error = null
    }

    private fun setLoading(loading: Boolean) {
        binding.progressOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnCreateAccount.isEnabled = !loading
    }

    private fun goToMain() {
        setLoading(false)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
