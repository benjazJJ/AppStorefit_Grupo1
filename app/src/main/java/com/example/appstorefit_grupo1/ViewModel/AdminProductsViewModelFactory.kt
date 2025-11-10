package com.example.appstorefit_grupo1.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.ProductosRepository

class AdminProductsViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val repo by lazy {
        val dao = AppDatabase.getInstance(context).productosDao()
        ProductosRepository(dao)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminProductsViewModel(repo) as T
    }
}
