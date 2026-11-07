package com.example.hasloya

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hasloya.adapter.TaskAdapter
import com.example.hasloya.data.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter

    private val taskList = mutableListOf<Task>()
    private var nextId = 1L

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newTask = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra(AddTaskActivity.EXTRA_TASK, Task::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getSerializableExtra(AddTaskActivity.EXTRA_TASK) as? Task
            }

            newTask?.let { task ->
                val taskWithId = task.copy(id = nextId++)
                taskList.add(0, taskWithId)
                updateUI()
            }
        }
    }

    private val detailTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedTask = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra("updated_task", Task::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getSerializableExtra("updated_task") as? Task
            }
            val deletedTaskId = result.data?.getLongExtra("deleted_task_id", -1L) ?: -1L

            if (deletedTaskId != -1L) {
                taskList.removeAll { it.id == deletedTaskId }
                updateUI()
            } else updatedTask?.let { task ->
                val index = taskList.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    taskList[index] = task
                    updateUI()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarMain)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_dashboard) {
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("task_list", ArrayList(taskList))
                startActivity(intent)
                true
            } else {
                false
            }
        }

        rvTasks = findViewById(R.id.rvTasks)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        fabAddTask = findViewById(R.id.fabAddTask)

        setupRecyclerView()

        fabAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }

        updateUI()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = taskList,
            onItemClick = { task ->
                val intent = Intent(this, TaskDetailActivity::class.java).apply {
                    putExtra("extra_task", task)
                }
                detailTaskLauncher.launch(intent)
            },
            onTaskStatusChanged = { task, isCompleted ->
                val index = taskList.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    taskList[index] = task.copy(isCompleted = isCompleted)
                    updateUI()
                }
            },
            onDeleteTask = { task ->
                taskList.removeAll { it.id == task.id }
                updateUI()
            }
        )

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = taskAdapter
    }

    private fun updateUI() {
        taskAdapter.updateTasks(taskList)
        if (taskList.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvTasks.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvTasks.visibility = View.VISIBLE
        }
    }
}
