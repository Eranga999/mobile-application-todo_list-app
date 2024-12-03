package com.example.to_do_listapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)


        val tasks = loadTasks(context)
        val taskNames = tasks.joinToString("\n") { it.taskName }

        views.setTextViewText(R.id.widgetTitle, "Upcoming Tasks")
        views.setTextViewText(R.id.widgetTaskListView, taskNames.ifEmpty { "No upcoming tasks" })

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun loadTasks(context: Context): List<Task> {
        val sharedPreferences = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val serializedTasks = sharedPreferences.getString("tasks", null) ?: return emptyList()

        return serializedTasks.split(";").mapNotNull { taskString ->
            val taskParts = taskString.split(",")
            if (taskParts.size == 5) {
                Task(
                    taskName = taskParts[0],
                    dueDate = taskParts[1],
                    dueTime = taskParts[2],
                    description = taskParts[3],
                    dueTimeInMillis = taskParts[4].toLong()
                )
            } else null
        }
    }
}
