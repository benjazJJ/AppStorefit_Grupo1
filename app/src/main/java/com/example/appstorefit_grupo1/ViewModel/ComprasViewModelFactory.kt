package com.example.appstorefit_grupo1.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.CompraRepository

class ComprasViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(context)
        val repo = CompraRepository(db.compraDao())
        @Suppress("UNCHECKED_CAST")
        return ComprasViewModel(repo) as T
    }
}