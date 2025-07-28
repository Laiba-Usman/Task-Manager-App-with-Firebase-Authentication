package com.example.mytasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mytasks.ui.components.TaskItem
import com.example.mytasks.viewmodel.TaskEvent
import com.example.mytasks.viewmodel.TaskFilter
import com.example.mytasks.viewmodel.TaskViewModel
import com.example.mytasks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()
    val activeTasksCount by viewModel.activeTasksCount.collectAsStateWithLifecycle()

    val currentTasks = when (uiState.currentFilter) {
        TaskFilter.ALL -> allTasks
        TaskFilter.ACTIVE -> activeTasks
        TaskFilter.COMPLETED -> completedTasks
    }

    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SpaceBlack,
                        SpaceGray.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "My Tasks",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$activeTasksCount active tasks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                if (completedTasks.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteAllDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Completed",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SpaceBlack.copy(alpha = 0.95f)
            )
        )

        // Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TaskFilter.entries) { filter ->
                FilterChip(
                    selected = uiState.currentFilter == filter,
                    onClick = { viewModel.onEvent(TaskEvent.SetFilter(filter)) },
                    label = {
                        Text(
                            text = when (filter) {
                                TaskFilter.ALL -> "${filter.displayName} (${allTasks.size})"
                                TaskFilter.ACTIVE -> "${filter.displayName} (${activeTasks.size})"
                                TaskFilter.COMPLETED -> "${filter.displayName} (${completedTasks.size})"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (filter) {
                                TaskFilter.ALL -> Icons.Default.List
                                TaskFilter.ACTIVE -> Icons.Default.Circle
                                TaskFilter.COMPLETED -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpaceBlueLight,
                        selectedLabelColor = SpaceWhite
                    )
                )
            }
        }

        // Task List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (currentTasks.isEmpty()) {
                EmptyState(
                    filter = uiState.currentFilter,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentTasks) { task ->
                        TaskItem(
                            task = task,
                            onToggleComplete = { id, isCompleted ->
                                viewModel.onEvent(TaskEvent.ToggleTaskCompletion(id, isCompleted))
                            },
                            onEdit = onNavigateToEditTask,
                            onDelete = { taskToDelete ->
                                viewModel.onEvent(TaskEvent.DeleteTask(taskToDelete))
                            }
                        )
                    }

                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = SpaceBlueLight,
                contentColor = SpaceWhite
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    }

    // Delete All Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Clear Completed Tasks") },
            text = { Text("Are you sure you want to delete all completed tasks? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TaskEvent.DeleteCompletedTasks)
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyState(
    filter: TaskFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
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