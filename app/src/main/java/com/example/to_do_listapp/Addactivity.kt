package com.example.to_do_listapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Addactivity : AppCompatActivity(), DialogAddTask.TaskListener {

    private lateinit var taskList: RecyclerView
    private lateinit var fab: FloatingActionButton
    private var tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addactivity)


        taskList = findViewById(R.id.recyclerViewTasks)
        fab = findViewById(R.id.fab)

        taskList.layoutManager = LinearLayoutManager(this)

        loadTasks()

        adapter = TaskAdapter(tasks, { task -> deleteTask(task) }, { task -> editTask(task) })
        taskList.adapter = adapter


        fab.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val dialog = DialogAddTask()
        dialog.setListener(this)
        dialog.show(supportFragmentManager, "DialogAddTask")
    }

    override fun onTaskAdded(task: Task) {
        tasks.add(task)
        adapter.notifyItemInserted(tasks.size - 1)
        saveTasks()
        scheduleNotification(task)
    }

    override fun onTaskUpdated(task: Task) {
        val index = tasks.indexOfFirst { it.taskName == task.taskName }
        if (index != -1) {
            val oldTask = tasks[index]
            tasks[index] = task
            adapter.notifyItemChanged(index)
            saveTasks()

            // Only reschedule the notification if the due time has changed
            if (oldTask.dueTimeInMillis != task.dueTimeInMillis) {
                cancelNotification(oldTask)
                scheduleNotification(task)
            }
        }
    }

    private fun editTask(task: Task) {
        val dialog = DialogAddTask()
        dialog.setTask(task)
        dialog.setListener(this)
        dialog.show(supportFragmentManager, "DialogEditTask")
    }

    private fun deleteTask(task: Task) {
        val index = tasks.indexOf(task)
        if (index != -1) {
            tasks.removeAt(index)
            cancelNotification(task)
            adapter.notifyItemRemoved(index)
            saveTasks()
        }
    }

    private fun cancelNotification(task: Task) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun saveTasks() {
        val sharedPreferences = getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            val serializedTasks = tasks.joinToString(";") { "${it.taskName},${it.dueDate},${it.dueTime},${it.description},${it.dueTimeInMillis}" }
            putString("tasks", serializedTasks)
            apply()
        }
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val serializedTasks = sharedPreferences.getString("tasks", null)
        serializedTasks?.let {
            tasks.clear()
            it.split(";").forEach { taskString ->
                val taskParts = taskString.split(",")
                if (taskParts.size == 5) {
                    val task = Task(
                        taskName = taskParts[0],
                        dueDate = taskParts[1],
                        dueTime = taskParts[2],
                        description = taskParts[3],
                        dueTimeInMillis = taskParts[4].toLong()
                    )
                    tasks.add(task)
                }
            }
        }
    }

    private fun scheduleNotification(task: Task) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("taskName", task.taskName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            task.dueTimeInMillis,
            pendingIntent
        )
    }
}
