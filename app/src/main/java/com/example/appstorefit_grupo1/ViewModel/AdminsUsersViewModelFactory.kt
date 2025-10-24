package com.example.appstorefit_grupo1.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.UserRepository

class AdminsUsersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext
        val db  = AppDatabase.getInstance(app)

        val repo = UserRepository(
            db          = db,
            userDao     = db.userDao(),
            registroDao = db.registroDao(),
            rolDao      = db.rolDao()
        )

        return AdminUsuariosViewModel(repo) as T
    }
}
