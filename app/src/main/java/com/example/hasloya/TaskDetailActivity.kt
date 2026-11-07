package com.example.hasloya

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hasloya.data.Task
import com.example.hasloya.data.TaskDatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var currentTask: Task
    private lateinit var dbHelper: TaskDatabaseHelper

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

        dbHelper = TaskDatabaseHelper(this)

        val task = intent.getSerializableExtra("extra_task", Task::class.java)

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
            tvDetailDescription.text = getString(R.string.detail_no_description)
        } else {
            tvDetailDescription.text = currentTask.description
        }

        val createdAtText = getString(R.string.created_at_label) + " " + currentTask.createdAt
        tvDetailCreatedAt.text = createdAtText

        if (currentTask.dueDate.isNotBlank()) {
            tvDetailDueDate.visibility = View.VISIBLE
            val dueDateText = getString(R.string.due_date_prefix) + " " + currentTask.dueDate
            tvDetailDueDate.text = dueDateText
        } else {
            tvDetailDueDate.visibility = View.GONE
        }

        // Priority
        val priorityText = getString(R.string.priority_label) + ": " + currentTask.priority
        tvDetailPriority.text = priorityText
        when (currentTask.priority.lowercase()) {
            "alta" -> tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_high))
            "media" -> tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_medium))
            else -> tvDetailPriority.setTextColor(ContextCompat.getColor(this, R.color.priority_low))
        }

        // Status
        if (currentTask.isCompleted) {
            val statusCompletedText = getString(R.string.status_label) + " " + getString(R.string.status_completed)
            tvDetailStatus.text = statusCompletedText
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed))
            btnToggleStatus.text = getString(R.string.action_mark_pending)
        } else {
            val statusPendingText = getString(R.string.status_label) + " " + getString(R.string.status_pending)
            tvDetailStatus.text = statusPendingText
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending))
            btnToggleStatus.text = getString(R.string.action_mark_completed)
        }
    }

    private fun toggleStatus() {
        val newStatus = !currentTask.isCompleted
        currentTask = currentTask.copy(isCompleted = newStatus)
        dbHelper.updateTaskStatus(currentTask.id, newStatus)
        bindTaskDetails()

        val resultIntent = Intent().apply {
            putExtra("updated_task", currentTask)
        }
        setResult(RESULT_OK, resultIntent)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_msg)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                dbHelper.deleteTask(currentTask.id)
                val resultIntent = Intent().apply {
                    putExtra("deleted_task_id", currentTask.id)
                }
                setResult(RESULT_OK, resultIntent)
                Toast.makeText(this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(R.string.dialog_no, null)
            .show()
    }
}
