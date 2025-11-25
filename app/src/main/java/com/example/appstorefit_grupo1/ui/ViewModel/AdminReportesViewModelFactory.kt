package com.example.appstorefit_grupo1.ui.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.ServiceUrls
import com.example.appstorefit_grupo1.data.remote.users.UsersApi
import com.example.appstorefit_grupo1.data.repository.UserRepository

class AdminReportesViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val api: UsersApi by lazy {
        RemoteModule.create(ServiceUrls.USERS_BASE_URL, UsersApi::class.java)
    }
    private val repo by lazy { UserRepository(api) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AdminReportesViewModel(repo) as T
}
