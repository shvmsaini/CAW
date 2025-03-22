package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.DefaultTaskRepository
import com.example.myapplication.data.Task
import com.example.myapplication.data.TaskDatabase
import com.example.myapplication.ui.TaskViewModel
import com.example.myapplication.ui.TaskViewModelFactory
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TaskApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskApp(
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            DefaultTaskRepository(TaskDatabase.getDatabase(LocalContext.current).taskDao())
        )
    )
) {
    val tasks by taskViewModel.allTasks.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, "Add")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TaskList(tasks = tasks, taskViewModel = taskViewModel)
        }
    }

    if (showDialog) {
        AddTaskDialog(
            onAddTask = { taskName, taskDescription ->
                taskViewModel.insertTask(Task(taskName = taskName, description = taskDescription))
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onAddTask: (String, String) -> Unit, onDismiss: () -> Unit) {
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = { Text("Task Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    placeholder = { Text("Task Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddTask(taskName, taskDescription)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TaskList(tasks: List<Task>, taskViewModel: TaskViewModel) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(tasks) { task ->
            TaskItem(task = task, taskViewModel = taskViewModel)
            Divider()
        }
    }
}

@Composable
fun TaskItem(task: Task, taskViewModel: TaskViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    ) {
                        append(task.taskName)
                    }
                }
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                            color = Color.Gray,
                            fontSize = 14.sp,
                        )
                    ) {
                        append(task.description)
                    }
                }
            )
        }

        Checkbox(
            checked = task.completed,
            onCheckedChange = {
                taskViewModel.updateTask(task.copy(completed = it))
            }
        )

        IconButton(onClick = { taskViewModel.deleteTask(task) }) {
            Icon(Icons.Filled.Close, "Delete", tint = Color.Red)
        }
    }
}