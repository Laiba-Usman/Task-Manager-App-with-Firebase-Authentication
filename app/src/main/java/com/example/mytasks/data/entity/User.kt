package com.example.mytasks.data.entity

import java.util.Date

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val lastLoginTime: Date = Date(),
    val createdAt: Date = Date()
)