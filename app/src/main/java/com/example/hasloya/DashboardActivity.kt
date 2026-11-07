package com.example.hasloya

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hasloya.adapter.TaskAdapter
import com.example.hasloya.data.Task
import com.example.hasloya.data.TaskDatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator

class DashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper

    private lateinit var tvStatTotal: TextView
    private lateinit var tvStatPending: TextView
    private lateinit var tvStatCompleted: TextView
    private lateinit var tvStatPercentage: TextView
    private lateinit var progressBarTasks: LinearProgressIndicator
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var rvDashboardTasks: RecyclerView
    private lateinit var tvDashboardEmpty: TextView

    private lateinit var taskAdapter: TaskAdapter
    private var allTasks = mutableListOf<Task>()
    private var selectedFilter = FILTER_ALL

    companion object {
        private const val FILTER_ALL = 0
        private const val FILTER_PENDING = 1
        private const val FILTER_COMPLETED = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        dbHelper = TaskDatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDashboard)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        tvStatTotal = findViewById(R.id.tvStatTotal)
        tvStatPending = findViewById(R.id.tvStatPending)
        tvStatCompleted = findViewById(R.id.tvStatCompleted)
        tvStatPercentage = findViewById(R.id.tvStatPercentage)
        progressBarTasks = findViewById(R.id.progressBarTasks)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
        rvDashboardTasks = findViewById(R.id.rvDashboardTasks)
        tvDashboardEmpty = findViewById(R.id.tvDashboardEmpty)

        setupRecyclerView()
        setupFilters()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = mutableListOf(),
            onItemClick = { task ->
                val intent = Intent(this, TaskDetailActivity::class.java).apply {
                    putExtra("extra_task", task)
                }
                startActivity(intent)
            },
            onTaskStatusChanged = { task, isCompleted ->
                dbHelper.updateTaskStatus(task.id, isCompleted)
                loadDashboardData()
            },
            onDeleteTask = { task ->
                dbHelper.deleteTask(task.id)
                loadDashboardData()
            }
        )

        rvDashboardTasks.layoutManager = LinearLayoutManager(this)
        rvDashboardTasks.adapter = taskAdapter
    }

    private fun setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedFilter = when {
                checkedIds.contains(R.id.chipFilterPending) -> FILTER_PENDING
                checkedIds.contains(R.id.chipFilterCompleted) -> FILTER_COMPLETED
                else -> FILTER_ALL
            }
            applyCurrentFilter()
        }
    }

    private fun loadDashboardData() {
        allTasks.clear()
        allTasks.addAll(dbHelper.getAllTasks())

        val total = allTasks.size
        val pending = allTasks.count { !it.isCompleted }
        val completed = allTasks.count { it.isCompleted }

        tvStatTotal.text = total.toString()
        tvStatPending.text = pending.toString()
        tvStatCompleted.text = completed.toString()

        val percentage = if (total > 0) (completed * 100) / total else 0
        tvStatPercentage.text = "$percentage%"
        progressBarTasks.setProgress(percentage, true)

        applyCurrentFilter()
    }

    private fun applyCurrentFilter() {
        val filteredTasks = when (selectedFilter) {
            FILTER_PENDING -> allTasks.filter { !it.isCompleted }
            FILTER_COMPLETED -> allTasks.filter { it.isCompleted }
            else -> allTasks
        }

        taskAdapter.updateTasks(filteredTasks)

        if (filteredTasks.isEmpty()) {
            tvDashboardEmpty.visibility = View.VISIBLE
            rvDashboardTasks.visibility = View.GONE
        } else {
            tvDashboardEmpty.visibility = View.GONE
            rvDashboardTasks.visibility = View.VISIBLE
        }
    }
}
