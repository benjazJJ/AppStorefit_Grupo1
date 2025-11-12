package com.example.appstorefit_grupo1.ui.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.CategoriaRepository



// Factory simple sin DI para obtener el repo desde Room
class AdminCategoriasViewModelFactory(context: Context) : ViewModelProvider.Factory {
    // Creamos el repo una vez reutilizando la instancia de DB
    private val repo by lazy {
        val db = AppDatabase.getInstance(context)
        CategoriaRepository(db.categoriaDao())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminCategoriasViewModel(repo) as T
    }
}
