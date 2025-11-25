package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.user.AdminUserRow
import com.example.appstorefit_grupo1.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminReportesViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _totalUsuarios = MutableStateFlow(0)
    val totalUsuarios: StateFlow<Int> = _totalUsuarios

    private val _ultimosRegistrados = MutableStateFlow<List<AdminUserRow>>(emptyList())
    val ultimosRegistrados: StateFlow<List<AdminUserRow>> = _ultimosRegistrados

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            userRepository.adminListUsers()
                .onSuccess { list ->
                    _totalUsuarios.value = list.size
                    // Sin fecha de creación en el DTO remoto; tomamos los últimos 20 del listado recibido.
                    _ultimosRegistrados.value = list.takeLast(20).reversed()
                }
                .onFailure {
                    _totalUsuarios.value = 0
                    _ultimosRegistrados.value = emptyList()
                }
        }
    }
}
