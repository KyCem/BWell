package com.example.bwell

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.bwell.model.Task
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etArea: EditText
    private lateinit var etUrgency: EditText
    private lateinit var btnSave: MaterialButton

    private val cal: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etArea = findViewById(R.id.etArea)
        etUrgency = findViewById(R.id.etUrgency)
        btnSave = findViewById(R.id.btnSave)

        val db = FirebaseFirestore.getInstance()

        // Date picker
        etDate.setOnClickListener {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, yy, mm, dd ->
                cal.set(Calendar.YEAR, yy)
                cal.set(Calendar.MONTH, mm)
                cal.set(Calendar.DAY_OF_MONTH, dd)
                etDate.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time))
            }, y, m, d).show()
        }

        // Time picker
        etTime.setOnClickListener {
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val min = cal.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, hh, mm ->
                cal.set(Calendar.HOUR_OF_DAY, hh)
                cal.set(Calendar.MINUTE, mm)
                cal.set(Calendar.SECOND, 0)
                etTime.setText(SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time))
            }, h, min, true).show()
        }

        // Save -> write to Firestore AND return to caller
        btnSave.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            if (title.isBlank()) {
                etTitle.error = "Title required"
                return@setOnClickListener
            }
            val desc = etDescription.text?.toString()?.trim().orEmpty()
            val area = etArea.text?.toString()?.trim().orEmpty().ifBlank { "General" }
            val urgency = etUrgency.text?.toString()?.toIntOrNull()?.coerceIn(1, 5) ?: 1
            val dueAtMillis = cal.timeInMillis

            // Object for your RecyclerView
            val task = Task(title, desc, dueAtMillis, area, urgency)

            // Firestore doc (simple primitives)
            val taskDoc = hashMapOf(
                "title" to title,
                "description" to desc,
                "dueAt" to dueAtMillis,
                "area" to area,
                "urgency" to urgency,
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("tasks")
                .add(taskDoc)
                .addOnSuccessListener {
                    // Return result to the fragment/activity that launched this screen
                    val data = Intent().putExtra("task", task)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
                .addOnFailureListener { e ->
                    etTitle.error = "Failed to save: ${e.localizedMessage}"
                }
        }
    }
}
