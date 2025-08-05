package com.example.mytasks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mytasks.ui.components.TaskItem
import com.example.mytasks.viewmodel.AuthEvent
import com.example.mytasks.viewmodel.AuthViewModel
import com.example.mytasks.viewmodel.TaskEvent
import com.example.mytasks.viewmodel.TaskFilter
import com.example.mytasks.viewmodel.TaskViewModel
import com.example.mytasks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()
    val activeTasksCount by viewModel.activeTasksCount.collectAsStateWithLifecycle()
    val userName by authViewModel.userName.collectAsStateWithLifecycle()

    var showClearCompletedDialog by remember { mutableStateOf(false) }

    val tasksToShow = when (uiState.currentFilter) {
        TaskFilter.ALL -> allTasks
        TaskFilter.ACTIVE -> activeTasks
        TaskFilter.COMPLETED -> completedTasks
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Tasks") },
                actions = {
                    IconButton(onClick = { authViewModel.onEvent(AuthEvent.Logout) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTask,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                text = { Text("Add Task") },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SpaceGray, SpaceBlack)
                    )
                )
        ) {
            HeaderSection(
                userName = userName,
                activeTasksCount = activeTasksCount,
                onClearCompleted = { showClearCompletedDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilterChips(
                currentFilter = uiState.currentFilter,
                onFilterChange = { viewModel.onEvent(TaskEvent.SetFilter(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = tasksToShow.isEmpty(),
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                },
                label = "TaskListContent"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyListPlaceholder(
                        filter = uiState.currentFilter,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasksToShow, key = { it.idString ?: "" }) { task ->
                            TaskItem(
                                task = task,
                                onToggleComplete = { id, isCompleted ->
                                    viewModel.onEvent(TaskEvent.ToggleTaskCompletion(id, isCompleted))
                                },
                                onEdit = { taskId -> onNavigateToEditTask(taskId) },
                                onDelete = { viewModel.onEvent(TaskEvent.DeleteTask(it)) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = { showClearCompletedDialog = false },
            title = {
                Text("Clear Completed Tasks")
            },
            text = {
                Text("Are you sure you want to delete all completed tasks? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TaskEvent.ClearCompleted)
                        showClearCompletedDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCompletedDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HeaderSection(
    userName: String,
    activeTasksCount: Int,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello $userName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SpaceWhite
            )
            Text(
                text = "$activeTasksCount active tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = SpaceWhite.copy(alpha = 0.7f)
            )
        }
        TextButton(onClick = onClearCompleted) {
            Text(
                text = "Clear Completed",
                color = SpaceBlueLight
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TaskFilter.values()) { filter ->
            FilterChip(
                selected = currentFilter == filter,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            TaskFilter.ALL -> "All Tasks"
                            TaskFilter.ACTIVE -> "Active"
                            TaskFilter.COMPLETED -> "Completed"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (filter) {
                            TaskFilter.ALL -> Icons.Default.Inbox
                            TaskFilter.ACTIVE -> Icons.Default.AssignmentTurnedIn
                            TaskFilter.COMPLETED -> Icons.Default.TaskAlt
                        },
                        contentDescription = null
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpaceBlueLight.copy(alpha = 0.2f),
                    selectedLabelColor = SpaceBlueLight,
                    selectedLeadingIconColor = SpaceBlueLight
                )
            )
        }
    }
}

@Composable
fun EmptyListPlaceholder(
    filter: TaskFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (filter) {
                TaskFilter.ALL -> Icons.Default.Inbox
                TaskFilter.ACTIVE -> Icons.Default.AssignmentTurnedIn
                TaskFilter.COMPLETED -> Icons.Default.TaskAlt
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (filter) {
                TaskFilter.ALL -> "No tasks yet"
                TaskFilter.ACTIVE -> "No active tasks"
                TaskFilter.COMPLETED -> "No completed tasks"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (filter) {
                TaskFilter.ALL -> "Add your first task to get started"
                TaskFilter.ACTIVE -> "All tasks are completed!"
                TaskFilter.COMPLETED -> "Complete some tasks to see them here"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
