package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.repository.UserRepository

@Suppress("UNCHEKED_CAST")
class AuthViewModelFactory (
    private val repository: UserRepository
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AuthViewModel::class.java)){
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Error Desconocido de clas: ${modelClass.name}")
    }
}