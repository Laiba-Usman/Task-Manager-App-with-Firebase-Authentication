package com.example.mytasks.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.mytasks.data.entity.TaskPriority

// Extension property to get colors for TaskPriority in UI
val TaskPriority.color: Color
    get() = when (this) {
        TaskPriority.LOW -> Color(0xFF4CAF50) // Green
        TaskPriority.MEDIUM -> Color(0xFFFF9800) // Orange
        TaskPriority.HIGH -> Color(0xFFFF5722) // Red-Orange
        TaskPriority.URGENT -> Color(0xFFF44336) // Red
    }