package com.example.hasloya.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hasloya.R
import com.example.hasloya.data.Task

class TaskAdapter(
    initialTasks: List<Task> = emptyList(),
    private val onItemClick: (Task) -> Unit,
    private val onTaskStatusChanged: (Task, Boolean) -> Unit,
    private val onDeleteTask: (Task) -> Unit,
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val tasks: MutableList<Task> = initialTasks.toMutableList()

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbCompleted)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvTitle.text = task.title

        if (task.description.isBlank()) {
            holder.tvDescription.visibility = View.GONE
        } else {
            holder.tvDescription.visibility = View.VISIBLE
            holder.tvDescription.text = task.description
        }

        holder.tvCategory.text = task.category
        val displayDate = if (task.dueDate.isNotBlank()) "Vence: ${task.dueDate}" else task.createdAt
        holder.tvDate.text = displayDate

        // Priority styling
        val context = holder.itemView.context
        holder.tvPriority.text = task.priority
        when (task.priority.lowercase()) {
            "alta" -> {
                holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_high))
            }
            "media" -> {
                holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_medium))
            }
            else -> {
                holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_low))
            }
        }

        // Completion status styling
        holder.cbCompleted.setOnCheckedChangeListener(null)
        holder.cbCompleted.isChecked = task.isCompleted

        if (task.isCompleted) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_completed))
            holder.tvDescription.paintFlags = holder.tvDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_completed))
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            holder.tvDescription.paintFlags = holder.tvDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }

        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            onTaskStatusChanged(task, isChecked)
        }

        holder.itemView.setOnClickListener {
            onItemClick(task)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteTask(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}
