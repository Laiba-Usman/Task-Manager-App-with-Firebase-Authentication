package com.example.mytasks.repository

import com.example.mytasks.data.dao.TaskDao
import com.example.mytasks.data.entity.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()

    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()

    // 🔄 Changed from Int to String
    suspend fun getTaskById(id: String): Task? = taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    // 🔄 Changed from Int to String
    suspend fun updateTaskCompletion(id: String, isCompleted: Boolean) =
        taskDao.updateTaskCompletion(id, isCompleted)

    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    fun getActiveTasksCount(): Flow<Int> = taskDao.getActiveTasksCount()
}
