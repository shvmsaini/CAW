package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByCompletion(completed: Boolean): Flow<List<Task>>

}

class DefaultTaskRepository(private val taskDao: TaskDao) : TaskRepository {
    override suspend fun insertTask(task: Task) = taskDao.insert(task)

    override suspend fun updateTask(task: Task) = taskDao.update(task)

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTasksByCompletion(completed: Boolean): Flow<List<Task>> = taskDao.getTasksByCompletion(completed)
}