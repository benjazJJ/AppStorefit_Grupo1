package com.example.appstorefit_grupo1.ui.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.ServiceUrls
import com.example.appstorefit_grupo1.data.remote.users.UsersApi
import com.example.appstorefit_grupo1.data.repository.UserRepository

class AdminsUsersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = RemoteModule.create(
            baseUrl = ServiceUrls.USERS_BASE_URL,
            service = UsersApi::class.java
        )
        val repo = UserRepository(api = api)

        return AdminUsuariosViewModel(repo) as T
    }
}
