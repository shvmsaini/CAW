package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.DefaultTaskRepository
import com.example.myapplication.data.Task
import com.example.myapplication.data.TaskDatabase
import com.example.myapplication.ui.TaskViewModel
import com.example.myapplication.ui.TaskViewModelFactory
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        firebaseAnalytics = Firebase.analytics
        setContent {
            MyApplicationTheme {
                TaskApp(context = LocalContext.current, firebaseAnalytics = firebaseAnalytics)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskApp(
    context: Context,
    firebaseAnalytics: FirebaseAnalytics,
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            DefaultTaskRepository(TaskDatabase.getDatabase(context).taskDao())
        )
    )
) {
    val tasks by taskViewModel.allTasks.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    val isFirstLaunch = remember { mutableStateOf(true) }

    // Load predefined tasks from JSON
    LaunchedEffect(Unit) {
        isFirstLaunch.value = context.dataStore.data.first()[IS_FIRST_LAUNCH] ?: true
        if (isFirstLaunch.value) {
            val predefinedTasks = loadTasksFromLocalJson(context)
            predefinedTasks.forEach { task ->
                taskViewModel.insertTask(task)
            }
            context.dataStore.edit { preferences ->
                preferences[IS_FIRST_LAUNCH] = false
            }
        }
    }
    // Fetch tasks from the API and add them to the db.
    LaunchedEffect(Unit) {
        fetchTasksFromApi(taskViewModel)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showDialog = true
                firebaseAnalytics.logEvent("Task Added") {
                    param("user", "user added a new task")
                }
            }) {
                Icon(Icons.Filled.Add, "Add")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TaskList(
                tasks = tasks,
                taskViewModel = taskViewModel,
                firebaseAnalytics = firebaseAnalytics
            )
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
fun TaskList(
    tasks: List<Task>,
    taskViewModel: TaskViewModel,
    firebaseAnalytics: FirebaseAnalytics
) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                taskViewModel = taskViewModel,
                firebaseAnalytics = firebaseAnalytics
            )
            Divider()
        }
    }
}

@Composable
fun TaskItem(task: Task, taskViewModel: TaskViewModel, firebaseAnalytics: FirebaseAnalytics) {
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
                if (it) {
                    firebaseAnalytics.logEvent("Task Completed") {
                        param("Task", "Task with name ${task.taskName} was completed")
                    }
                } else {
                    firebaseAnalytics.logEvent("Task Edited") {
                        param("Task", "Task with name ${task.taskName} was edited")
                    }
                }
            }
        )

        IconButton(onClick = {
            try {
                taskViewModel.deleteTask(task)
            } catch (e: Exception) {
                throw RuntimeException("database Error")
            }
        }) {
            Icon(Icons.Filled.Close, "Delete", tint = Color.Red)
        }
    }
}

// Load Tasks from JSON file.
fun loadTasksFromLocalJson(context: Context): List<Task> {
    val jsonString = try {
        context.assets.open("tasks.json").bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
        return emptyList()
    }
    val gson = Gson()
    val listTaskType: Type = object : TypeToken<List<Task>>() {}.type
    return gson.fromJson(jsonString, listTaskType)
}

// Fetch tasks from the API
fun fetchTasksFromApi(taskViewModel: TaskViewModel) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://jsonplaceholder.typicode.com/todos?_limit=5") // Example URL, adjust as needed
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Network", "Failed to fetch tasks", e)
            // Monitor the network performance in Firebase
            Firebase.analytics.logEvent("network_error") {
                param("error_message", e.message ?: "unknown error")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.let { responseBody ->
                    val jsonString = responseBody.string()
                    val gson = Gson()
                    val listTaskType: Type = object : TypeToken<List<ApiTask>>() {}.type
                    val apiTasks: List<ApiTask> = gson.fromJson(jsonString, listTaskType)
                    val tasks = apiTasks.map { apiTask ->
                        Task(
                            taskName = apiTask.title,
                            description = "",
                            completed = apiTask.completed
                        )
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        tasks.forEach { task ->
                            taskViewModel.insertTask(task)
                        }
                    }
                }
            } else {
                Log.e("Network", "API request was not successful. Code: ${response.code}")
            }
        }
    })
}

data class ApiTask(val id: Int, val userId: Int, val title: String, val completed: Boolean)