package com.example.appstorefit_grupo1.ui.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.UserRepository

// Factory que arma el UserRepository desde Room usando el Context de la app
class AuthViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val context = appContext.applicationContext
        val db = AppDatabase.getInstance(context)
        val repo = UserRepository(
            db = db,
            userDao = db.userDao(),
            registroDao = db.registroDao(),
            rolDao = db.rolDao()
        )
        return AuthViewModel(repo, context) as T
    }
}
