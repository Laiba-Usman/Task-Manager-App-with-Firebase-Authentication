package com.example.mytasks.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytasks.data.entity.Task
import com.example.mytasks.data.entity.TaskPriority
import com.example.mytasks.repository.FirebaseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: FirebaseTaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    val allTasks = repository.getAllTasks()
        .catch { e ->
            Log.e("TaskViewModel", "Error loading tasks", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtering is now done in-memory since Firebase returns all tasks for the user
    val activeTasks = allTasks.map { tasks ->
        tasks.filter { !it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completedTasks = allTasks.map { tasks ->
        tasks.filter { it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeTasksCount = activeTasks.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.LoadTask -> loadTask(event.taskId)
            is TaskEvent.SaveTask -> saveTask()
            is TaskEvent.DeleteTask -> deleteTask(event.task)
            is TaskEvent.ToggleTaskCompletion -> toggleTaskCompletion(event.taskId, event.isCompleted)
            is TaskEvent.UpdateTitle -> updateTitle(event.title)
            is TaskEvent.UpdateDescription -> updateDescription(event.description)
            is TaskEvent.UpdateDate -> updateDate(event.date)
            is TaskEvent.UpdatePriority -> updatePriority(event.priority)
            is TaskEvent.SetFilter -> setFilter(event.filter)
            TaskEvent.ClearForm -> clearForm()
            TaskEvent.ClearCompleted -> clearCompletedTasks()
        }
    }

    private fun loadTask(taskId: String?) {
        if (taskId.isNullOrBlank()) {
            clearForm()
            return
        }

        viewModelScope.launch {
            try {
                val task = repository.getTaskById(taskId)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        currentTask = task,
                        title = task.title,
                        description = task.description,
                        date = task.date,
                        priority = task.priority,
                        isEditing = true
                    )
                    Log.d("TaskViewModel", "Task loaded successfully: ${task.title}")
                } else {
                    Log.w("TaskViewModel", "Task not found: $taskId")
                    clearForm()
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error loading task", e)
                clearForm()
            }
        }
    }

    private fun saveTask() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            Log.w("TaskViewModel", "Cannot save task with empty title")
            return
        }

        viewModelScope.launch {
            try {
                if (currentState.isEditing && currentState.currentTask != null) {
                    val updatedTask = currentState.currentTask.copy(
                        title = currentState.title.trim(),
                        description = currentState.description.trim(),
                        date = currentState.date,
                        priority = currentState.priority,
                        updatedAt = Date()
                    )
                    repository.updateTask(updatedTask)
                    Log.d("TaskViewModel", "Task updated successfully")
                } else {
                    val newTask = Task(
                        idString = "", // Firebase will generate the ID
                        title = currentState.title.trim(),
                        description = currentState.description.trim(),
                        date = currentState.date,
                        priority = currentState.priority,
                        isCompleted = false,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                    repository.insertTask(newTask)
                    Log.d("TaskViewModel", "Task created successfully")
                }
                clearForm()
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error saving task", e)
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
                Log.d("TaskViewModel", "Task deleted successfully")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error deleting task", e)
            }
        }
    }

    private fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateTaskCompletion(taskId, isCompleted)
                Log.d("TaskViewModel", "Task completion toggled: $isCompleted")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error toggling task completion", e)
            }
        }
    }

    private fun clearCompletedTasks() {
        viewModelScope.launch {
            try {
                val completed = completedTasks.value
                completed.forEach { task ->
                    repository.deleteTask(task)
                }
                Log.d("TaskViewModel", "Cleared ${completed.size} completed tasks")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error clearing completed tasks", e)
            }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    private fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    private fun updateDate(date: Date) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    private fun updatePriority(priority: TaskPriority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    private fun setFilter(filter: TaskFilter) {
        _uiState.value = _uiState.value.copy(currentFilter = filter)
    }

    private fun clearForm() {
        _uiState.value = TaskUiState()
        Log.d("TaskViewModel", "Form cleared")
    }
}

data class TaskUiState(
    val currentTask: Task? = null,
    val title: String = "",
    val description: String = "",
    val date: Date = Date(),
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isEditing: Boolean = false,
    val currentFilter: TaskFilter = TaskFilter.ALL
)

sealed class TaskEvent {
    data class LoadTask(val taskId: String) : TaskEvent()
    object SaveTask : TaskEvent()
    data class DeleteTask(val task: Task) : TaskEvent()
    data class ToggleTaskCompletion(val taskId: String, val isCompleted: Boolean) : TaskEvent()
    data class UpdateTitle(val title: String) : TaskEvent()
    data class UpdateDescription(val description: String) : TaskEvent()
    data class UpdateDate(val date: Date) : TaskEvent()
    data class UpdatePriority(val priority: TaskPriority) : TaskEvent()
    data class SetFilter(val filter: TaskFilter) : TaskEvent()
    object ClearForm : TaskEvent()
    object ClearCompleted : TaskEvent()
}

enum class TaskFilter {
    ALL, ACTIVE, COMPLETED
}
