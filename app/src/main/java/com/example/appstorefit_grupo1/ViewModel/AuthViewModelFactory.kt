package com.example.appstorefit_grupo1.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.UserRepository

/**
 * Factory que arma el UserRepository desde Room usando el Context de la app.
 */
class AuthViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(appContext)
        val repo = UserRepository(
            userDao = db.userDao(),
            registroDao = db.registroDao()
        )
        return AuthViewModel(repo) as T
    }
}
