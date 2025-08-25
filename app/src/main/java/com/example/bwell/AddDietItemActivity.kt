package com.example.bwell

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bwell.model.DietEntry
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddDietItemActivity : AppCompatActivity() {

    private lateinit var etFoodName: EditText
    private lateinit var etCalories: EditText
    private lateinit var groupPart: MaterialButtonToggleGroup
    private lateinit var btnSave: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diet_item)

        etFoodName = findViewById(R.id.etFoodName)
        etCalories = findViewById(R.id.etCalories)
        groupPart  = findViewById(R.id.groupPart)
        btnSave    = findViewById(R.id.btnSaveDiet)

        val db = FirebaseFirestore.getInstance()

        btnSave.setOnClickListener {
            val name = etFoodName.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                etFoodName.error = "Required"
                return@setOnClickListener
            }

            val kcal = etCalories.text?.toString()?.toIntOrNull() ?: 0
            if (kcal <= 0) {
                etCalories.error = "Enter calories"
                return@setOnClickListener
            }

            val part = when (groupPart.checkedButtonId) {
                R.id.btnMorning   -> "Morning"
                R.id.btnAfternoon -> "Afternoon"
                R.id.btnEvening   -> "Evening"
                else              -> "Morning"
            }

            val entry = DietEntry(name = name, calories = kcal, partOfDay = part)
            val doc = hashMapOf(
                "name" to name,
                "calories" to kcal,
                "partOfDay" to part,
                "createdAt" to FieldValue.serverTimestamp()
            )

            // UX: prevent double taps while saving
            btnSave.isEnabled = false
            btnSave.text = "Saving…"

            FirebaseFirestore.getInstance()
                .collection("dietEntries")
                .add(doc)
                .addOnSuccessListener { _ ->
                    android.widget.Toast.makeText(this@AddDietItemActivity, "Saved", android.widget.Toast.LENGTH_SHORT).show()
                    val result = android.content.Intent().putExtra("dietEntry", entry)
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSave.isEnabled = true
                    btnSave.text = "Save"
                    android.widget.Toast.makeText(this@AddDietItemActivity, "Save failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    etFoodName.error = e.localizedMessage ?: "Save failed"
                }
        }
    }
}
