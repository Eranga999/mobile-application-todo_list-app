package com.example.to_do_listapp

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private val taskList: MutableList<Task>,
    private val deleteCallback: (Task) -> Unit,
    private val editCallback: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val timers = mutableListOf<CountDownTimer?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_task_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskNameTextView.text = task.taskName
        holder.dueDateTextView.text = "Due: ${task.dueDate} ${task.dueTime}"


        timers.getOrNull(position)?.cancel()


        val timeUntilDue = task.dueTimeInMillis - System.currentTimeMillis()
        if (timeUntilDue > 0) {
            val countdownTimer = object : CountDownTimer(timeUntilDue, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                    holder.countdownTextView.text = String.format("Countdown: %02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    holder.countdownTextView.text = "Time's up!"
                }
            }.start()


            timers.add(position, countdownTimer)
        } else {
            holder.countdownTextView.text = "Time's up!"
            timers.add(position, null)
        }


        holder.itemView.setOnLongClickListener {
            deleteCallback(task)
            true
        }


        holder.itemView.setOnClickListener {
            editCallback(task)
        }
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        super.onViewRecycled(holder)

        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            timers.getOrNull(position)?.cancel()
            timers[position] = null
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = taskList.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        val dueDateTextView: TextView = itemView.findViewById(R.id.dueDateTextView)
        val countdownTextView: TextView = itemView.findViewById(R.id.countdownTextView)
    }
}


