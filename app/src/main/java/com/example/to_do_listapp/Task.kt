package com.example.to_do_listapp

import java.io.Serializable

data class Task(
    var taskName: String,
    var dueDate: String,
    var dueTime: String,
    var description: String,
    var dueTimeInMillis: Long
) : Serializable
