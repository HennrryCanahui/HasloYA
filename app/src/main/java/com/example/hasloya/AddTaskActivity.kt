package com.example.hasloya

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hasloya.data.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK = "extra_task"
    }

    private lateinit var tilTitle: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var actvPriority: AutoCompleteTextView
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var etDueDate: TextInputEditText
    private lateinit var btnSaveTask: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private var selectedDueDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAddTask)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        tilTitle = findViewById(R.id.tilTitle)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        actvPriority = findViewById(R.id.actvPriority)
        actvCategory = findViewById(R.id.actvCategory)
        etDueDate = findViewById(R.id.etDueDate)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        btnCancel = findViewById(R.id.btnCancel)

        setupDropdowns()
        setupDatePicker()

        btnSaveTask.setOnClickListener {
            saveTask()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupDropdowns() {
        val priorities = arrayOf(
            getString(R.string.priority_high),
            getString(R.string.priority_medium),
            getString(R.string.priority_low)
        )
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, priorities)
        actvPriority.setAdapter(priorityAdapter)
        actvPriority.setText(priorities[1], false) // Default: Media

        val categories = arrayOf(
            getString(R.string.category_personal),
            getString(R.string.category_work),
            getString(R.string.category_study),
            getString(R.string.category_home),
            getString(R.string.category_other)
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(categoryAdapter)
        actvCategory.setText(categories[0], false) // Default: Personal
    }

    private fun setupDatePicker() {
        etDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonth + 1)
                val formattedDay = String.format(Locale.getDefault(), "%02d", selectedDay)
                selectedDueDate = "$formattedDay/$formattedMonth/$selectedYear"
                etDueDate.setText(selectedDueDate)
            }, year, month, day)

            datePickerDialog.show()
        }
    }

    private fun saveTask() {
        val title = etTitle.text?.toString()?.trim() ?: ""
        val description = etDescription.text?.toString()?.trim() ?: ""
        val priority = actvPriority.text.toString()
        val category = actvCategory.text.toString()
        val dueDate = etDueDate.text?.toString()?.trim() ?: ""

        if (title.isEmpty()) {
            tilTitle.error = getString(R.string.error_empty_title)
            return
        } else {
            tilTitle.error = null
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val createdAt = dateFormat.format(Calendar.getInstance().time)

        val newTask = Task(
            title = title,
            description = description,
            isCompleted = false,
            priority = priority,
            category = category,
            createdAt = createdAt,
            dueDate = dueDate
        )

        val resultIntent = Intent().apply {
            putExtra(EXTRA_TASK, newTask)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        Toast.makeText(this, R.string.msg_task_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
}
