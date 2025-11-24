package com.example.appstorefit_grupo1.ui.ViewModel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.ServiceUrls
import com.example.appstorefit_grupo1.data.remote.support.SupportApi
import com.example.appstorefit_grupo1.data.repository.MensajeRepository
import com.example.appstorefit_grupo1.session.SessionManager

class MensajesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 1) Crear instancia de la API de soporte usando RemoteModule + ServiceUrls
        val supportApi: SupportApi = RemoteModule.create(
            ServiceUrls.SUPPORT_BASE_URL,
            SupportApi::class.java
        )

        // 2) Crear repo de mensajes con la API y la sesión
        val repo = MensajeRepository(
            api = supportApi,
            session = SessionManager
        )

        // 3) Devolver el ViewModel
        return MensajesViewModel(repo) as T
    }
}