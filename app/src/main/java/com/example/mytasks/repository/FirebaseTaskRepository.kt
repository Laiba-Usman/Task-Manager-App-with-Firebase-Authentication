package com.example.mytasks.repository

import com.example.mytasks.data.entity.Task
import com.example.mytasks.data.entity.TaskPriority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTaskRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private fun getUserTasksRef() =
        auth.currentUser?.uid?.let { uid ->
            database.reference.child("tasks").child(uid)
        }

    fun getAllTasks(): Flow<List<Task>> = callbackFlow {
        val ref = getUserTasksRef()
        if (ref == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = snapshot.children.mapNotNull { child ->
                    child.getValue(FirebaseTask::class.java)?.toTask(child.key ?: "")
                }
                trySend(tasks)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Propagate error
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun insertTask(task: Task) {
        val ref = getUserTasksRef() ?: return
        val key = ref.push().key ?: return
        ref.child(key).setValue(FirebaseTask.fromTask(task)).await()
    }

    suspend fun updateTask(task: Task) {
        val ref = getUserTasksRef() ?: return
        val key = task.idString
        if (key.isNotBlank()) {
            ref.child(key).setValue(FirebaseTask.fromTask(task)).await()
        }
    }

    suspend fun deleteTask(task: Task) {
        val ref = getUserTasksRef() ?: return
        val key = task.idString
        if (key.isNotBlank()) {
            ref.child(key).removeValue().await()
        }
    }

    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        val ref = getUserTasksRef() ?: return
        val updates = mapOf(
            "completed" to isCompleted,
            "updatedAt" to Date().time
        )
        ref.child(taskId).updateChildren(updates).await()
    }

    suspend fun getTaskById(taskId: String): Task? {
        val ref = getUserTasksRef() ?: return null
        val snapshot = ref.child(taskId).get().await()
        return snapshot.getValue(FirebaseTask::class.java)?.toTask(taskId)
    }

    // Helper data class for Firebase serialization
    data class FirebaseTask(
        val title: String? = null,
        val description: String? = null,
        val date: Long? = null,
        val priority: String? = null,
        val completed: Boolean? = false, // Renamed to match updateTaskCompletion
        val createdAt: Long? = null,
        val updatedAt: Long? = null
    ) {
        fun toTask(id: String): Task? {
            return try {
                Task(
                    idString = id,
                    title = this.title ?: "",
                    description = this.description ?: "",
                    date = this.date?.let { Date(it) } ?: Date(),
                    priority = this.priority?.let { TaskPriority.valueOf(it) } ?: TaskPriority.MEDIUM,
                    isCompleted = this.completed ?: false,
                    createdAt = this.createdAt?.let { Date(it) } ?: Date(),
                    updatedAt = this.updatedAt?.let { Date(it) } ?: Date()
                )
            } catch (e: Exception) {
                null
            }
        }

        companion object {
            fun fromTask(task: Task): FirebaseTask {
                return FirebaseTask(
                    title = task.title,
                    description = task.description,
                    date = task.date.time,
                    priority = task.priority.name,
                    completed = task.isCompleted,
                    createdAt = task.createdAt.time,
                    updatedAt = task.updatedAt.time
                )
            }
        }
    }
}