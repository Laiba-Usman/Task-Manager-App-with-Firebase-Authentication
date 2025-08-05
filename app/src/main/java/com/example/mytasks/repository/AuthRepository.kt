package com.example.mytasks.repository

import android.util.Log
import com.example.mytasks.data.entity.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {

    val currentUser: FirebaseUser? = auth.currentUser

    /**
     * Provides a Flow of the current FirebaseUser, listening for authentication state changes.
     */
    fun getCurrentUserFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            Log.d("AuthRepository", "Auth state changed: ${auth.currentUser?.email}")
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Registers a new user with email and password and saves user data to the database.
     */
    suspend fun register(email: String, password: String, name: String): AuthResult {
        return try {
            Log.d("AuthRepository", "Attempting to register user: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Registration failed - no UID")

            val user = User(
                uid = uid,
                name = name,
                email = email,
                createdAt = Date(),
                lastLoginTime = Date()
            )

            // Convert User to HashMap for Firebase
            val userMap = hashMapOf(
                "uid" to user.uid,
                "name" to user.name,
                "email" to user.email,
                "createdAt" to user.createdAt.time,
                "lastLoginTime" to user.lastLoginTime.time
            )

            database.reference.child("users").child(uid).setValue(userMap).await()
            Log.d("AuthRepository", "User registered successfully")
            AuthResult.Success
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration failed", e)
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    /**
     * Logs in a user with email and password and updates their last login time.
     */
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            Log.d("AuthRepository", "Attempting to login user: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Login failed - no UID")

            // Update last login time
            database.reference.child("users").child(uid).child("lastLoginTime")
                .setValue(Date().time).await()

            Log.d("AuthRepository", "User logged in successfully")
            AuthResult.Success
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    /**
     * Signs out the current user.
     */
    suspend fun logout() {
        try {
            Log.d("AuthRepository", "Logging out user")
            auth.signOut()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout failed", e)
        }
    }

    /**
     * Fetches the current user's data from the database.
     */
    suspend fun getCurrentUserData(): User? {
        return try {
            val uid = currentUser?.uid ?: return null
            val snapshot = database.reference.child("users").child(uid).get().await()

            // Convert snapshot to User manually
            val data = snapshot.value as? Map<*, *> ?: return null
            User(
                uid = data["uid"] as? String ?: "",
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                lastLoginTime = (data["lastLoginTime"] as? Long)?.let { Date(it) } ?: Date(),
                createdAt = (data["createdAt"] as? Long)?.let { Date(it) } ?: Date()
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to get user data", e)
            null
        }
    }

    /**
     * Provides a Flow of user data for a given UID, listening for real-time changes.
     */
    fun getUserData(uid: String): Flow<User?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val data = snapshot.value as? Map<*, *>
                    val user = if (data != null) {
                        User(
                            uid = data["uid"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            lastLoginTime = (data["lastLoginTime"] as? Long)?.let { Date(it) } ?: Date(),
                            createdAt = (data["createdAt"] as? Long)?.let { Date(it) } ?: Date()
                        )
                    } else null
                    trySend(user)
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Error parsing user data", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AuthRepository", "Database error: ${error.message}")
                trySend(null)
            }
        }

        database.reference.child("users").child(uid).addValueEventListener(listener)
        awaitClose { database.reference.child("users").child(uid).removeEventListener(listener) }
    }
}