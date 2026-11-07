package com.example.hasloya.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "hasloya_tasks.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_TASKS = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_PRIORITY = "priority"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_DUE_DATE = "due_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_PRIORITY TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_CREATED_AT TEXT NOT NULL,
                $COLUMN_DUE_DATE TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
            put(COLUMN_CREATED_AT, task.createdAt)
            put(COLUMN_DUE_DATE, task.dueDate)
        }
        val id = db.insert(TABLE_TASKS, null, values)
        db.close()
        return id
    }

    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_ID))
                val title = getString(getColumnIndexOrThrow(COLUMN_TITLE))
                val description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val isCompleted = getInt(getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1
                val priority = getString(getColumnIndexOrThrow(COLUMN_PRIORITY))
                val category = getString(getColumnIndexOrThrow(COLUMN_CATEGORY))
                val createdAt = getString(getColumnIndexOrThrow(COLUMN_CREATED_AT))
                val dueDate = getString(getColumnIndexOrThrow(COLUMN_DUE_DATE))

                taskList.add(
                    Task(
                        id = id,
                        title = title,
                        description = description,
                        isCompleted = isCompleted,
                        priority = priority,
                        category = category,
                        createdAt = createdAt,
                        dueDate = dueDate
                    )
                )
            }
            close()
        }
        db.close()
        return taskList
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
            put(COLUMN_CREATED_AT, task.createdAt)
            put(COLUMN_DUE_DATE, task.dueDate)
        }
        val rows = db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
        db.close()
        return rows
    }

    fun updateTaskStatus(id: Long, isCompleted: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        val rows = db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }
}
