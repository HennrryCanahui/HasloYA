package com.example.hasloya

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hasloya.adapter.TaskAdapter
import com.example.hasloya.data.Task
import com.example.hasloya.data.TaskDatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var dbHelper: TaskDatabaseHelper

    private val taskList = mutableListOf<Task>()

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newTask = result.data?.getSerializableExtra(AddTaskActivity.EXTRA_TASK, Task::class.java)
            newTask?.let { task ->
                dbHelper.insertTask(task)
                loadTasksFromDatabase()
            }
        }
    }

    private val detailTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        loadTasksFromDatabase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarMain)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_dashboard) {
                val intent = Intent(this, DashboardActivity::class.java)
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
    }

    override fun onResume() {
        super.onResume()
        loadTasksFromDatabase()
    }

    private fun loadTasksFromDatabase() {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
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
                dbHelper.updateTaskStatus(task.id, isCompleted)
                loadTasksFromDatabase()
            },
            onDeleteTask = { task ->
                deleteTask(task)
            },
        )

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = taskAdapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position in taskList.indices) {
                    val taskToDelete = taskList[position]
                    deleteTask(taskToDelete)
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvTasks)
    }

    private fun deleteTask(task: Task) {
        dbHelper.deleteTask(task.id)
        loadTasksFromDatabase()

        Snackbar.make(rvTasks, R.string.msg_task_deleted, Snackbar.LENGTH_LONG)
            .setAction("Deshacer") {
                dbHelper.insertTask(task)
                loadTasksFromDatabase()
            }.show()
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
