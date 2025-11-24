package com.example.appstorefit_grupo1.ui.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.ServiceUrls
import com.example.appstorefit_grupo1.data.remote.catalog.CatalogApi
import com.example.appstorefit_grupo1.data.repository.ProductosRepository

class AdminProductsViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val repo by lazy {
        val db = AppDatabase.getInstance(context)
        val api = RemoteModule.create(ServiceUrls.CATALOG_BASE_URL, CatalogApi::class.java)
        ProductosRepository(db.productosDao(), api)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminProductsViewModel(repo) as T
    }
}
