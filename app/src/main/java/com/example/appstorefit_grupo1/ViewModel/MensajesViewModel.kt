package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeEntity
import com.example.appstorefit_grupo1.data.repository.MensajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class EnvioMensajeUi(
    val enviando: Boolean = false,
    val ok: Boolean = false,
    val error: String? = null
)

class MensajesViewModel(
    private val repo: MensajeRepository
) : ViewModel() {

    private val _envio = MutableStateFlow(EnvioMensajeUi())
    val envio: StateFlow<EnvioMensajeUi> = _envio.asStateFlow()

    //Bandeja para soporte (targetRoleId = 3 por defecto)
    fun observarBandejaSoporte(idRolSoporte: Int = 3): Flow<List<MensajeEntity>> =
        repo.observarBandejaSoporte(idRolSoporte)

    //Historial por remitente (cliente)
    fun observarMensajesDe(idUsuario: Long): Flow<List<MensajeEntity>> =
        repo.observarMensajesDe(idUsuario)

    //Enviar mensaje
    fun enviarMensaje(idUsuarioRemitente: Long, idRolDestinoSoporte: Int, contenido: String) {
        viewModelScope.launch {
            _envio.value = EnvioMensajeUi(enviando = true)
            try {
                repo.enviarMensaje(idUsuarioRemitente, idRolDestinoSoporte, contenido)
                _envio.value = EnvioMensajeUi(ok = true)
            } catch (e: Exception) {
                _envio.value = EnvioMensajeUi(error = e.message ?: "Error desconocido")
            }
        }
    }

    //Marcar como leído
    fun marcarComoLeido(idMensaje: Long) {
        viewModelScope.launch { runCatching { repo.marcarComoLeido(idMensaje) } }
    }

    //Reset del estado de envío
    fun limpiarEstadoEnvio() {
        _envio.value = EnvioMensajeUi()
    }
}
