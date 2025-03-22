package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "task_name") val taskName: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "completed") val completed: Boolean = false
    // Add other relevant fields like dueDate, priority, etc.
)