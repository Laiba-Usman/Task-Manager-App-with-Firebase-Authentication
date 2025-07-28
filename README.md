# 🗂 My Tasks – A Personal Task Manager App

> *“Stay focused. Stay in control. Because every task deserves your attention.”*

---

## 🎯 Overview

**My Tasks** is a Kotlin-based personal task manager app built using **Jetpack Compose**, **MVVM**, and **Room Database**.  
It features a beautiful dark, space-themed UI and helps you stay productive by allowing you to add, update, complete, and manage your daily tasks — all with full offline support.

---

## 📽 Demo Video

👉 Want to see the app in action?

<p align="center">
  <a href="https://github.com/Laiba-Usman/Task-Manager-App-Kotlin/blob/main/To-Do-App.mp4" target="_blank">
    <img src="https://img.shields.io/badge/🎬 Watch_Demo-%23FF0000?style=for-the-badge&logo=YouTube&logoColor=white" alt="Watch Demo">
  </a>
</p>

---

## 🧠 Features

| ✅ Feature              | 💬 Description                                                                 |
|------------------------|---------------------------------------------------------------------------------|
| ➕ *Add Tasks*          | Add tasks with title, description, due date, and priority level.               |
| 📋 *View Tasks*         | Display all tasks in a scrollable list using LazyColumn.                      |
| ✏ *Edit/Update*        | Modify existing tasks anytime with ease.                                       |
| ❌ *Delete Tasks*       | Remove tasks via swipe or delete button.                                       |
| 📌 *Task Priority*      | Visually tag tasks using color-coded priority markers.                         |
| ✔ *Task Status*        | Mark tasks as complete/incomplete with a toggle.                               |

---

## 🧰 Tech Stack

| Layer            | Tools / Frameworks Used                  |
|------------------|------------------------------------------|
| 🖼 UI            | Jetpack Compose (Dark Space Theme)       |
| 🗃 Database      | Room (SQLite persistence)                |
| 🏛 Architecture  | MVVM (Model–View–ViewModel)              |
| 🔁 State Mgmt    | LiveData & Compose State APIs            |
| 🧭 Navigation    | Jetpack Compose Navigation               |

---

## 🖼 App Screens

- 🏠 Task List – All tasks displayed in cards  
- ➕ Add/Edit Task – Create or modify tasks  
- 📌 Priority markers – Color-coded for urgency  
- ✔ Toggle – One-tap mark as complete/incomplete  
- 🌌 Dark Theme – Space-inspired UI (black, blue, purple)

---

## 📂 File Structure

| 📄 File Name            | 📌 Responsibility                                |
|------------------------|--------------------------------------------------|
| `Task.kt`              | Data class for task entity                       |
| `TaskDao.kt`           | Room DAO interface for database operations       |
| `TaskDatabase.kt`      | Singleton Room database                          |
| `TaskRepository.kt`    | Data source abstraction                          |
| `TaskViewModel.kt`     | Exposes data and handles business logic          |
| `MainActivity.kt`      | Compose launcher & app entry point               |
| `NavGraph.kt`          | Manages navigation between Compose screens       |
| `TaskListScreen.kt`    | Screen showing all tasks                         |
| `AddEditTaskScreen.kt` | Screen for adding/updating tasks                 |
| `TaskItem.kt`          | Reusable UI for rendering a single task item     |

---

## ✨ Why You'll Love It

- 🪐 **Modern space-themed design**  
- 🧠 **Clean MVVM architecture**  
- ⚡ **Smooth navigation & real-time UI updates**  
- 💾 **Offline support with Room DB**  
- 📱 **Great project to learn Compose, LiveData, and state handling**

---

Made with 💜 Kotlin & Compose magic by Laiba [https://github.com/Laiba-Usman]
