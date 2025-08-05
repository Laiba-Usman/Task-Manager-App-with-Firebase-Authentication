package com.example.mytasks.di

import android.content.Context
import com.example.mytasks.data.datastore.UserPreferencesRepository
import com.example.mytasks.repository.AuthRepository
import com.example.mytasks.repository.FirebaseTaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): AuthRepository {
        return AuthRepository(auth, database)
    }

    @Provides
    @Singleton
    fun provideFirebaseTaskRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): FirebaseTaskRepository {
        return FirebaseTaskRepository(auth, database)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }
}