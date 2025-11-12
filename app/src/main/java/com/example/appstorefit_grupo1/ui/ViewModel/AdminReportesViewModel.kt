package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.local.user.AdminUserRow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AdminReportesViewModel(db: AppDatabase) : ViewModel() {

    private val userDao = db.userDao()
    private val registroDao = db.registroDao()

    // total de usuarios en vivo
    val totalUsuarios: StateFlow<Int> =
        userDao.observeCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // últimos registrados (usando AdminUserRow)
    val ultimosRegistrados: StateFlow<List<AdminUserRow>> =
        registroDao.observeUltimosAdminUsers(limitRows = 20)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}


