package com.example.hasloya

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hasloya.data.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var currentTask: Task

    private lateinit var tvDetailTitle: TextView
    private lateinit var tvDetailStatus: TextView
    private lateinit var tvDetailPriority: TextView
    private lateinit var tvDetailCategory: TextView
    private lateinit var tvDetailCreatedAt: TextView
    private lateinit var tvDetailDueDate: TextView
    private lateinit var tvDetailDescription: TextView

    private lateinit var btnToggleStatus: MaterialButton
    private lateinit var btnDeleteTask: MaterialButton
    private lateinit var btnBackToList: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("extra_task", Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("extra_task") as? Task
        }

        if (task == null) {
            finish()
            return
        }

        currentTask = task

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetail)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        tvDetailTitle = findViewById(R.id.tvDetailTitle)
        tvDetailStatus = findViewById(R.id.tvDetailStatus)
        tvDetailPriority = findViewById(R.id.tvDetailPriority)
        tvDetailCategory = findViewById(R.id.tvDetailCategory)
        tvDetailCreatedAt = findViewById(R.id.tvDetailCreatedAt)
        tvDetailDueDate = findViewById(R.id.tvDetailDueDate)
        tvDetailDescription = findViewById(R.id.tvDetailDescription)

        btnToggleStatus = findViewById(R.id.btnToggleStatus)
        btnDeleteTask = findViewById(R.id.btnDeleteTask)
        btnBackToList = findViewById(R.id.btnBackToList)

        bindTaskDetails()

        btnToggleStatus.setOnClickListener {
            toggleStatus()
        }

        btnDeleteTask.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        btnBackToList.setOnClickListener {
            finish()
        }
    }

    private fun bindTaskDetails() {
        tvDetailTitle.text = currentTask.title
        tvDetailCategory.text = currentTask.category

        if (currentTask.description.isBlank()) {
            tvDetailDescription.text = "Sin descripción adicional"
        } else {
            tvDetailDescription.text = currentTask.description
        }

        val createdAtText = "${getString(R.string.created_at_label)} ${currentTask.createdAt}"
        tvDetailCreatedAt.text = createdAtText

        if (currentTask.dueDate.isNotBlank()) {
            tvDetailDueDate.visibility = View.VISIBLE
            tvDetailDueDate.text = "Fecha de vencimiento: ${currentTask.dueDate}"
        } else {
            tvDetailDueDate.visibility = View.GONE
        }

        // Priority
        tvDetailPriority.text = "Prioridad: ${currentTask.priority}"
        when (currentTask.priority.lowercase()) {
            "alta" -> tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_high))
            "media" -> tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_medium))
            else -> tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_low))
        }

        // Status
        if (currentTask.isCompleted) {
            tvDetailStatus.text = "Estado: ${getString(R.string.status_completed)}"
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed))
            btnToggleStatus.text = getString(R.string.action_mark_pending)
        } else {
            tvDetailStatus.text = "Estado: ${getString(R.string.status_pending)}"
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending))
            btnToggleStatus.text = getString(R.string.action_mark_completed)
        }
    }

    private fun toggleStatus() {
        currentTask = currentTask.copy(isCompleted = !currentTask.isCompleted)
        bindTaskDetails()

        val resultIntent = Intent().apply {
            putExtra("updated_task", currentTask)
        }
        setResult(Activity.RESULT_OK, resultIntent)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_msg)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                val resultIntent = Intent().apply {
                    putExtra("deleted_task_id", currentTask.id)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                Toast.makeText(this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(R.string.dialog_no, null)
            .show()
    }
}
