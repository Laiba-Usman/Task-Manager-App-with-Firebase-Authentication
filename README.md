# 🌌 Taskify – Your Personal Space for Tasks  
**"Keep calm and check it off – every task counts!"**

---

## 🚀 What is Taskify?

Taskify is a modern Android app designed with **Kotlin**, built using **Jetpack Compose**, **Room Database**, and **MVVM architecture**.  
With its sleek, galaxy-themed UI, this app makes managing daily tasks both aesthetic and efficient — even when you’re offline.

---

## 🎬 App Preview  
👇 Curious how it looks?

<p align="center">
  <a href="https://github.com/Laiba-Usman/Task-Manager-App-Kotlin/raw/master/Task%20Manager%20App.mp4" target="_blank">
    <img src="https://img.shields.io/badge/🎥 Demo_Video-%2300C897?style=for-the-badge&logo=android&logoColor=white" alt="Demo">
  </a>
</p>

---

## ✍️ Core Features

| 🌟 Feature        | 📝 Description                                           |
|------------------|----------------------------------------------------------|
| ➕ Add Task       | Add tasks with title, detail, deadline, and priority     |
| 📃 Task Overview | See your tasks neatly listed using `LazyColumn`          |
| 🛠️ Edit Task     | Update tasks anytime with just a few taps                |
| 🗑️ Remove Task   | Delete tasks via swipe or delete icon                    |
| 🎯 Set Priorities| Color-coded labels make high-priority tasks pop!         |
| ✅ Mark as Done   | Toggle completion status in real time                   |

---

## 🧱 Tech Stack Breakdown

| Layer              | Tools Used                                |
|--------------------|--------------------------------------------|
| 🎨 UI Design       | Jetpack Compose with a cosmic theme        |
| 💾 Local Storage   | Room Database (SQLite)                     |
| 🧠 Architecture    | MVVM (Model-View-ViewModel)                |
| 🔄 State Mgmt      | LiveData + Compose State APIs              |
| 🚦 Navigation      | Jetpack Navigation for Compose             |

---

## 📸 App Highlights

- 🪐 **All Tasks Screen** – View everything in card format  
- 📝 **Task Editor** – Create or modify task details  
- 🎨 **Priority Indicator** – Visual priority badges  
- ✔️ **One-tap Complete** – Flip status with a toggle  
- 🌙 **Dark Mode UI** – Inspired by outer space colors  

---

## 📁 Project Structure

| 📂 File               | 🧩 Responsibility                             |
|----------------------|----------------------------------------------|
| `Task.kt`            | Entity model class for tasks                 |
| `TaskDao.kt`         | Data Access Object (DAO) interface           |
| `TaskDatabase.kt`    | Room DB setup & instance handling            |
| `TaskRepository.kt`  | Abstraction layer over DAO                   |
| `TaskViewModel.kt`   | Business logic + LiveData exposure           |
| `MainActivity.kt`    | App launch entry point                       |
| `NavGraph.kt`        | Navigation graph with Compose destinations   |
| `TaskListScreen.kt`  | Displays list of tasks                       |
| `AddEditTaskScreen.kt`| UI for adding/editing a task               |
| `TaskItem.kt`        | Reusable UI card for each task               |

---

## 🌟 Why Try Taskify?

✨ Minimal yet modern UI  
🧭 Seamless user flow with navigation  
📶 Offline access using Room  
🧰 Good starter for learning Compose + MVVM  
💼 Perfect addition to your Android dev portfolio

---

🔗Made with Kotlin and compose magic by Laiba [https://github.com/Laiba-Usman]
