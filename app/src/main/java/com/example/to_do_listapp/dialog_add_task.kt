package com.example.to_do_listapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.util.*

class DialogAddTask : DialogFragment() {

    private lateinit var taskName: EditText
    private lateinit var dueDate: EditText
    private lateinit var dueTime: EditText
    private lateinit var description: EditText
    private var listener: TaskListener? = null
    private var existingTask: Task? = null

    interface TaskListener {
        fun onTaskAdded(task: Task)
        fun onTaskUpdated(task: Task)
    }

    fun setListener(listener: TaskListener) {
        this.listener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? TaskListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_add_task, container, false)

        taskName = view.findViewById(R.id.editTextTaskName)
        dueDate = view.findViewById(R.id.editTextDate)
        dueTime = view.findViewById(R.id.editTextTime)
        description = view.findViewById(R.id.editTextDescription)

        dueDate.setOnClickListener { showDatePicker() }
        dueTime.setOnClickListener { showTimePicker() }

        view.findViewById<Button>(R.id.buttonSave).setOnClickListener { saveTask() }
        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener { dismiss() }

        existingTask?.let {
            taskName.setText(it.taskName)
            dueDate.setText(it.dueDate)
            dueTime.setText(it.dueTime)
            description.setText(it.description)
        }

        return view
    }

    fun setTask(task: Task) {
        existingTask = task
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            dueDate.setText("${selectedDay}/${selectedMonth + 1}/${selectedYear}")
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            dueTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
        }, hour, minute, true).show()
    }

    private fun saveTask() {
        val name = taskName.text.toString()
        val date = dueDate.text.toString()
        val time = dueTime.text.toString()
        val desc = description.text.toString()

        if (name.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && desc.isNotEmpty()) {
            val task = Task(name, date, time, desc, getTimeInMillis(date, time))

            if (existingTask == null) {
                listener?.onTaskAdded(task)
                Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show()
            } else {
                listener?.onTaskUpdated(task)
                Toast.makeText(requireContext(), "Task updated!", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        } else {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTimeInMillis(date: String, time: String): Long {
        val parts = date.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val year = parts[2].toInt()
        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute, 0)
        return calendar.timeInMillis
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
