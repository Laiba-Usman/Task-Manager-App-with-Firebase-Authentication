package com.example.mytasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytasks.data.entity.Task
import com.example.mytasks.data.entity.TaskPriority
import com.example.mytasks.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    val allTasks = repository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeTasks = repository.getActiveTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedTasks = repository.getCompletedTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeTasksCount = repository.getActiveTasksCount()
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
            is TaskEvent.ClearForm -> clearForm()
            is TaskEvent.DeleteCompletedTasks -> deleteCompletedTasks()
            is TaskEvent.SetFilter -> setFilter(event.filter)
        }
    }

    private fun loadTask(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            task?.let {
                _uiState.value = _uiState.value.copy(
                    currentTask = it,
                    title = it.title,
                    description = it.description,
                    date = it.date,
                    priority = it.priority,
                    isEditing = true
                )
            }
        }
    }

    private fun saveTask() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            val task = if (state.isEditing) {
                state.currentTask?.copy(
                    title = state.title,
                    description = state.description,
                    date = state.date,
                    priority = state.priority,
                    updatedAt = Date()
                )
            } else {
                Task(
                    title = state.title,
                    description = state.description,
                    date = state.date,
                    priority = state.priority
                )
            }

            task?.let {
                if (state.isEditing) {
                    repository.updateTask(it)
                } else {
                    repository.insertTask(it)
                }
                clearForm()
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskCompletion(taskId, isCompleted)
        }
    }

    private fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
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
    data class LoadTask(val taskId: Int) : TaskEvent()
    object SaveTask : TaskEvent()
    data class DeleteTask(val task: Task) : TaskEvent()
    data class ToggleTaskCompletion(val taskId: Int, val isCompleted: Boolean) : TaskEvent()
    data class UpdateTitle(val title: String) : TaskEvent()
    data class UpdateDescription(val description: String) : TaskEvent()
    data class UpdateDate(val date: Date) : TaskEvent()
    data class UpdatePriority(val priority: TaskPriority) : TaskEvent()
    object ClearForm : TaskEvent()
    object DeleteCompletedTasks : TaskEvent()
    data class SetFilter(val filter: TaskFilter) : TaskEvent()
}

enum class TaskFilter(val displayName: String) {
    ALL("All Tasks"),
    ACTIVE("Active"),
    COMPLETED("Completed")
}