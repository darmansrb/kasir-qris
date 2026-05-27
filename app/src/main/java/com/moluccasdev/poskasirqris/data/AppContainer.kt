package com.moluccasdev.poskasirqris.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

interface AppContainer {
    val appRepository: AppRepository
}

class AppContainerImpl(private val context: Context) : AppContainer {
    private val databaseScope = CoroutineScope(SupervisorJob())
    
    override val appRepository: AppRepository by lazy {
        AppRepositoryImpl(AppDatabase.getDatabase(context, databaseScope))
    }
}
