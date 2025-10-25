package com.example.appstorefit_grupo1.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.local.database.AppDatabase

class AdminReportesViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val db by lazy { AppDatabase.getInstance(context) }
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AdminReportesViewModel(db) as T
}