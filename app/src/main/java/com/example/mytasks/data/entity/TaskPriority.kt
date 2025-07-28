package com.example.mytasks.data.entity


import androidx.compose.ui.graphics.Color

enum class TaskPriority(val displayName: String, val color: Color) {
    LOW("Low", Color(0xFF4CAF50)),
    MEDIUM("Medium", Color(0xFFFF9800)),
    HIGH("High", Color(0xFFE91E63)),
    URGENT("Urgent", Color(0xFFE53E3E))
}